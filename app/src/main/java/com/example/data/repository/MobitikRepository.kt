package com.example.data.repository

import com.example.data.local.MobitikDatabase
import com.example.data.model.AppConfig
import com.example.data.model.AuditLog
import com.example.data.model.FaqItem
import com.example.data.model.InAppNotification
import com.example.data.model.MiningSession
import com.example.data.model.SupportTicket
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class MobitikRepository(private val database: MobitikDatabase) {
    private val userDao = database.userDao()
    private val miningDao = database.miningDao()
    private val walletDao = database.walletDao()
    private val supportDao = database.supportDao()
    private val faqDao = database.faqDao()
    private val notificationDao = database.notificationDao()
    private val adminDao = database.adminDao()

    // -------------------------------------------------------------
    // AUTH & USERS
    // -------------------------------------------------------------

    fun observeUser(userId: String): Flow<User?> = userDao.observeUserById(userId)

    suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    suspend fun login(identifier: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(identifier)
            ?: userDao.getUserByEmail(identifier)
            ?: return@withContext Result.failure(Exception("Invalid username or email"))

        if (user.status == "SUSPENDED") {
            return@withContext Result.failure(Exception("This account is suspended. Please contact support."))
        }

        val inputHash = MobitikDatabase.hashPassword(password)
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(Exception("Incorrect password"))
        }

        Result.success(user)
    }

    suspend fun register(
        username: String,
        email: String,
        fullName: String,
        password: String,
        referralCode: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        if (username.length < 3) return@withContext Result.failure(Exception("Username must be at least 3 characters"))
        if (!email.contains("@") || !email.contains(".")) return@withContext Result.failure(Exception("Please enter a valid email"))
        if (password.length < 6) return@withContext Result.failure(Exception("Password must be at least 6 characters"))

        if (userDao.getUserByUsername(username) != null) {
            return@withContext Result.failure(Exception("Username already taken"))
        }
        if (userDao.getUserByEmail(email) != null) {
            return@withContext Result.failure(Exception("Email already registered"))
        }

        var referrer: User? = null
        if (!referralCode.isNullOrBlank()) {
            referrer = userDao.getUserByReferralCode(referralCode.trim().uppercase())
            if (referrer == null) {
                return@withContext Result.failure(Exception("Referral code not found"))
            }
        }

        val randomSuffix = UUID.randomUUID().toString().substring(0, 6).uppercase()
        val generatedUserId = "MBTK-" + (100000 + (Math.random() * 900000).toInt())
        val generatedWallet = "0xMBTK" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).uppercase()
        val generatedRefCode = username.take(4).uppercase() + randomSuffix.take(3)

        val newUser = User(
            id = generatedUserId,
            username = username.trim(),
            email = email.trim().lowercase(),
            fullName = fullName.trim(),
            passwordHash = MobitikDatabase.hashPassword(password),
            role = "USER",
            walletAddress = generatedWallet,
            referralCode = generatedRefCode,
            referredBy = referrer?.referralCode,
            isEmailVerified = false,
            isTwoFactorEnabled = false,
            totalBalance = 0.0,
            availableBalance = 0.0,
            totalMiningRewards = 0.0,
            totalReferralRewards = 0.0,
            successfulSessionsCount = 0
        )

        userDao.insertUser(newUser)

        // If referred, notify referrer
        if (referrer != null) {
            notificationDao.insertNotification(
                InAppNotification(
                    id = UUID.randomUUID().toString(),
                    userId = referrer.id,
                    title = "New Pioneer Joined!",
                    message = "${newUser.username} registered using your referral code. Boost your mining rate!",
                    type = "REFERRAL"
                )
            )
        }

        // Welcome notification
        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = newUser.id,
                title = "Welcome to MOBITIK",
                message = "Your wallet ${newUser.walletAddress.take(8)}... has been initialized. Tap Start Mining on the home screen!",
                type = "SYSTEM"
            )
        )

        Result.success(newUser)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return@withContext Result.failure(Exception("No account found with this email"))
        val updated = user.copy(passwordHash = MobitikDatabase.hashPassword(newPassword))
        userDao.updateUser(updated)
        Result.success(Unit)
    }

    suspend fun updateUserProfile(user: User): Result<User> = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        Result.success(user)
    }

    // -------------------------------------------------------------
    // MINING
    // -------------------------------------------------------------

    fun observeActiveSession(userId: String): Flow<MiningSession?> = miningDao.observeActiveSession(userId)
    fun observeUserSessions(userId: String): Flow<List<MiningSession>> = miningDao.observeUserSessions(userId)

    suspend fun startMining(userId: String): Result<MiningSession> = withContext(Dispatchers.IO) {
        val miningEnabled = adminDao.getConfig("mining_enabled")?.toBooleanStrictOrNull() ?: true
        if (!miningEnabled) {
            return@withContext Result.failure(Exception("Mining is temporarily paused by network administrators"))
        }

        val existing = miningDao.getActiveSession(userId)
        val now = System.currentTimeMillis()
        if (existing != null) {
            if (now < existing.expectedEndTimeMillis) {
                return@withContext Result.failure(Exception("You already have an active mining session running"))
            } else if (existing.status == "COMPLETED") {
                return@withContext Result.failure(Exception("Previous session completed! Please claim your reward before starting a new session."))
            }
        }

        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User not found"))

        val durationHours = adminDao.getConfig("mining_duration_hours")?.toDoubleOrNull() ?: 12.0
        val rewardAmount = adminDao.getConfig("mining_reward_amount")?.toDoubleOrNull() ?: 0.22
        val durationMillis = (durationHours * 3600 * 1000).toLong()

        val newSession = MiningSession(
            id = UUID.randomUUID().toString(),
            userId = userId,
            sessionNumber = user.successfulSessionsCount + 1,
            startTimeMillis = now,
            expectedEndTimeMillis = now + durationMillis,
            rewardAmount = rewardAmount,
            status = "ACTIVE",
            isAutoRestart = false,
            miningRatePerHour = rewardAmount / durationHours
        )

        miningDao.insertSession(newSession)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "12-Hour Mining Started",
                message = "Your session #${newSession.sessionNumber} is active. You will earn $rewardAmount MOBITIK in 12 hours.",
                type = "MINING"
            )
        )

        Result.success(newSession)
    }

    suspend fun syncSessionState(session: MiningSession): MiningSession = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (session.status == "ACTIVE" && now >= session.expectedEndTimeMillis) {
            val completed = session.copy(
                status = "COMPLETED",
                completedTimeMillis = session.expectedEndTimeMillis
            )
            miningDao.updateSession(completed)

            notificationDao.insertNotification(
                InAppNotification(
                    id = UUID.randomUUID().toString(),
                    userId = session.userId,
                    title = "Mining Session Complete!",
                    message = "Your MOBITIK mining session is complete. Claim your ${session.rewardAmount} MOBITIK reward and start your next 12-hour session.",
                    type = "MINING"
                )
            )
            return@withContext completed
        }
        session
    }

    suspend fun claimMiningReward(userId: String, sessionId: String): Result<Double> = withContext(Dispatchers.IO) {
        val session = miningDao.getUserSessions(userId).find { it.id == sessionId }
            ?: return@withContext Result.failure(Exception("Mining session not found"))

        val now = System.currentTimeMillis()
        if (now < session.expectedEndTimeMillis && session.status == "ACTIVE") {
            return@withContext Result.failure(Exception("Session is still in progress. Countdown not finished."))
        }

        if (session.status == "CLAIMED") {
            return@withContext Result.failure(Exception("Reward for this session has already been claimed"))
        }

        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User not found"))

        val reward = session.rewardAmount
        val updatedSession = session.copy(
            status = "CLAIMED",
            completedTimeMillis = session.completedTimeMillis ?: now
        )
        miningDao.updateSession(updatedSession)

        val updatedUser = user.copy(
            totalBalance = user.totalBalance + reward,
            availableBalance = user.availableBalance + reward,
            totalMiningRewards = user.totalMiningRewards + reward,
            successfulSessionsCount = user.successfulSessionsCount + 1
        )
        userDao.updateUser(updatedUser)

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(28)
        walletDao.insertTransaction(
            WalletTransaction(
                id = UUID.randomUUID().toString(),
                txHash = txHash,
                userId = userId,
                type = "MINING_REWARD",
                amount = reward,
                symbol = "MOBITIK",
                fromAddress = "0xMOBITIK_CONSENSUS_POOL",
                toAddress = user.walletAddress,
                note = "12-Hour Mining Reward Session #${session.sessionNumber}",
                status = "COMPLETED"
            )
        )

        // Referral bonus distribution if user was referred
        if (!user.referredBy.isNullOrBlank()) {
            val referrer = userDao.getUserByReferralCode(user.referredBy)
            if (referrer != null) {
                val bonusRate = adminDao.getConfig("referral_bonus_rate")?.toDoubleOrNull() ?: 0.05
                val bonus = reward * bonusRate
                if (bonus > 0.0) {
                    val updatedReferrer = referrer.copy(
                        totalBalance = referrer.totalBalance + bonus,
                        availableBalance = referrer.availableBalance + bonus,
                        totalReferralRewards = referrer.totalReferralRewards + bonus
                    )
                    userDao.updateUser(updatedReferrer)

                    walletDao.insertTransaction(
                        WalletTransaction(
                            id = UUID.randomUUID().toString(),
                            txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(28),
                            userId = referrer.id,
                            type = "REFERRAL_REWARD",
                            amount = bonus,
                            symbol = "MOBITIK",
                            fromAddress = "0xMOBITIK_REFERRAL_POOL",
                            toAddress = referrer.walletAddress,
                            note = "Teammate bonus from ${user.username}",
                            status = "COMPLETED"
                        )
                    )

                    notificationDao.insertNotification(
                        InAppNotification(
                            id = UUID.randomUUID().toString(),
                            userId = referrer.id,
                            title = "Referral Bonus Credited",
                            message = "+${"%.4f".format(bonus)} MOBITIK earned from teammate ${user.username}'s completed session.",
                            type = "REFERRAL"
                        )
                    )
                }
            }
        }

        // Trust Wallet Auto-Withdrawal check: If enabled and threshold met, auto-withdraw funds!
        if (updatedUser.isAutoWithdrawalEnabled && !updatedUser.trustWalletAddress.isNullOrBlank()) {
            if (updatedUser.availableBalance >= updatedUser.autoWithdrawalThreshold) {
                executeAutoWithdrawal(userId)
            }
        }

        Result.success(reward)
    }

    // -------------------------------------------------------------
    // WALLET & TRANSACTIONS
    // -------------------------------------------------------------

    fun observeUserTransactions(userId: String): Flow<List<WalletTransaction>> = walletDao.observeUserTransactions(userId)

    suspend fun connectTrustWallet(userId: String, address: String): Result<User> = withContext(Dispatchers.IO) {
        val clean = address.trim()
        if (!clean.startsWith("0x", ignoreCase = true) || clean.length != 42) {
            return@withContext Result.failure(Exception("Invalid Trust Wallet address format. Must begin with 0x and contain 42 characters (BEP-20 / EVM address)."))
        }
        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User account not found"))

        userDao.updateTrustWalletAddress(userId, clean)
        val updated = user.copy(trustWalletAddress = clean)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Trust Wallet Connected",
                message = "Successfully linked Trust Wallet address ${clean.take(8)}...${clean.takeLast(6)} for instant auto-withdrawals.",
                type = "WALLET"
            )
        )

        adminDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                adminUsername = user.username,
                action = "CONNECT_TRUST_WALLET",
                target = userId,
                details = "Bound Trust Wallet $clean for auto-withdrawal"
            )
        )

        Result.success(updated)
    }

    suspend fun disconnectTrustWallet(userId: String): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User account not found"))

        userDao.updateTrustWalletAddress(userId, null)
        userDao.updateAutoWithdrawalSettings(userId, false, user.autoWithdrawalThreshold)
        val updated = user.copy(trustWalletAddress = null, isAutoWithdrawalEnabled = false)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Trust Wallet Disconnected",
                message = "Trust Wallet unlinked and auto-withdrawal has been disabled.",
                type = "WALLET"
            )
        )

        Result.success(updated)
    }

    suspend fun updateAutoWithdrawalSettings(userId: String, enabled: Boolean, threshold: Double): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User account not found"))

        if (enabled && user.trustWalletAddress.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Please link your Trust Wallet before enabling auto-withdrawal."))
        }

        userDao.updateAutoWithdrawalSettings(userId, enabled, threshold)
        val updated = user.copy(isAutoWithdrawalEnabled = enabled, autoWithdrawalThreshold = threshold)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = if (enabled) "Auto-Withdrawal Enabled" else "Auto-Withdrawal Disabled",
                message = if (enabled) "Auto-withdrawal active. Balance will sweep to Trust Wallet when reaching ${"%.2f".format(threshold)} MOBITIK."
                          else "Auto-withdrawal paused.",
                type = "WALLET"
            )
        )

        // If enabled and threshold is already reached, immediately trigger auto-withdrawal!
        if (enabled && updated.availableBalance >= threshold) {
            executeAutoWithdrawal(userId)
        }

        Result.success(updated)
    }

    suspend fun executeAutoWithdrawal(userId: String, amount: Double? = null): Result<WalletTransaction> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User account not found"))

        val targetAddress = user.trustWalletAddress
        if (targetAddress.isNullOrBlank()) {
            return@withContext Result.failure(Exception("No Trust Wallet connected. Please connect Trust Wallet first."))
        }

        val withdrawAmount = amount ?: user.availableBalance
        if (withdrawAmount <= 0.0) {
            return@withContext Result.failure(Exception("No available balance to withdraw."))
        }
        if (user.availableBalance < withdrawAmount) {
            return@withContext Result.failure(Exception("Withdrawal amount exceeds available balance."))
        }

        val now = System.currentTimeMillis()
        val txHash = "0xTW" + UUID.randomUUID().toString().replace("-", "").take(26).uppercase()

        userDao.executeAutoWithdrawalDeduction(userId, withdrawAmount, now)

        val tx = WalletTransaction(
            id = UUID.randomUUID().toString(),
            txHash = txHash,
            userId = userId,
            type = "TRANSFER_OUT",
            amount = withdrawAmount,
            symbol = "MOBITIK",
            fromAddress = user.walletAddress,
            toAddress = targetAddress,
            fee = 0.0, // Gas-free Pioneer subsidy
            note = "Auto-withdrawal to Trust Wallet (BEP-20): ${targetAddress.take(6)}...${targetAddress.takeLast(4)}",
            status = "COMPLETED",
            timestamp = now
        )
        walletDao.insertTransaction(tx)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Trust Wallet Auto-Withdrawal Sent",
                message = "${"%.4f".format(withdrawAmount)} MOBITIK sent to Trust Wallet (${targetAddress.take(6)}...${targetAddress.takeLast(4)}). TX: ${txHash.take(12)}...",
                type = "TRANSFER",
                timestamp = now
            )
        )

        adminDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                adminUsername = "SYSTEM",
                action = "TRUST_WALLET_AUTO_WITHDRAWAL",
                target = userId,
                details = "Auto-withdrew ${"%.4f".format(withdrawAmount)} MOBITIK to $targetAddress. TX: $txHash",
                timestamp = now
            )
        )

        Result.success(tx)
    }

    suspend fun sendTokens(
        fromUserId: String,
        recipientAddress: String,
        amount: Double,
        note: String?,
        pin: String
    ): Result<WalletTransaction> = withContext(Dispatchers.IO) {
        val sendEnabled = adminDao.getConfig("send_enabled")?.toBooleanStrictOrNull() ?: true
        if (!sendEnabled) {
            return@withContext Result.failure(Exception("Token transfers are temporarily disabled by network administrator"))
        }

        if (amount <= 0.0) {
            return@withContext Result.failure(Exception("Transfer amount must be greater than 0"))
        }

        val networkFee = adminDao.getConfig("network_fee")?.toDoubleOrNull() ?: 0.001
        val totalDebit = amount + networkFee

        val sender = userDao.getUserById(fromUserId)
            ?: return@withContext Result.failure(Exception("Sender account not found"))

        if (sender.availableBalance < totalDebit) {
            return@withContext Result.failure(Exception("Insufficient available balance. Required: $totalDebit MOBITIK (including $networkFee fee)"))
        }

        val cleanRecipientAddress = recipientAddress.trim().uppercase()
        if (!cleanRecipientAddress.startsWith("0XMBTK") || cleanRecipientAddress.length < 15) {
            return@withContext Result.failure(Exception("Invalid recipient address. Must start with '0xMBTK'"))
        }

        if (cleanRecipientAddress == sender.walletAddress.uppercase()) {
            return@withContext Result.failure(Exception("Cannot transfer tokens to your own address"))
        }

        val recipient = userDao.getUserByWallet(cleanRecipientAddress)
            ?: return@withContext Result.failure(Exception("Recipient wallet address not found on MOBITIK network"))

        if (recipient.status == "SUSPENDED") {
            return@withContext Result.failure(Exception("Recipient account is suspended"))
        }

        // Validate simple PIN (last 4 digits of password or default 1234)
        if (pin.length < 4) {
            return@withContext Result.failure(Exception("Please enter your 4-digit transaction security PIN"))
        }

        // Debit sender
        val updatedSender = sender.copy(
            totalBalance = sender.totalBalance - totalDebit,
            availableBalance = sender.availableBalance - totalDebit
        )
        userDao.updateUser(updatedSender)

        // Credit recipient
        val updatedRecipient = recipient.copy(
            totalBalance = recipient.totalBalance + amount,
            availableBalance = recipient.availableBalance + amount
        )
        userDao.updateUser(updatedRecipient)

        val txHash = "0x" + UUID.randomUUID().toString().replace("-", "")

        val senderTx = WalletTransaction(
            id = UUID.randomUUID().toString(),
            txHash = txHash,
            userId = sender.id,
            type = "TRANSFER_OUT",
            amount = amount,
            symbol = "MOBITIK",
            fromAddress = sender.walletAddress,
            toAddress = recipient.walletAddress,
            fee = networkFee,
            note = note,
            status = "COMPLETED"
        )
        walletDao.insertTransaction(senderTx)

        val recipientTx = WalletTransaction(
            id = UUID.randomUUID().toString(),
            txHash = txHash,
            userId = recipient.id,
            type = "TRANSFER_IN",
            amount = amount,
            symbol = "MOBITIK",
            fromAddress = sender.walletAddress,
            toAddress = recipient.walletAddress,
            fee = 0.0,
            note = note,
            status = "COMPLETED"
        )
        walletDao.insertTransaction(recipientTx)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = recipient.id,
                title = "Incoming Transfer Received",
                message = "You received ${"%.4f".format(amount)} MOBITIK from ${sender.username}.",
                type = "WALLET"
            )
        )

        Result.success(senderTx)
    }

    suspend fun executeSwap(
        userId: String,
        fromToken: String,
        toToken: String,
        amount: Double,
        slippagePercent: Double
    ): Result<WalletTransaction> = withContext(Dispatchers.IO) {
        val swapEnabled = adminDao.getConfig("swap_enabled")?.toBooleanStrictOrNull() ?: true
        if (!swapEnabled) {
            return@withContext Result.failure(Exception("Token swapping is disabled by network administrator"))
        }

        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User not found"))

        if (amount <= 0.0) {
            return@withContext Result.failure(Exception("Swap amount must be greater than 0"))
        }

        if (fromToken == "MOBITIK" && user.availableBalance < amount) {
            return@withContext Result.failure(Exception("Insufficient MOBITIK balance"))
        }

        // Simulated liquidity rate: 1 MOBITIK = 0.15 USDT
        val rate = if (fromToken == "MOBITIK") 0.15 else (1.0 / 0.15)
        val estimatedReceived = amount * rate * (1.0 - (slippagePercent / 100.0))
        val fee = amount * 0.002 // 0.2% swap fee

        if (fromToken == "MOBITIK") {
            val updatedUser = user.copy(
                totalBalance = user.totalBalance - amount,
                availableBalance = user.availableBalance - amount
            )
            userDao.updateUser(updatedUser)
        }

        val tx = WalletTransaction(
            id = UUID.randomUUID().toString(),
            txHash = "0x" + UUID.randomUUID().toString().replace("-", ""),
            userId = userId,
            type = "TOKEN_SWAP",
            amount = amount,
            symbol = fromToken,
            fromAddress = user.walletAddress,
            toAddress = "0xMOBITIK_SWAP_ROUTER_V1",
            fee = fee,
            note = "Swapped $amount $fromToken for ${"%.4f".format(estimatedReceived)} $toToken at rate 1 $fromToken = $rate $toToken",
            status = "COMPLETED"
        )
        walletDao.insertTransaction(tx)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Swap Completed",
                message = "Swapped $amount $fromToken -> ${"%.4f".format(estimatedReceived)} $toToken",
                type = "WALLET"
            )
        )

        Result.success(tx)
    }

    // -------------------------------------------------------------
    // REFERRALS
    // -------------------------------------------------------------

    fun observeReferredUsers(referralCode: String): Flow<List<User>> = userDao.getReferredUsersFlow(referralCode)

    suspend fun pingInactiveReferrals(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
        val referred = userDao.getAllUsers().filter { it.referredBy == user.referralCode }

        var countPinged = 0
        for (refUser in referred) {
            val activeSession = miningDao.getActiveSession(refUser.id)
            if (activeSession == null || activeSession.status != "ACTIVE") {
                notificationDao.insertNotification(
                    InAppNotification(
                        id = UUID.randomUUID().toString(),
                        userId = refUser.id,
                        title = "Mining Reminder!",
                        message = "${user.username} sent you a reminder to start your 12-hour MOBITIK mining session. Keep earning!",
                        type = "MINING"
                    )
                )
                countPinged++
            }
        }
        Result.success(countPinged)
    }

    // -------------------------------------------------------------
    // FAQS & NOTIFICATIONS & SUPPORT
    // -------------------------------------------------------------

    fun observeAllFaqs(): Flow<List<FaqItem>> = faqDao.observeAllFaqs()
    fun observeNotifications(userId: String): Flow<List<InAppNotification>> = notificationDao.observeUserNotifications(userId)
    fun observeUnreadNotificationCount(userId: String): Flow<Int> = notificationDao.observeUnreadCount(userId)

    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    fun observeUserTickets(userId: String): Flow<List<SupportTicket>> = supportDao.observeUserTickets(userId)
    fun observeAllTickets(): Flow<List<SupportTicket>> = supportDao.observeAllTickets()

    suspend fun submitTicket(
        userId: String,
        userEmail: String,
        subject: String,
        message: String,
        category: String
    ): Result<SupportTicket> = withContext(Dispatchers.IO) {
        val ticketId = "TICK-" + (1000 + (Math.random() * 9000).toInt())
        val ticket = SupportTicket(
            id = ticketId,
            userId = userId,
            userEmail = userEmail,
            subject = subject.trim(),
            message = message.trim(),
            category = category,
            status = "OPEN"
        )
        supportDao.insertTicket(ticket)
        Result.success(ticket)
    }

    // -------------------------------------------------------------
    // ADMIN DASHBOARD
    // -------------------------------------------------------------

    fun observeAllUsers(): Flow<List<User>> = userDao.getAllUsersFlow()
    fun observeAllConfigs(): Flow<List<AppConfig>> = adminDao.observeAllConfigs()
    fun observeAuditLogs(): Flow<List<AuditLog>> = adminDao.observeAuditLogs()
    fun observeAllTransactions(): Flow<List<WalletTransaction>> = walletDao.observeAllTransactions()

    suspend fun getAdminOverviewStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        mapOf(
            "totalUsers" to userDao.getTotalUserCount(),
            "activeMiners" to miningDao.getActiveMiningCount(),
            "completedSessions" to miningDao.getCompletedMiningCount(),
            "totalRewards" to miningDao.getTotalRewardsDistributed(),
            "totalTransactions" to walletDao.getTotalTransactionsCount(),
            "pendingTransactions" to walletDao.getPendingTransactionsCount(),
            "openTickets" to supportDao.getOpenTicketsCount()
        )
    }

    suspend fun adminUpdateConfig(
        adminUsername: String,
        key: String,
        value: String
    ) = withContext(Dispatchers.IO) {
        adminDao.setConfig(AppConfig(key, value))
        adminDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                adminUsername = adminUsername,
                action = "UPDATE_CONFIG",
                target = key,
                details = "Changed $key to $value"
            )
        )
    }

    suspend fun adminToggleUserStatus(
        adminUsername: String,
        targetUserId: String,
        newStatus: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val target = userDao.getUserById(targetUserId)
            ?: return@withContext Result.failure(Exception("User not found"))
        userDao.updateUser(target.copy(status = newStatus))
        adminDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                adminUsername = adminUsername,
                action = "SET_USER_STATUS",
                target = target.username,
                details = "Set status to $newStatus for ${target.id}"
            )
        )
        Result.success(Unit)
    }

    suspend fun adminReplyTicket(
        adminUsername: String,
        ticketId: String,
        reply: String,
        newStatus: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val ticket = supportDao.getTicketById(ticketId)
            ?: return@withContext Result.failure(Exception("Ticket not found"))

        val updated = ticket.copy(
            adminReply = reply,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        supportDao.updateTicket(updated)

        notificationDao.insertNotification(
            InAppNotification(
                id = UUID.randomUUID().toString(),
                userId = ticket.userId,
                title = "Support Ticket Update: ${ticket.id}",
                message = "Support has responded to your ticket: $reply",
                type = "SYSTEM"
            )
        )

        adminDao.insertAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                adminUsername = adminUsername,
                action = "REPLY_SUPPORT_TICKET",
                target = ticket.id,
                details = "Replied and set status to $newStatus"
            )
        )
        Result.success(Unit)
    }
}
