package com.twenty.app.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twenty.app.data.Session
import com.twenty.app.data.Storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val storage: Storage,
    private val timerViewModel: TimerViewModel
) : ViewModel() {

    private val _sessionState = MutableStateFlow("idle")
    val sessionState: StateFlow<String> = _sessionState.asStateFlow()

    private val _breaksTaken = MutableStateFlow(0)
    val breaksTaken: StateFlow<Int> = _breaksTaken.asStateFlow()

    private val _breaksSkipped = MutableStateFlow(0)
    val breaksSkipped: StateFlow<Int> = _breaksSkipped.asStateFlow()

    private val _breaksTriggered = MutableStateFlow(0)
    val breaksTriggered: StateFlow<Int> = _breaksTriggered.asStateFlow()

    private val _completedSession = MutableStateFlow<Session?>(null)
    val completedSession: StateFlow<Session?> = _completedSession.asStateFlow()

    private var sessionStartTime: Long = 0

    private var onStartSession: (() -> Unit)? = null
    private var onEndSession: (() -> Unit)? = null
    private var onBreakConfirmed: (() -> Unit)? = null
    private var onBreakSkipped: (() -> Unit)? = null
    private var onBreakTriggered: (() -> Unit)? = null

    private var pendingElapsedAtPause: Long = 0

    init {
        timerViewModel.setBreakTriggerListener {
            pendingElapsedAtPause = timerViewModel.elapsed.value
            timerViewModel.pause()
            _breaksTriggered.value = _breaksTriggered.value + 1
            _sessionState.value = "break_pending"
            onBreakTriggered?.invoke()
        }
    }

    fun setSessionListeners(
        onStart: () -> Unit,
        onEnd: () -> Unit,
        onConfirm: () -> Unit,
        onSkip: () -> Unit,
        onTrigger: () -> Unit
    ) {
        onStartSession = onStart
        onEndSession = onEnd
        onBreakConfirmed = onConfirm
        onBreakSkipped = onSkip
        onBreakTriggered = onTrigger
    }

    fun handleStart() {
        timerViewModel.reset()
        _breaksTaken.value = 0
        _breaksSkipped.value = 0
        _breaksTriggered.value = 0
        _completedSession.value = null
        sessionStartTime = System.currentTimeMillis()
        _sessionState.value = "active"
        timerViewModel.start()
        onStartSession?.invoke()
    }

    fun handleEnd(sessions: List<Session>, onSave: (List<Session>) -> Unit) {
        timerViewModel.stop()
        val elapsed = timerViewModel.elapsed.value
        val endTime = System.currentTimeMillis()

        val session = Session(
            id = storage.generateId(),
            profileId = "default",
            startTime = sessionStartTime,
            endTime = endTime,
            durationMs = elapsed,
            breaksTriggered = _breaksTriggered.value,
            breaksTaken = _breaksTaken.value,
            breaksSkipped = _breaksSkipped.value,
            complianceRate = if (_breaksTriggered.value > 0) {
                _breaksTaken.value.toFloat() / _breaksTriggered.value
            } else 0f
        )

        _completedSession.value = session
        val updated = listOf(session) + sessions
        onSave(updated)
        _sessionState.value = "summary"
    }

    fun handleBreakTake() {
        _sessionState.value = "break_active"
        timerViewModel.startBreakCountdown()
    }

    fun handleBreakSkipSession() {
        handleEnd(emptyList()) { updated ->
            viewModelScope.launch { storage.saveSessions(updated) }
        }
    }

    fun handleBreakConfirm() {
        timerViewModel.endBreakCountdown()
        _breaksTaken.value = _breaksTaken.value + 1
        _sessionState.value = "active"
        timerViewModel.resumeAfterBreak()
        onBreakConfirmed?.invoke()
    }

    fun handleBreakSkip() {
        timerViewModel.endBreakCountdown()
        _breaksSkipped.value = _breaksSkipped.value + 1
        _sessionState.value = "active"
        timerViewModel.resumeAfterBreak()
        onBreakSkipped?.invoke()
    }

    fun handleDismissSummary() {
        _sessionState.value = "idle"
        _completedSession.value = null
        timerViewModel.reset()
    }
}
