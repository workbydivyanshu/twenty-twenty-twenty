package com.twenty.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.twenty.app.data.Settings
import com.twenty.app.data.Storage
import com.twenty.app.domain.SessionViewModel
import com.twenty.app.domain.TimerViewModel
import com.twenty.app.platform.HapticFeedback
import com.twenty.app.platform.SoundManager
import com.twenty.app.ui.TwentyApp
import com.twenty.app.ui.theme.TwentyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var storage: Storage
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var sessionViewModel: SessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storage = Storage(applicationContext)
        timerViewModel = TimerViewModel()
        sessionViewModel = SessionViewModel(storage, timerViewModel)

        setContent {
            val soundManager = remember { SoundManager(applicationContext) }
            val hapticFeedback = remember { HapticFeedback(applicationContext) }

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
}
