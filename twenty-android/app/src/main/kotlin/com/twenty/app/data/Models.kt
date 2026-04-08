package com.twenty.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val profileId: String = "default",
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val breaksTriggered: Int,
    val breaksTaken: Int,
    val breaksSkipped: Int,
    val complianceRate: Float
)

@Serializable
data class Settings(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val volume: Float = 0.7f,
    val onboardingComplete: Boolean = false,
    val activeProfileId: String = "default"
)
