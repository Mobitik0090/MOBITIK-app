package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey
    val id: String, // UUID
    val txHash: String, // e.g. "0x9f4a...e12c"
    val userId: String,
    val type: String, // "MINING_REWARD", "REFERRAL_REWARD", "TRANSFER_IN", "TRANSFER_OUT", "TOKEN_SWAP", "ADMIN_ADJUSTMENT"
    val amount: Double,
    val symbol: String = "MOBITIK",
    val fromAddress: String? = null,
    val toAddress: String? = null,
    val fee: Double = 0.0,
    val note: String? = null,
    val status: String = "COMPLETED", // "COMPLETED", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)
