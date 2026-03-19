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

    private var startTime: Long? = null
    private var pausedTime: Long = 0
    private var tickJob: Job? = null

    private val _elapsed = MutableStateFlow(0L)
    val elapsed: StateFlow<Long> = _elapsed.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var lastBreakTime: Long = 0
    private var intervalJob: Job? = null
    private var hiddenAt: Long? = null

    private val _nextBreakIn = MutableStateFlow<Int?>(null)
    val nextBreakIn: StateFlow<Int?> = _nextBreakIn.asStateFlow()

    private val _breakCountdown = MutableStateFlow(20)
    val breakCountdown: StateFlow<Int> = _breakCountdown.asStateFlow()

    private val _isBreakActive = MutableStateFlow(false)
    val isBreakActive: StateFlow<Boolean> = _isBreakActive.asStateFlow()

    private var onBreakTrigger: (() -> Unit)? = null

    fun setBreakTriggerListener(listener: () -> Unit) {
        onBreakTrigger = listener
    }

    fun start() {
        if (_isRunning.value) return
        startTime = System.currentTimeMillis() - pausedTime
        _isRunning.value = true

        lastBreakTime = System.currentTimeMillis()
        startTick()
        startBreakInterval()
    }

    fun stop(): Long {
        val elapsed = if (startTime != null) {
            System.currentTimeMillis() - startTime!!
        } else {
            pausedTime
        }
        pausedTime = elapsed
        _isRunning.value = false
        _nextBreakIn.value = null
        stopTick()
        stopBreakInterval()
        return elapsed
    }

    fun reset() {
        stop()
        _elapsed.value = 0
        pausedTime = 0
        startTime = null
        _breakCountdown.value = 20
        _isBreakActive.value = false
    }

    private fun startTick() {
        tickJob = viewModelScope.launch {
            while (isActive && _isRunning.value) {
                startTime?.let {
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
                val now = System.currentTimeMillis()
                val since = now - lastBreakTime
                val remaining = (BREAK_INTERVAL_MS - since) / 1000

                if (remaining <= 0 && !_isBreakActive.value) {
                    triggerBreak()
                } else {
                    _nextBreakIn.value = remaining.toInt().coerceAtLeast(0)
                }
                delay(500)
            }
        }
    }

    private fun stopBreakInterval() {
        intervalJob?.cancel()
        intervalJob = null
    }

    private fun triggerBreak() {
        _isBreakActive.value = true
        _breakCountdown.value = 20
        onBreakTrigger?.invoke()

        viewModelScope.launch {
            while (_breakCountdown.value > 0 && _isBreakActive.value) {
                delay(1000)
                _breakCountdown.value = _breakCountdown.value - 1
            }
        }
    }

    fun endBreak() {
        _isBreakActive.value = false
        _breakCountdown.value = 20
        lastBreakTime = System.currentTimeMillis()
        _nextBreakIn.value = null
    }

    fun onAppBackground() {
        if (_isRunning.value && !_isBreakActive.value) {
            hiddenAt = System.currentTimeMillis()
            stopBreakInterval()
        }
    }

    fun onAppForeground() {
        hiddenAt?.let { hidden ->
            lastBreakTime += System.currentTimeMillis() - hidden
            hiddenAt = null
        }
        if (_isRunning.value && !_isBreakActive.value) {
            startBreakInterval()
        }
    }

    fun cleanup() {
        tickJob?.cancel()
        intervalJob?.cancel()
    }

    companion object {
        private const val BREAK_INTERVAL_MS = 20 * 60 * 1000L
    }
}
