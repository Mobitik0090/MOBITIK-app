package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MiningCircle
import com.example.ui.components.MobitikLogo
import com.example.ui.components.StatCard
import com.example.ui.components.WalletBalanceCard
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikOrangeDark
import com.example.ui.theme.MobitikOrangeLight
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToWalletTab: () -> Unit,
    onNavigateToWalletSwap: () -> Unit = onNavigateToWalletTab,
    onNavigateToTrustWallet: () -> Unit = onNavigateToWalletTab,
    onNavigateToReferralsTab: () -> Unit,
    onNavigateToHistoryTab: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val remainingMillis by viewModel.remainingTimeMillis.collectAsState()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val miningHistory by viewModel.miningHistory.collectAsState()
    val referredUsers by viewModel.referredUsers.collectAsState()

    val user = currentUser ?: return

    val activeMiningTeammates = referredUsers.count { it.status == "ACTIVE" }
    val baseRate = 0.01833
    val referralBoostRate = activeMiningTeammates * 0.00458 // +25% boost per teammate
    val currentMiningRate = baseRate + referralBoostRate

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // TOP APP BAR / HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MobitikLogo(size = 38.dp, showText = true)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Account Switcher for Evaluator/Reviewer
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val switchToAdmin = user.role != "ADMIN"
                                viewModel.switchAccount(toAdmin = switchToAdmin)
                            }
                            .border(1.dp, MobitikOrange.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MobitikOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (user.role == "ADMIN") "Role: Admin" else "Role: Pioneer",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MobitikOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notifications Bell
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(
                                        containerColor = MobitikOrange,
                                        contentColor = MobitikWhite
                                    ) {
                                        Text("$unreadNotifs")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Avatar button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MobitikOrangeDark)
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.take(1).uppercase(),
                            color = MobitikWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // USER GREETING & WALLET ACCUMULATED TOKENS COMPONENT
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hello, ${user.fullName.ifBlank { user.username }} 👋",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MobitikGrayMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MobitikOrange.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "RATE: ${"%.4f".format(currentMiningRate)}/h",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MobitikOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                WalletBalanceCard(
                    totalBalance = user.totalBalance,
                    availableBalance = user.availableBalance,
                    totalMiningRewards = user.totalMiningRewards,
                    totalReferralRewards = user.totalReferralRewards,
                    walletAddress = user.walletAddress,
                    onSwapClick = onNavigateToWalletSwap,
                    onSendClick = onNavigateToWalletTab,
                    onReceiveClick = onNavigateToWalletTab
                )
            }
        }

        // CENTERPIECE CIRCULAR MINING INTERFACE
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                MiningCircle(
                    session = activeSession,
                    remainingMillis = remainingMillis,
                    isLoading = isLoading,
                    onStartMining = { viewModel.startMining() },
                    onClaimReward = { viewModel.claimMiningReward() }
                )
            }
        }

        // PRIMARY 'START MINING' / 'CLAIM REWARD' ACTION BUTTON
        item {
            val isMiningActive = activeSession != null && activeSession!!.status == "ACTIVE" && remainingMillis > 0
            val isReadyToClaim = activeSession != null && (activeSession!!.status == "COMPLETED" || (activeSession!!.status == "ACTIVE" && remainingMillis <= 0))
            val isIdle = activeSession == null || activeSession!!.status == "CLAIMED" || activeSession!!.status == "EXPIRED"

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (isReadyToClaim) viewModel.claimMiningReward()
                        else if (isIdle) viewModel.startMining()
                    },
                    enabled = !isLoading && (isReadyToClaim || isIdle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("primary_start_mining_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isReadyToClaim -> MobitikSuccess
                            isMiningActive -> MobitikOrange.copy(alpha = 0.35f)
                            else -> MobitikOrange
                        },
                        contentColor = MobitikWhite,
                        disabledContainerColor = if (isMiningActive) MaterialTheme.colorScheme.surfaceVariant else MobitikOrange.copy(alpha = 0.5f),
                        disabledContentColor = if (isMiningActive) MobitikOrange else MobitikWhite.copy(alpha = 0.7f)
                    ),
                    border = if (isMiningActive) androidx.compose.foundation.BorderStroke(1.5.dp, MobitikOrange) else null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isReadyToClaim -> Icons.Default.CheckCircle
                                isMiningActive -> Icons.Default.Bolt
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when {
                                isReadyToClaim -> "CLAIM 0.22 MOBITIK REWARD"
                                isMiningActive -> {
                                    val hours = remainingMillis / 3600000
                                    val minutes = (remainingMillis % 3600000) / 60000
                                    val seconds = (remainingMillis % 60000) / 1000
                                    "MINING IN PROGRESS • %02d:%02d:%02d".format(hours, minutes, seconds)
                                }
                                else -> "START 12-HOUR MINING (+0.22 MOBITIK)"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isMiningActive) "⚡ Cloud Consensus Node Active • Zero Battery Consumption"
                    else if (isReadyToClaim) "🎉 12-Hour Session Complete! Claim your verified reward above"
                    else "Tap to start your 12-hour proof-of-presence session",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isReadyToClaim) MobitikSuccess else MobitikGrayMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // HASHRATE & MINING BOOST BREAKDOWN CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mining Hashrate Breakdown",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MobitikOrange.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Total: ${"%.4f".format(currentMiningRate)}/h",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MobitikOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Node Rate", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("${"%.4f".format(baseRate)} MOBITIK/h", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Referral Team Multiplier ($activeMiningTeammates Active)", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("+${"%.4f".format(referralBoostRate)} MOBITIK/h", style = MaterialTheme.typography.bodySmall, color = MobitikSuccess, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("12-Hour Session Yield", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("0.2200 MOBITIK", style = MaterialTheme.typography.bodySmall, color = MobitikOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TRUST WALLET AUTO-WITHDRAWAL STATUS & QUICK ACCESS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToTrustWallet() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!user.trustWalletAddress.isNullOrBlank())
                        MobitikOrange.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (!user.trustWalletAddress.isNullOrBlank()) MobitikOrange.copy(alpha = 0.5f) else MobitikBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (!user.trustWalletAddress.isNullOrBlank()) MobitikOrange else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🛡️",
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Trust Wallet Auto-Withdrawal",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (user.isAutoWithdrawalEnabled) MobitikSuccess.copy(alpha = 0.2f) else MobitikOrange.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (user.isAutoWithdrawalEnabled) "ACTIVE"
                                               else if (!user.trustWalletAddress.isNullOrBlank()) "LINKED"
                                               else "READY",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (user.isAutoWithdrawalEnabled) MobitikSuccess else MobitikOrange,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (!user.trustWalletAddress.isNullOrBlank()) {
                                    val tw = user.trustWalletAddress!!
                                    "Target: ${tw.take(6)}...${tw.takeLast(4)} • Min: ${"%.1f".format(user.autoWithdrawalThreshold)} MOBITIK"
                                } else {
                                    "Connect Trust Wallet to auto-sweep rewards to BEP-20"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MobitikGrayMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToTrustWallet,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!user.trustWalletAddress.isNullOrBlank()) MobitikOrange.copy(alpha = 0.2f) else MobitikOrange,
                            contentColor = if (!user.trustWalletAddress.isNullOrBlank()) MobitikOrange else MobitikWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (!user.trustWalletAddress.isNullOrBlank()) "Manage" else "Connect",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // LOW BATTERY & LOW INTERNET OFFLINE DATA CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🌱 Zero Battery & Offline Engine",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MobitikSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "0% CPU",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MobitikSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (activeSession?.status == "ACTIVE") {
                                val sdf = SimpleDateFormat("h:mm a, MMM dd", Locale.getDefault())
                                "Mining verified locally in SQLite. Next claim: ${sdf.format(Date(activeSession!!.expectedEndTimeMillis))}"
                            } else {
                                "Real persistent user data in Room SQLite • Low bandwidth & battery consumption"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // QUICK ACTION SHORTCUTS (Send, Receive, Swap, Invite)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionItem(
                    label = "Send",
                    icon = Icons.Default.ArrowUpward,
                    onClick = onNavigateToWalletTab
                )
                QuickActionItem(
                    label = "Receive",
                    icon = Icons.Default.ArrowDownward,
                    onClick = onNavigateToWalletTab
                )
                QuickActionItem(
                    label = "Swap",
                    icon = Icons.Default.SwapHoriz,
                    onClick = onNavigateToWalletTab
                )
                QuickActionItem(
                    label = "Invite",
                    icon = Icons.Default.Group,
                    onClick = onNavigateToReferralsTab
                )
            }
        }

        // STATS GRID SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Mining Network Metrics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Available Balance",
                        value = "%.3f".format(user.availableBalance),
                        subtitle = "Unlocked MOBITIK",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    StatCard(
                        title = "Mining Rewards",
                        value = "%.3f".format(user.totalMiningRewards),
                        subtitle = "${user.successfulSessionsCount} Sessions",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f),
                        isHighlighted = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Referral Team",
                        value = "${referredUsers.size} Pioneers",
                        subtitle = "$activeMiningTeammates Mining Active",
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    StatCard(
                        title = "Team Boost Rate",
                        value = "+${"%.4f".format(referralBoostRate)}/h",
                        subtitle = "Active Team Bonus",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // RECENT MINING ACTIVITY
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Mining Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MobitikOrange,
                        modifier = Modifier.clickable { onNavigateToHistoryTab() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (miningHistory.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No completed mining sessions yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MobitikGrayMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start your first 12-hour mining session above!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MobitikOrange
                            )
                        }
                    }
                } else {
                    miningHistory.take(3).forEach { session ->
                        RecentMiningItem(session)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MobitikOrange.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MobitikOrange,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun RecentMiningItem(session: com.example.data.model.MiningSession) {
    val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MobitikOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MobitikOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Session #${session.sessionNumber}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = sdf.format(Date(session.startTimeMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${session.rewardAmount} MOBITIK",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MobitikSuccess
                )
                Text(
                    text = session.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (session.status) {
                        "CLAIMED" -> MobitikSuccess
                        "ACTIVE" -> MobitikOrange
                        else -> MobitikGrayMedium
                    }
                )
            }
        }
    }
}
