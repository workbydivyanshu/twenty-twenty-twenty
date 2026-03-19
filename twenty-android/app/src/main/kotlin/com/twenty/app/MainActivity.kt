package com.twenty.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.twenty.app.data.Settings
import com.twenty.app.data.Storage
import com.twenty.app.domain.SessionViewModel
import com.twenty.app.domain.TimerViewModel
import com.twenty.app.platform.HapticFeedback
import com.twenty.app.platform.NotificationHelper
import com.twenty.app.platform.SoundManager
import com.twenty.app.platform.TimerForegroundService
import com.twenty.app.ui.TwentyApp
import com.twenty.app.ui.theme.TwentyTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var storage: Storage
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var sessionViewModel: SessionViewModel
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var soundManager: SoundManager
    private lateinit var hapticFeedback: HapticFeedback

    private var notificationUpdateJob: Job? = null

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                NotificationHelper.ACTION_STOP -> {
                    lifecycleScope.launch {
                        val sessions = storage.sessionsFlow.first()
                        sessionViewModel.handleEnd(sessions) { updated ->
                            lifecycleScope.launch { storage.saveSessions(updated) }
                        }
                    }
                }
                NotificationHelper.ACTION_BREAK_CONFIRM -> {
                    sessionViewModel.handleBreakConfirm()
                    lifecycleScope.launch {
                        val s = storage.settingsFlow.first()
                        if (s.soundEnabled) soundManager.playConfirm(s.volume)
                    }
                    hapticFeedback.light()
                }
                NotificationHelper.ACTION_BREAK_SKIP -> {
                    sessionViewModel.handleBreakSkip()
                    lifecycleScope.launch {
                        val s = storage.settingsFlow.first()
                        if (s.soundEnabled) soundManager.playSkip(s.volume)
                    }
                    hapticFeedback.light()
                }
                NotificationHelper.ACTION_BREAK_TAKE -> {
                    sessionViewModel.handleBreakTake()
                }
                NotificationHelper.ACTION_BREAK_SKIP_SESSION -> {
                    lifecycleScope.launch {
                        val sessions = storage.sessionsFlow.first()
                        sessionViewModel.handleEnd(sessions) { updated ->
                            lifecycleScope.launch { storage.saveSessions(updated) }
                        }
                    }
                }
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storage = Storage(applicationContext)
        timerViewModel = TimerViewModel()
        sessionViewModel = SessionViewModel(storage, timerViewModel)
        notificationHelper = NotificationHelper(applicationContext)
        soundManager = SoundManager(applicationContext)
        hapticFeedback = HapticFeedback(applicationContext)

        val filter = IntentFilter().apply {
            addAction(NotificationHelper.ACTION_STOP)
            addAction(NotificationHelper.ACTION_BREAK_CONFIRM)
            addAction(NotificationHelper.ACTION_BREAK_SKIP)
            addAction(NotificationHelper.ACTION_BREAK_TAKE)
            addAction(NotificationHelper.ACTION_BREAK_SKIP_SESSION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val settings by storage.settingsFlow.collectAsState(initial = Settings())
            val sessions by storage.sessionsFlow.collectAsState(initial = emptyList())

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> timerViewModel.onAppBackground()
                        Lifecycle.Event.ON_RESUME -> timerViewModel.onAppForeground()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    timerViewModel.cleanup()
                    notificationUpdateJob?.cancel()
                }
            }

            LaunchedEffect(Unit) {
                sessionViewModel.setSessionListeners(
                    onStart = { if (settings.soundEnabled) soundManager.playStart(settings.volume) },
                    onEnd = { if (settings.soundEnabled) soundManager.playEnd(settings.volume); hapticFeedback.heavy() },
                    onConfirm = { if (settings.soundEnabled) soundManager.playConfirm(settings.volume); hapticFeedback.light() },
                    onSkip = { if (settings.soundEnabled) soundManager.playSkip(settings.volume); hapticFeedback.light() }
                )
            }

            val sessionState by sessionViewModel.sessionState.collectAsState()
            val isBreakActive by timerViewModel.isBreakActive.collectAsState()
            val breakCountdown by timerViewModel.breakCountdown.collectAsState()
            val isRunning by timerViewModel.isRunning.collectAsState()

            LaunchedEffect(isRunning, isBreakActive, sessionState) {
                when {
                    isBreakActive -> {
                        startForegroundService()
                        startNotificationUpdateLoop()
                    }
                    sessionState == "break_pending" -> {
                        startForegroundService()
                        startNotificationUpdateLoop()
                    }
                    isRunning -> {
                        startForegroundService()
                        startNotificationUpdateLoop()
                    }
                    else -> {
                        stopForegroundService()
                        notificationUpdateJob?.cancel()
                        notificationHelper.cancelNotification()
                    }
                }
            }

            TwentyTheme {
                val scope = rememberCoroutineScope()
                TwentyApp(
                    settings = settings,
                    sessions = sessions,
                    timerViewModel = timerViewModel,
                    sessionViewModel = sessionViewModel,
                    onUpdateSettings = { scope.launch { storage.saveSettings(it) } },
                    onSaveSessions = { scope.launch { storage.saveSessions(it) } },
                    onStartSession = { if (settings.soundEnabled) soundManager.playStart(settings.volume) },
                    onEndSession = { if (settings.soundEnabled) soundManager.playEnd(settings.volume); hapticFeedback.heavy() },
                    onBreakConfirmed = { if (settings.soundEnabled) soundManager.playConfirm(settings.volume); hapticFeedback.light() },
                    onBreakSkipped = { if (settings.soundEnabled) soundManager.playSkip(settings.volume); hapticFeedback.light() }
                )
            }
        }
    }

    private fun startForegroundService() {
        val intent = Intent(this, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(this, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        startService(intent)
    }

    private fun startNotificationUpdateLoop() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = lifecycleScope.launch {
            while (isActive) {
                val elapsed = timerViewModel.elapsed.value
                val currentBreakActive = timerViewModel.isBreakActive.value
                val currentBreakCountdown = timerViewModel.breakCountdown.value
                val isRunning = timerViewModel.isRunning.value
                val currentSessionState = sessionViewModel.sessionState.value

                val title = when {
                    currentSessionState == "break_pending" -> "Time for a break!"
                    currentBreakActive && currentBreakCountdown > 0 -> "Break Time!"
                    currentBreakActive -> "Break Complete!"
                    isRunning -> "Twenty ·³"
                    else -> "Twenty ·³"
                }

                val content = when {
                    currentSessionState == "break_pending" -> "Tap to take your 20-second break"
                    currentBreakActive && currentBreakCountdown > 0 -> "Look away · ${currentBreakCountdown}s remaining"
                    currentBreakActive -> "Did you rest your eyes?"
                    isRunning -> formatDuration(elapsed)
                    else -> formatDuration(elapsed)
                }

                val notification = notificationHelper.buildNotification(
                    title = title,
                    content = content,
                    isBreakPending = currentSessionState == "break_pending",
                    isBreakActive = currentBreakActive && currentBreakCountdown > 0,
                    isSessionActive = !currentBreakActive && isRunning,
                    isBreakConfirmPending = currentBreakActive && currentBreakCountdown == 0
                )
                notificationHelper.showNotification(notification)

                delay(1000)
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "$hours:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
        } else {
            "$minutes:${String.format("%02d", seconds)}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(notificationReceiver)
        } catch (_: Exception) {}
    }
}
