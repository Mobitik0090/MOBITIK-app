package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mining_sessions")
data class MiningSession(
    @PrimaryKey
    val id: String, // UUID
    val userId: String,
    val sessionNumber: Int,
    val startTimeMillis: Long,
    val expectedEndTimeMillis: Long,
    val completedTimeMillis: Long? = null,
    val rewardAmount: Double = 0.22,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED" (ready to claim), "CLAIMED", "EXPIRED"
    val isAutoRestart: Boolean = false,
    val miningRatePerHour: Double = 0.01833 // 0.22 / 12h
)
