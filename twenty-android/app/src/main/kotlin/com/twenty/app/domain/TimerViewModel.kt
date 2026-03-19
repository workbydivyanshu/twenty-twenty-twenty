package com.twenty.app.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private var sessionStartTime: Long? = null
    private var pausedElapsed: Long = 0
    private var lastBreakTime: Long = 0
    private var tickJob: Job? = null

    private val _elapsed = MutableStateFlow(0L)
    val elapsed: StateFlow<Long> = _elapsed.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var intervalJob: Job? = null

    private val _nextBreakIn = MutableStateFlow<Int?>(null)
    val nextBreakIn: StateFlow<Int?> = _nextBreakIn.asStateFlow()

    private val _breakCountdown = MutableStateFlow(0)
    val breakCountdown: StateFlow<Int> = _breakCountdown.asStateFlow()

    private val _isBreakActive = MutableStateFlow(false)
    val isBreakActive: StateFlow<Boolean> = _isBreakActive.asStateFlow()

    private var onBreakTrigger: (() -> Unit)? = null

    fun setBreakTriggerListener(listener: () -> Unit) {
        onBreakTrigger = listener
    }

    fun start() {
        if (_isRunning.value) return
        sessionStartTime = System.currentTimeMillis() - pausedElapsed
        lastBreakTime = sessionStartTime!!
        _isRunning.value = true
        startTick()
        startBreakInterval()
    }

    fun stop(): Long {
        val currentElapsed = if (sessionStartTime != null) {
            System.currentTimeMillis() - sessionStartTime!!
        } else {
            pausedElapsed
        }
        pausedElapsed = currentElapsed
        _isRunning.value = false
        _nextBreakIn.value = null
        stopTick()
        stopBreakInterval()
        return currentElapsed
    }

    fun pause() {
        if (!_isRunning.value) return
        pausedElapsed = sessionStartTime?.let { System.currentTimeMillis() - it } ?: pausedElapsed
        _isRunning.value = false
        _nextBreakIn.value = null
        stopTick()
        stopBreakInterval()
    }

    fun resumeAfterBreak() {
        if (_isRunning.value) return
        sessionStartTime = System.currentTimeMillis() - pausedElapsed
        lastBreakTime = sessionStartTime!!
        _isRunning.value = true
        startTick()
        startBreakInterval()
    }

    fun reset() {
        stop()
        _elapsed.value = 0
        pausedElapsed = 0
        sessionStartTime = null
        lastBreakTime = 0
        _breakCountdown.value = 0
        _isBreakActive.value = false
    }

    private fun startTick() {
        tickJob = viewModelScope.launch {
            while (isActive && _isRunning.value) {
                sessionStartTime?.let {
                    _elapsed.value = System.currentTimeMillis() - it
                }
                delay(100)
            }
        }
    }

    private fun stopTick() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun startBreakInterval() {
        intervalJob = viewModelScope.launch {
            while (isActive && _isRunning.value) {
                val elapsedMs = sessionStartTime?.let { System.currentTimeMillis() - it } ?: 0L
                val sinceLastBreak = elapsedMs - lastBreakTime
                val remaining = BREAK_INTERVAL_MS - sinceLastBreak

                if (remaining <= 0 && !_isBreakActive.value) {
                    onBreakTrigger?.invoke()
                } else if (!_isBreakActive.value) {
                    _nextBreakIn.value = (remaining / 1000).toInt()
                }
                delay(500)
            }
        }
    }

    private fun stopBreakInterval() {
        intervalJob?.cancel()
        intervalJob = null
    }

    fun startBreakCountdown() {
        _isBreakActive.value = true
        _breakCountdown.value = 20

        viewModelScope.launch {
            while (_breakCountdown.value > 0) {
                delay(1000)
                _breakCountdown.value = _breakCountdown.value - 1
            }
        }
    }

    fun endBreakCountdown() {
        _isBreakActive.value = false
        _breakCountdown.value = 0
        lastBreakTime = sessionStartTime?.let { System.currentTimeMillis() - it } ?: 0L
    }

    fun onAppBackground() {
    }

    fun onAppForeground() {
    }

    fun cleanup() {
        tickJob?.cancel()
        intervalJob?.cancel()
    }

    companion object {
        private const val BREAK_INTERVAL_MS = 20 * 60 * 1000L
    }
}
