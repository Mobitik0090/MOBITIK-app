package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey
    val id: String, // e.g. "TICK-4829"
    val userId: String,
    val userEmail: String,
    val subject: String,
    val message: String,
    val category: String = "General",
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "RESOLVED"
    val adminReply: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
