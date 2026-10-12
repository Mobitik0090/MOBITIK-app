package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.SupportTicket
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikError
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.adminOverviewStats.collectAsState()
    val allUsers by viewModel.adminAllUsers.collectAsState()
    val allTickets by viewModel.adminAllTickets.collectAsState()
    val auditLogs by viewModel.adminAuditLogs.collectAsState()
    val configs by viewModel.adminConfigs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Mining Config", "Wallet Config", "Users", "Support", "Audit Logs")

    var selectedTicketToReply by remember { mutableStateOf<SupportTicket?>(null) }
    var userSearchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier.testTag("admin_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MobitikOrange,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MobitikOrange,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                label,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == idx) MobitikOrange else MobitikGrayMedium
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminOverviewTab(stats, allUsers, allTickets)
                1 -> AdminMiningConfigTab(configs, onUpdate = { k, v -> viewModel.adminUpdateConfig(k, v) })
                2 -> AdminWalletConfigTab(configs, onUpdate = { k, v -> viewModel.adminUpdateConfig(k, v) })
                3 -> AdminUsersTab(allUsers, userSearchQuery, onSearchChange = { userSearchQuery = it }) { targetId, newStatus ->
                    viewModel.adminToggleUserStatus(targetId, newStatus)
                }
                4 -> AdminSupportTab(allTickets) { ticket -> selectedTicketToReply = ticket }
                5 -> AdminAuditLogsTab(auditLogs)
            }
        }
    }

    // Reply to Support Ticket Dialog
    selectedTicketToReply?.let { ticket ->
        var replyText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedTicketToReply = null },
            title = { Text("Reply to Ticket ${ticket.id}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Subject: ${ticket.subject}", fontWeight = FontWeight.SemiBold)
                    Text("User: ${ticket.userEmail}", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Message: ${ticket.message}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Official Response") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            viewModel.adminReplyTicket(ticket.id, replyText, "RESOLVED")
                            selectedTicketToReply = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite)
                ) {
                    Text("Resolve & Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTicketToReply = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    stats: Map<String, Any>,
    users: List<User>,
    tickets: List<SupportTicket>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "System Telemetry & Key Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Total Pioneers",
                    value = "${users.size}",
                    subtitle = "Registered Accounts",
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Active Miners",
                    value = "${stats["activeMiners"] ?: 0}",
                    subtitle = "Concurrent 12h Sessions",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f),
                    isHighlighted = true
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Completed Sessions",
                    value = "${stats["completedSessions"] ?: 0}",
                    subtitle = "Proof-of-Presence",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Total Rewards",
                    value = "${"%.2f".format((stats["totalRewards"] as? Double) ?: 0.0)}",
                    subtitle = "MOBITIK Distributed",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Open Support",
                    value = "${tickets.count { it.status == "OPEN" }}",
                    subtitle = "Pending Resolutions",
                    icon = Icons.Default.SupportAgent,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Suspicious Flags",
                    value = "0",
                    subtitle = "Sybil Protection Active",
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AdminMiningConfigTab(
    configs: List<com.example.data.model.AppConfig>,
    onUpdate: (String, String) -> Unit
) {
    val durationHours = configs.find { it.configKey == "mining_duration_hours" }?.configValue ?: "12"
    val rewardAmount = configs.find { it.configKey == "mining_reward_amount" }?.configValue ?: "0.22"
    val miningEnabled = configs.find { it.configKey == "mining_enabled" }?.configValue?.toBooleanStrictOrNull() ?: true
    val refBonusRate = configs.find { it.configKey == "referral_bonus_rate" }?.configValue ?: "0.05"

    var tempDuration by remember(durationHours) { mutableStateOf(durationHours) }
    var tempReward by remember(rewardAmount) { mutableStateOf(rewardAmount) }
    var tempRefRate by remember(refBonusRate) { mutableStateOf(refBonusRate) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Consensus Engine Parameters", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MobitikOrange)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mining Master Switch", fontWeight = FontWeight.SemiBold)
                            Text("Enable or pause new 12h sessions", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        }
                        Switch(
                            checked = miningEnabled,
                            onCheckedChange = { onUpdate("mining_enabled", it.toString()) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MobitikWhite, checkedTrackColor = MobitikOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tempDuration,
                        onValueChange = { tempDuration = it },
                        label = { Text("Mining Session Duration (Hours)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempReward,
                        onValueChange = { tempReward = it },
                        label = { Text("Base Reward Per Session (MOBITIK)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempRefRate,
                        onValueChange = { tempRefRate = it },
                        label = { Text("Referral Bonus Rate (Multiplier, e.g. 0.05 = 5%)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onUpdate("mining_duration_hours", tempDuration)
                            onUpdate("mining_reward_amount", tempReward)
                            onUpdate("referral_bonus_rate", tempRefRate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Consensus Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWalletConfigTab(
    configs: List<com.example.data.model.AppConfig>,
    onUpdate: (String, String) -> Unit
) {
    val sendEnabled = configs.find { it.configKey == "send_enabled" }?.configValue?.toBooleanStrictOrNull() ?: true
    val receiveEnabled = configs.find { it.configKey == "receive_enabled" }?.configValue?.toBooleanStrictOrNull() ?: true
    val swapEnabled = configs.find { it.configKey == "swap_enabled" }?.configValue?.toBooleanStrictOrNull() ?: true
    val networkFee = configs.find { it.configKey == "network_fee" }?.configValue ?: "0.001"

    var tempFee by remember(networkFee) { mutableStateOf(networkFee) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wallet & Transfer Controls", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MobitikOrange)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Send Transfers", fontWeight = FontWeight.SemiBold)
                            Text("Allow peer-to-peer sending", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        }
                        Switch(
                            checked = sendEnabled,
                            onCheckedChange = { onUpdate("send_enabled", it.toString()) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MobitikWhite, checkedTrackColor = MobitikOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Receive Transfers", fontWeight = FontWeight.SemiBold)
                            Text("Allow address resolution & deposits", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        }
                        Switch(
                            checked = receiveEnabled,
                            onCheckedChange = { onUpdate("receive_enabled", it.toString()) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MobitikWhite, checkedTrackColor = MobitikOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Token Swaps", fontWeight = FontWeight.SemiBold)
                            Text("Enable automated DEX liquidity swapping", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        }
                        Switch(
                            checked = swapEnabled,
                            onCheckedChange = { onUpdate("swap_enabled", it.toString()) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MobitikWhite, checkedTrackColor = MobitikOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tempFee,
                        onValueChange = { tempFee = it },
                        label = { Text("Network Fee (MOBITIK)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onUpdate("network_fee", tempFee) },
                        colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Wallet Controls", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab(
    users: List<User>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleStatus: (String, String) -> Unit
) {
    val filtered = users.filter {
        searchQuery.isBlank() ||
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.walletAddress.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search users by name, email, or wallet...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MobitikGrayMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                singleLine = true
            )
        }

        items(filtered) { u ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(u.username, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(u.email, style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        }
                        StatusBadge(
                            status = u.status,
                            isPositive = u.status == "ACTIVE"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wallet: ${u.walletAddress.take(12)}...${u.walletAddress.takeLast(6)}", style = MaterialTheme.typography.labelSmall, color = MobitikOrange)
                    Text("Balance: ${"%.4f".format(u.totalBalance)} MOBITIK · Ref Code: ${u.referralCode}", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)

                    Spacer(modifier = Modifier.height(10.dp))

                    if (u.role != "ADMIN") {
                        Row {
                            if (u.status == "ACTIVE") {
                                Button(
                                    onClick = { onToggleStatus(u.id, "SUSPENDED") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MobitikError, contentColor = MobitikWhite),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Suspend", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { onToggleStatus(u.id, "ACTIVE") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MobitikSuccess, contentColor = MobitikWhite),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reactivate", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSupportTab(
    tickets: List<SupportTicket>,
    onSelectReply: (SupportTicket) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tickets) { ticket ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ticket.id, fontWeight = FontWeight.Bold, color = MobitikOrange)
                        StatusBadge(status = ticket.status, isPositive = ticket.status == "RESOLVED")
                    }
                    Text(ticket.subject, fontWeight = FontWeight.SemiBold)
                    Text("From: ${ticket.userEmail}", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ticket.message, style = MaterialTheme.typography.bodySmall)

                    if (!ticket.adminReply.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Current Reply: ${ticket.adminReply}", style = MaterialTheme.typography.labelSmall, color = MobitikSuccess)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSelectReply(ticket) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = MobitikOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Respond & Resolve", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogsTab(logs: List<AuditLog>) {
    val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm:ss a", Locale.getDefault())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(logs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.action, fontWeight = FontWeight.Bold, color = MobitikOrange)
                        Text(sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    }
                    Text("By: ${log.adminUsername} ${if (!log.target.isNullOrBlank()) "→ Target: ${log.target}" else ""}", style = MaterialTheme.typography.bodySmall)
                    Text(log.details, style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                }
            }
        }
    }
}
