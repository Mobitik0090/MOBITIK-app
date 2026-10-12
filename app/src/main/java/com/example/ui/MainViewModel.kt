package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MobitikDatabase
import com.example.data.model.AppConfig
import com.example.data.model.AuditLog
import com.example.data.model.FaqItem
import com.example.data.model.InAppNotification
import com.example.data.model.MiningSession
import com.example.data.model.SupportTicket
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import com.example.data.repository.MobitikRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MobitikDatabase.getDatabase(application)
    val repository = MobitikRepository(database)

    // Current logged in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Active mining session
    private val _activeSession = MutableStateFlow<MiningSession?>(null)
    val activeSession: StateFlow<MiningSession?> = _activeSession.asStateFlow()

    // Real-time remaining countdown in millis
    private val _remainingTimeMillis = MutableStateFlow<Long>(0L)
    val remainingTimeMillis: StateFlow<Long> = _remainingTimeMillis.asStateFlow()

    // UI state
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Wallet & History
    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _miningHistory = MutableStateFlow<List<MiningSession>>(emptyList())
    val miningHistory: StateFlow<List<MiningSession>> = _miningHistory.asStateFlow()

    private val _referredUsers = MutableStateFlow<List<User>>(emptyList())
    val referredUsers: StateFlow<List<User>> = _referredUsers.asStateFlow()

    // Notifications & FAQs & Tickets
    val faqs: StateFlow<List<FaqItem>> = repository.observeAllFaqs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    private val _unreadNotifCount = MutableStateFlow(0)
    val unreadNotifCount: StateFlow<Int> = _unreadNotifCount.asStateFlow()

    private val _userTickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val userTickets: StateFlow<List<SupportTicket>> = _userTickets.asStateFlow()

    // Admin flows
    val adminAllUsers: StateFlow<List<User>> = repository.observeAllUsers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val adminConfigs: StateFlow<List<AppConfig>> = repository.observeAllConfigs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val adminAuditLogs: StateFlow<List<AuditLog>> = repository.observeAuditLogs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val adminAllTickets: StateFlow<List<SupportTicket>> = repository.observeAllTickets().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _adminOverviewStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val adminOverviewStats: StateFlow<Map<String, Any>> = _adminOverviewStats.asStateFlow()

    private var timerJob: Job? = null
    private var userObserverJobs = mutableListOf<Job>()

    init {
        // Auto login default pioneer initially or seed check
        viewModelScope.launch {
            // Check if seeded
            val pioneer = repository.getUser("MBTK-784291")
            if (pioneer != null) {
                setCurrentUser(pioneer)
            } else {
                // If database hasn't created yet, delay slightly and recheck
                delay(300)
                val retried = repository.getUser("MBTK-784291")
                if (retried != null) {
                    setCurrentUser(retried)
                }
            }
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        userObserverJobs.forEach { it.cancel() }
        userObserverJobs.clear()

        // Observe user updates (e.g. balance changes)
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeUser(user.id).collect { updated ->
                    if (updated != null) {
                        _currentUser.value = updated
                    }
                }
            }
        )

        // Observe active session
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeActiveSession(user.id).collect { session ->
                    _activeSession.value = session
                    startOrUpdateTimer(session)
                }
            }
        )

        // Observe user transactions
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeUserTransactions(user.id).collect { txs ->
                    _transactions.value = txs
                }
            }
        )

        // Observe mining history
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeUserSessions(user.id).collect { sessions ->
                    _miningHistory.value = sessions
                }
            }
        )

        // Observe referred users
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeReferredUsers(user.referralCode).collect { refs ->
                    _referredUsers.value = refs
                }
            }
        )

        // Observe notifications
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeNotifications(user.id).collect { notifs ->
                    _notifications.value = notifs
                }
            }
        )

        // Observe unread count
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeUnreadNotificationCount(user.id).collect { count ->
                    _unreadNotifCount.value = count
                }
            }
        )

        // Observe support tickets
        userObserverJobs.add(
            viewModelScope.launch {
                repository.observeUserTickets(user.id).collect { tickets ->
                    _userTickets.value = tickets
                }
            }
        )

        // If admin, load stats
        if (user.role == "ADMIN") {
            loadAdminStats()
        }
    }

    private fun startOrUpdateTimer(session: MiningSession?) {
        timerJob?.cancel()
        if (session == null || session.status == "CLAIMED" || session.status == "EXPIRED") {
            _remainingTimeMillis.value = 0L
            return
        }

        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = session.expectedEndTimeMillis - now
                if (remaining <= 0) {
                    _remainingTimeMillis.value = 0L
                    if (session.status == "ACTIVE") {
                        // Mark completed and ready to claim
                        val completed = repository.syncSessionState(session)
                        _activeSession.value = completed
                    }
                    break
                } else {
                    _remainingTimeMillis.value = remaining
                }
                delay(1000)
            }
        }
    }

    // -------------------------------------------------------------
    // AUTHENTICATION
    // -------------------------------------------------------------

    fun login(identifier: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(identifier, pass)
            _isLoading.value = false
            result.onSuccess { user ->
                setCurrentUser(user)
                showMessage("Welcome back, ${user.username}!")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Login failed")
            }
        }
    }

    fun register(
        username: String,
        email: String,
        fullName: String,
        pass: String,
        referralCode: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(username, email, fullName, pass, referralCode)
            _isLoading.value = false
            result.onSuccess { user ->
                setCurrentUser(user)
                showMessage("Account created! Welcome, ${user.username}!")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        timerJob?.cancel()
        userObserverJobs.forEach { it.cancel() }
        userObserverJobs.clear()
        _currentUser.value = null
        _activeSession.value = null
        _remainingTimeMillis.value = 0L
        showMessage("Logged out successfully")
    }

    fun switchAccount(toAdmin: Boolean) {
        viewModelScope.launch {
            val targetId = if (toAdmin) "MBTK-ADMIN01" else "MBTK-784291"
            val targetUser = repository.getUser(targetId)
            if (targetUser != null) {
                setCurrentUser(targetUser)
                showMessage("Switched to ${if (toAdmin) "Admin" else "Pioneer"} account")
            }
        }
    }

    fun resetPassword(email: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.resetPassword(email, newPass)
            _isLoading.value = false
            res.onSuccess {
                showMessage("Password updated successfully. You can now login.")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to reset password")
            }
        }
    }

    // -------------------------------------------------------------
    // MINING
    // -------------------------------------------------------------

    fun startMining() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.startMining(user.id)
            _isLoading.value = false
            res.onSuccess { session ->
                _activeSession.value = session
                startOrUpdateTimer(session)
                showMessage("Mining session #${session.sessionNumber} started! Earn ${session.rewardAmount} MOBITIK in 12 hours.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to start mining")
            }
        }
    }

    fun claimMiningReward() {
        val user = _currentUser.value ?: return
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.claimMiningReward(user.id, session.id)
            _isLoading.value = false
            res.onSuccess { reward ->
                showMessage("Claimed $reward MOBITIK! Added to your available balance.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to claim reward")
            }
        }
    }

    // -------------------------------------------------------------
    // WALLET & SWAP
    // -------------------------------------------------------------

    fun sendTokens(
        recipientAddress: String,
        amount: Double,
        note: String?,
        pin: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.sendTokens(user.id, recipientAddress, amount, note, pin)
            _isLoading.value = false
            res.onSuccess {
                showMessage("Sent ${"%.4f".format(amount)} MOBITIK successfully!")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Transfer failed")
            }
        }
    }

    fun executeSwap(
        fromToken: String,
        toToken: String,
        amount: Double,
        slippage: Double,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.executeSwap(user.id, fromToken, toToken, amount, slippage)
            _isLoading.value = false
            res.onSuccess {
                showMessage("Swap executed successfully!")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Swap failed")
            }
        }
    }

    // -------------------------------------------------------------
    // TRUST WALLET & AUTO-WITHDRAWAL
    // -------------------------------------------------------------

    fun connectTrustWallet(address: String, onSuccess: () -> Unit = {}) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.connectTrustWallet(user.id, address)
            _isLoading.value = false
            res.onSuccess { updated ->
                _currentUser.value = updated
                showMessage("Trust Wallet successfully connected!")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to connect Trust Wallet")
            }
        }
    }

    fun disconnectTrustWallet() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.disconnectTrustWallet(user.id)
            _isLoading.value = false
            res.onSuccess { updated ->
                _currentUser.value = updated
                showMessage("Trust Wallet unlinked.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to disconnect Trust Wallet")
            }
        }
    }

    fun updateAutoWithdrawalSettings(enabled: Boolean, threshold: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.updateAutoWithdrawalSettings(user.id, enabled, threshold)
            _isLoading.value = false
            res.onSuccess { updated ->
                _currentUser.value = updated
                showMessage(
                    if (enabled) "Auto-withdrawal activated! Minimum threshold: ${"%.2f".format(threshold)} MOBITIK"
                    else "Auto-withdrawal deactivated."
                )
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to update auto-withdrawal settings")
            }
        }
    }

    fun triggerAutoWithdrawalNow(amount: Double? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.executeAutoWithdrawal(user.id, amount)
            _isLoading.value = false
            res.onSuccess { tx ->
                showMessage("Auto-withdrawal of ${"%.4f".format(tx.amount)} MOBITIK sent to Trust Wallet!")
            }.onFailure { err ->
                showMessage(err.message ?: "Auto-withdrawal failed")
            }
        }
    }

    // -------------------------------------------------------------
    // REFERRAL
    // -------------------------------------------------------------

    fun pingInactiveReferrals() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.pingInactiveReferrals(user.id)
            res.onSuccess { count ->
                if (count > 0) {
                    showMessage("Pinged $count inactive pioneers in your team!")
                } else {
                    showMessage("All teammates are currently mining or team is empty.")
                }
            }.onFailure {
                showMessage("Unable to ping team")
            }
        }
    }

    // -------------------------------------------------------------
    // TICKETS & NOTIFICATIONS
    // -------------------------------------------------------------

    fun submitSupportTicket(subject: String, message: String, category: String, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.submitTicket(user.id, user.email, subject, message, category)
            _isLoading.value = false
            res.onSuccess { ticket ->
                showMessage("Ticket ${ticket.id} submitted! Support will respond shortly.")
                onSuccess()
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to submit ticket")
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
            showMessage("All notifications marked as read")
        }
    }

    // -------------------------------------------------------------
    // ADMIN ACTIONS
    // -------------------------------------------------------------

    fun loadAdminStats() {
        viewModelScope.launch {
            _adminOverviewStats.value = repository.getAdminOverviewStats()
        }
    }

    fun adminUpdateConfig(key: String, value: String) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.adminUpdateConfig(user.username, key, value)
            showMessage("Updated $key to $value")
            loadAdminStats()
        }
    }

    fun adminToggleUserStatus(targetUserId: String, newStatus: String) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.adminToggleUserStatus(user.username, targetUserId, newStatus)
            showMessage("User status updated to $newStatus")
            loadAdminStats()
        }
    }

    fun adminReplyTicket(ticketId: String, reply: String, newStatus: String) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.adminReplyTicket(user.username, ticketId, reply, newStatus)
            showMessage("Ticket $ticketId updated")
            loadAdminStats()
        }
    }
}
