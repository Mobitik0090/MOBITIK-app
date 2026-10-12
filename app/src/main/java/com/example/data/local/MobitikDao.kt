package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppConfig
import com.example.data.model.AuditLog
import com.example.data.model.FaqItem
import com.example.data.model.InAppNotification
import com.example.data.model.MiningSession
import com.example.data.model.SupportTicket
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE referralCode = :referralCode LIMIT 1")
    suspend fun getUserByReferralCode(referralCode: String): User?

    @Query("SELECT * FROM users WHERE walletAddress = :address LIMIT 1")
    suspend fun getUserByWallet(address: String): User?

    @Query("SELECT * FROM users ORDER BY registeredAt DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY registeredAt DESC")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE referredBy = :referralCode ORDER BY registeredAt DESC")
    fun getReferredUsersFlow(referralCode: String): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET trustWalletAddress = :address WHERE id = :userId")
    suspend fun updateTrustWalletAddress(userId: String, address: String?)

    @Query("UPDATE users SET isAutoWithdrawalEnabled = :enabled, autoWithdrawalThreshold = :threshold WHERE id = :userId")
    suspend fun updateAutoWithdrawalSettings(userId: String, enabled: Boolean, threshold: Double)

    @Query("UPDATE users SET availableBalance = availableBalance - :amount, totalBalance = totalBalance - :amount, lastAutoWithdrawalMillis = :now WHERE id = :userId")
    suspend fun executeAutoWithdrawalDeduction(userId: String, amount: Double, now: Long)
}

@Dao
interface MiningDao {
    @Query("SELECT * FROM mining_sessions WHERE userId = :userId AND status IN ('ACTIVE', 'COMPLETED') ORDER BY startTimeMillis DESC LIMIT 1")
    fun observeActiveSession(userId: String): Flow<MiningSession?>

    @Query("SELECT * FROM mining_sessions WHERE userId = :userId AND status IN ('ACTIVE', 'COMPLETED') ORDER BY startTimeMillis DESC LIMIT 1")
    suspend fun getActiveSession(userId: String): MiningSession?

    @Query("SELECT * FROM mining_sessions WHERE userId = :userId ORDER BY startTimeMillis DESC")
    fun observeUserSessions(userId: String): Flow<List<MiningSession>>

    @Query("SELECT * FROM mining_sessions WHERE userId = :userId ORDER BY startTimeMillis DESC")
    suspend fun getUserSessions(userId: String): List<MiningSession>

    @Query("SELECT COUNT(*) FROM mining_sessions WHERE status = 'ACTIVE'")
    suspend fun getActiveMiningCount(): Int

    @Query("SELECT COUNT(*) FROM mining_sessions WHERE status = 'CLAIMED'")
    suspend fun getCompletedMiningCount(): Int

    @Query("SELECT TOTAL(rewardAmount) FROM mining_sessions WHERE status = 'CLAIMED'")
    suspend fun getTotalRewardsDistributed(): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MiningSession)

    @Update
    suspend fun updateSession(session: MiningSession)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeUserTransactions(userId: String): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserTransactions(userId: String): List<WalletTransaction>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<WalletTransaction>>

    @Query("SELECT COUNT(*) FROM wallet_transactions")
    suspend fun getTotalTransactionsCount(): Int

    @Query("SELECT COUNT(*) FROM wallet_transactions WHERE status = 'PENDING'")
    suspend fun getPendingTransactionsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserTickets(userId: String): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun observeAllTickets(): Flow<List<SupportTicket>>

    @Query("SELECT COUNT(*) FROM support_tickets WHERE status = 'OPEN'")
    suspend fun getOpenTicketsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket)

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): SupportTicket?

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    suspend fun getAllTickets(): List<SupportTicket>

    @Update
    suspend fun updateTicket(ticket: SupportTicket)
}

@Dao
interface FaqDao {
    @Query("SELECT * FROM faq_items ORDER BY orderIndex ASC")
    fun observeAllFaqs(): Flow<List<FaqItem>>

    @Query("SELECT * FROM faq_items ORDER BY orderIndex ASC")
    suspend fun getAllFaqs(): List<FaqItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaqs(items: List<FaqItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(item: FaqItem)

    @Query("DELETE FROM faq_items WHERE id = :id")
    suspend fun deleteFaq(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeUserNotifications(userId: String): Flow<List<InAppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun observeUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: InAppNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun observeAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Query("SELECT configValue FROM app_configs WHERE configKey = :key LIMIT 1")
    suspend fun getConfig(key: String): String?

    @Query("SELECT * FROM app_configs")
    fun observeAllConfigs(): Flow<List<AppConfig>>

    @Query("SELECT * FROM app_configs")
    suspend fun getAllConfigs(): List<AppConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: AppConfig)
}
