package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppConfig
import com.example.data.model.AuditLog
import com.example.data.model.FaqItem
import com.example.data.model.InAppNotification
import com.example.data.model.MiningSession
import com.example.data.model.SupportTicket
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        User::class,
        MiningSession::class,
        WalletTransaction::class,
        SupportTicket::class,
        FaqItem::class,
        InAppNotification::class,
        AppConfig::class,
        AuditLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MobitikDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun miningDao(): MiningDao
    abstract fun walletDao(): WalletDao
    abstract fun supportDao(): SupportDao
    abstract fun faqDao(): FaqDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: MobitikDatabase? = null

        fun getDatabase(context: Context): MobitikDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MobitikDatabase::class.java,
                    "mobitik_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(db: MobitikDatabase) {
            val userDao = db.userDao()
            val faqDao = db.faqDao()
            val adminDao = db.adminDao()
            val walletDao = db.walletDao()
            val notificationDao = db.notificationDao()

            // 1. Seed Admin
            val adminUser = User(
                id = "MBTK-ADMIN01",
                username = "Admin",
                email = "admin@mobitik.network",
                fullName = "MOBITIK Network Admin",
                passwordHash = hashPassword("Admin@123"),
                role = "ADMIN",
                walletAddress = "0xMBTK888ADMIN777MASTER001",
                referralCode = "MOBITIKHQ",
                isEmailVerified = true,
                isTwoFactorEnabled = true,
                totalBalance = 100000.0,
                availableBalance = 100000.0,
                totalMiningRewards = 0.0,
                totalReferralRewards = 0.0,
                successfulSessionsCount = 0,
                trustWalletAddress = "0x1A2B3C4D5E6F7890123456789ABCDEF012345678",
                isAutoWithdrawalEnabled = false,
                autoWithdrawalThreshold = 25.0
            )
            userDao.insertUser(adminUser)

            // 2. Seed Default Regular User (Pioneer)
            val pioneer = User(
                id = "MBTK-784291",
                username = "CryptoPioneer",
                email = "pioneer@mobitik.network",
                fullName = "Alex Rivera",
                passwordHash = hashPassword("Pioneer@123"),
                role = "USER",
                walletAddress = "0xMBTK992F4A0182E37B6C89",
                referralCode = "PIONEER99",
                referredBy = "MOBITIKHQ",
                isEmailVerified = true,
                isTwoFactorEnabled = false,
                totalBalance = 4.84,
                availableBalance = 4.84,
                totalMiningRewards = 4.40,
                totalReferralRewards = 0.44,
                successfulSessionsCount = 20,
                trustWalletAddress = "0x712a8F9C34005b630eBe9472314Ac1271dB6A2C1",
                isAutoWithdrawalEnabled = true,
                autoWithdrawalThreshold = 10.0
            )
            userDao.insertUser(pioneer)

            // 3. Seed Teammate Pioneer to show referral team in action
            val teamMember1 = User(
                id = "MBTK-341902",
                username = "SarahMining",
                email = "sarah@mobitik.network",
                fullName = "Sarah Chen",
                passwordHash = hashPassword("User@123"),
                role = "USER",
                walletAddress = "0xMBTK772C59A1938B983D01",
                referralCode = "SARAH88",
                referredBy = "PIONEER99",
                isEmailVerified = true,
                totalBalance = 2.20,
                availableBalance = 2.20,
                totalMiningRewards = 2.20,
                totalReferralRewards = 0.0,
                successfulSessionsCount = 10
            )
            val teamMember2 = User(
                id = "MBTK-551029",
                username = "DavidCrypto",
                email = "david@mobitik.network",
                fullName = "David Miller",
                passwordHash = hashPassword("User@123"),
                role = "USER",
                walletAddress = "0xMBTK441D299E104A772C33",
                referralCode = "DAVID77",
                referredBy = "PIONEER99",
                isEmailVerified = true,
                totalBalance = 1.32,
                availableBalance = 1.32,
                totalMiningRewards = 1.32,
                totalReferralRewards = 0.0,
                successfulSessionsCount = 6
            )
            userDao.insertUser(teamMember1)
            userDao.insertUser(teamMember2)

            // 4. Seed Initial Transactions for Pioneer
            val now = System.currentTimeMillis()
            val day = 86400000L
            walletDao.insertTransaction(
                WalletTransaction(
                    id = "TX-INIT-01",
                    txHash = "0x9a3c8e71b2d049f57134aa89c67",
                    userId = pioneer.id,
                    type = "MINING_REWARD",
                    amount = 0.22,
                    symbol = "MOBITIK",
                    fromAddress = "0xMOBITIK_REWARD_POOL",
                    toAddress = pioneer.walletAddress,
                    note = "12-Hour Mining Reward Session #20",
                    status = "COMPLETED",
                    timestamp = now - (day / 4)
                )
            )
            walletDao.insertTransaction(
                WalletTransaction(
                    id = "TX-INIT-02",
                    txHash = "0x51b472e399c0d182741ef09a128",
                    userId = pioneer.id,
                    type = "REFERRAL_REWARD",
                    amount = 0.022,
                    symbol = "MOBITIK",
                    fromAddress = "0xMOBITIK_REWARD_POOL",
                    toAddress = pioneer.walletAddress,
                    note = "Referral Bonus from SarahMining",
                    status = "COMPLETED",
                    timestamp = now - (day / 2)
                )
            )
            walletDao.insertTransaction(
                WalletTransaction(
                    id = "TX-INIT-03",
                    txHash = "0x12d94a8bc3704fe90831cb67aa1",
                    userId = pioneer.id,
                    type = "MINING_REWARD",
                    amount = 0.22,
                    symbol = "MOBITIK",
                    fromAddress = "0xMOBITIK_REWARD_POOL",
                    toAddress = pioneer.walletAddress,
                    note = "12-Hour Mining Reward Session #19",
                    status = "COMPLETED",
                    timestamp = now - day
                )
            )

            // 5. Seed In-App Notifications
            notificationDao.insertNotification(
                InAppNotification(
                    id = "NOTIF-01",
                    userId = pioneer.id,
                    title = "Mining Reward Claimed",
                    message = "0.22 MOBITIK was successfully credited to your available balance.",
                    type = "MINING",
                    isRead = false,
                    timestamp = now - (day / 4)
                )
            )
            notificationDao.insertNotification(
                InAppNotification(
                    id = "NOTIF-02",
                    userId = pioneer.id,
                    title = "Referral Bonus Received",
                    message = "Your teammate Sarah completed a mining session! +0.022 MOBITIK bonus added.",
                    type = "REFERRAL",
                    isRead = false,
                    timestamp = now - (day / 2)
                )
            )

            // 6. Seed Configuration
            val configs = listOf(
                AppConfig("mining_duration_hours", "12"),
                AppConfig("mining_reward_amount", "0.22"),
                AppConfig("mining_enabled", "true"),
                AppConfig("referral_bonus_rate", "0.05"),
                AppConfig("send_enabled", "true"),
                AppConfig("receive_enabled", "true"),
                AppConfig("swap_enabled", "true"),
                AppConfig("network_fee", "0.001"),
                AppConfig("support_email", "support@mobitik.network"),
                AppConfig("announcement", "Welcome to MOBITIK Mainnet Alpha! Start your 12-hour session to earn rewards.")
            )
            configs.forEach { adminDao.setConfig(it) }

            // 7. Seed FAQs (Addressing all 17 asked questions)
            val faqs = listOf(
                FaqItem(
                    id = "FAQ-01",
                    question = "What is MOBITIK?",
                    answer = "MOBITIK is a modern, decentralized social cryptocurrency mining initiative designed to distribute tokens fairly and sustainably to everyday mobile users through a simple 12-hour proof-of-presence mechanism without draining your battery.",
                    category = "General",
                    orderIndex = 1
                ),
                FaqItem(
                    id = "FAQ-02",
                    question = "How does mining work?",
                    answer = "Unlike high-energy proof-of-work mining (like Bitcoin), MOBITIK uses lightweight cloud-verified consensus. You simply tap 'Start Mining' once every 12 hours to verify your presence and contribute to network growth.",
                    category = "Mining",
                    orderIndex = 2
                ),
                FaqItem(
                    id = "FAQ-03",
                    question = "How long is one mining session?",
                    answer = "Each mining session lasts exactly 12 hours from the moment you initiate it.",
                    category = "Mining",
                    orderIndex = 3
                ),
                FaqItem(
                    id = "FAQ-04",
                    question = "How much can I earn per session?",
                    answer = "The standard reward is 0.22 MOBITIK per completed 12-hour mining session. You can increase your total earnings by inviting friends to join your mining team.",
                    category = "Mining",
                    orderIndex = 4
                ),
                FaqItem(
                    id = "FAQ-05",
                    question = "How do I claim my mining rewards?",
                    answer = "Once the 12-hour countdown expires, the status changes to 'Claim Reward'. Tap the button to verify session completion and instantly transfer your 0.22 MOBITIK to your available balance.",
                    category = "Mining",
                    orderIndex = 5
                ),
                FaqItem(
                    id = "FAQ-06",
                    question = "Why must I restart mining every 12 hours?",
                    answer = "The 12-hour cycle prevents bot automation and ensures rewards are fairly distributed only to active, genuine human network participants.",
                    category = "Mining",
                    orderIndex = 6
                ),
                FaqItem(
                    id = "FAQ-07",
                    question = "How do I invite friends?",
                    answer = "Navigate to the Referrals tab, where you'll find your personal referral code and invite link. Share this with friends, and when they register with your code, they join your team.",
                    category = "Referrals",
                    orderIndex = 7
                ),
                FaqItem(
                    id = "FAQ-08",
                    question = "Where can I find my referral code?",
                    answer = "Your unique referral code is displayed on your Profile page as well as the Referrals tab.",
                    category = "Referrals",
                    orderIndex = 8
                ),
                FaqItem(
                    id = "FAQ-09",
                    question = "How do I find my wallet address?",
                    answer = "Go to the Wallet tab or tap 'Receive' to view your unique MOBITIK wallet address starting with '0xMBTK'.",
                    category = "Wallet",
                    orderIndex = 9
                ),
                FaqItem(
                    id = "FAQ-10",
                    question = "How do I receive MOBITIK?",
                    answer = "Open the Wallet tab, tap Receive, and show your QR code or copy your wallet address to provide to the sender.",
                    category = "Wallet",
                    orderIndex = 10
                ),
                FaqItem(
                    id = "FAQ-11",
                    question = "How can I send MOBITIK?",
                    answer = "Open the Wallet tab, tap Send, enter the recipient's validated 0xMBTK address and amount, review the 0.001 MOBITIK network fee, and confirm with your security PIN.",
                    category = "Wallet",
                    orderIndex = 11
                ),
                FaqItem(
                    id = "FAQ-12",
                    question = "How does token swapping work?",
                    answer = "The swap feature allows swapping MOBITIK for other simulated liquidity pairs like USDT. Swaps are subject to administrator enablement and network liquidity.",
                    category = "Swap",
                    orderIndex = 12
                ),
                FaqItem(
                    id = "FAQ-13",
                    question = "Why is my transaction pending?",
                    answer = "Transactions may remain pending briefly during network block validation or security checks. If it takes longer than expected, check Support.",
                    category = "Wallet",
                    orderIndex = 13
                ),
                FaqItem(
                    id = "FAQ-14",
                    question = "Can I mine while offline?",
                    answer = "Yes! Once you start a session, tracking occurs on the secure cloud consensus ledger. You can turn off your device, disconnect from Wi-Fi, and your timer continues running.",
                    category = "Mining",
                    orderIndex = 14
                ),
                FaqItem(
                    id = "FAQ-15",
                    question = "Can I use the same account on multiple devices?",
                    answer = "For anti-fraud and sybil protection, each MOBITIK account should be tied to one active session at a time.",
                    category = "Security",
                    orderIndex = 15
                ),
                FaqItem(
                    id = "FAQ-16",
                    question = "How do I secure my account?",
                    answer = "Use a strong password, verify your email address, enable Two-Factor Authentication (2FA) in Settings, and never share your credentials or transaction PIN.",
                    category = "Security",
                    orderIndex = 16
                ),
                FaqItem(
                    id = "FAQ-17",
                    question = "How do I contact support?",
                    answer = "Go to the Profile tab, tap Support & Help, and submit a ticket. Our administrative team reviews tickets and responds directly in the app.",
                    category = "Support",
                    orderIndex = 17
                )
            )
            faqDao.insertFaqs(faqs)
        }
    }
}
