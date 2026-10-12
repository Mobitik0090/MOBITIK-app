package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // e.g. "MBTK-784291"
    val username: String,
    val email: String,
    val fullName: String,
    val passwordHash: String,
    val role: String = "USER", // "USER" or "ADMIN"
    val walletAddress: String, // e.g. "0xMBTK7F129A883490B6154"
    val referralCode: String,
    val referredBy: String? = null,
    val isEmailVerified: Boolean = false,
    val isTwoFactorEnabled: Boolean = false,
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED"
    val totalBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val totalMiningRewards: Double = 0.0,
    val totalReferralRewards: Double = 0.0,
    val successfulSessionsCount: Int = 0,
    val registeredAt: Long = System.currentTimeMillis(),
    val trustWalletAddress: String? = null,
    val isAutoWithdrawalEnabled: Boolean = false,
    val autoWithdrawalThreshold: Double = 10.0,
    val lastAutoWithdrawalMillis: Long? = null
)
