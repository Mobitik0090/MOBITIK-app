package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faq_items")
data class FaqItem(
    @PrimaryKey
    val id: String,
    val question: String,
    val answer: String,
    val category: String = "General",
    val orderIndex: Int = 0
)

@Entity(tableName = "notifications")
data class InAppNotification(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "MINING", "WALLET", "REFERRAL", "SECURITY", "SYSTEM"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_configs")
data class AppConfig(
    @PrimaryKey
    val configKey: String,
    val configValue: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey
    val id: String,
    val adminUsername: String,
    val action: String,
    val target: String? = null,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
