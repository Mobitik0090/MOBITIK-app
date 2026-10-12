package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.MiningSession
import com.example.data.model.WalletTransaction
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val miningSessions by viewModel.miningHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTxTypeFilter by remember { mutableStateOf("ALL") }
    var selectedTxDetail by remember { mutableStateOf<WalletTransaction?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // Tab Row: "Wallet Transactions" vs "Mining History"
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MobitikOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MobitikOrange,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Wallet Ledger",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) MobitikOrange else MobitikGrayMedium
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Mining Sessions",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) MobitikOrange else MobitikGrayMedium
                    )
                }
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by ID, address, or note...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MobitikGrayMedium) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MobitikGrayMedium)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MobitikOrange,
                unfocusedBorderColor = MobitikBorder
            ),
            singleLine = true
        )

        if (selectedTab == 0) {
            // Transaction Filters Row
            val filters = listOf("ALL", "MINING_REWARD", "REFERRAL_REWARD", "TRANSFER_IN", "TRANSFER_OUT", "TOKEN_SWAP")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { f ->
                    val label = when (f) {
                        "ALL" -> "All"
                        "MINING_REWARD" -> "Mining"
                        "REFERRAL_REWARD" -> "Referral"
                        "TRANSFER_IN" -> "Incoming"
                        "TRANSFER_OUT" -> "Outgoing"
                        "TOKEN_SWAP" -> "Swaps"
                        else -> f
                    }
                    FilterChip(
                        selected = selectedTxTypeFilter == f,
                        onClick = { selectedTxTypeFilter = f },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MobitikOrange,
                            selectedLabelColor = MobitikWhite
                        )
                    )
                }
            }

            // Filtered Transactions List
            val filteredTxs = transactions.filter { tx ->
                val matchesType = selectedTxTypeFilter == "ALL" || tx.type == selectedTxTypeFilter
                val matchesSearch = searchQuery.isBlank() ||
                        tx.txHash.contains(searchQuery, ignoreCase = true) ||
                        (tx.fromAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                        (tx.toAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                        (tx.note?.contains(searchQuery, ignoreCase = true) == true)
                matchesType && matchesSearch
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredTxs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                        ) {
                            Text(
                                text = "No matching transactions found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MobitikGrayMedium,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(filteredTxs) { tx ->
                        DetailedTransactionRow(tx = tx, onClick = { selectedTxDetail = tx })
                    }
                }
            }
        } else {
            // Mining Sessions List
            val filteredSessions = miningSessions.filter { s ->
                searchQuery.isBlank() ||
                        s.id.contains(searchQuery, ignoreCase = true) ||
                        s.sessionNumber.toString().contains(searchQuery) ||
                        s.status.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredSessions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                        ) {
                            Text(
                                text = "No mining sessions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MobitikGrayMedium,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(filteredSessions) { session ->
                        MiningSessionCard(session)
                    }
                }
            }
        }
    }

    // Transaction Details Dialog
    selectedTxDetail?.let { tx ->
        val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm:ss a", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { selectedTxDetail = null },
            title = { Text("Transaction Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    DetailField("Transaction Hash", tx.txHash)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailField("Type", tx.type)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailField("Amount", "${tx.amount} ${tx.symbol}")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailField("Network Fee", "${tx.fee} MOBITIK")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailField("Timestamp", sdf.format(Date(tx.timestamp)))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!tx.fromAddress.isNullOrBlank()) {
                        DetailField("From Address", tx.fromAddress)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!tx.toAddress.isNullOrBlank()) {
                        DetailField("To Address", tx.toAddress)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!tx.note.isNullOrBlank()) {
                        DetailField("Note / Memo", tx.note)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    DetailField("Status", tx.status)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTxDetail = null }) {
                    Text("Close", color = MobitikOrange, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun DetailField(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DetailedTransactionRow(tx: WalletTransaction, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
    val isIncoming = tx.type in listOf("MINING_REWARD", "REFERRAL_REWARD", "TRANSFER_IN")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        .background(if (isIncoming) MobitikSuccess.copy(alpha = 0.15f) else MobitikOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (tx.type) {
                            "MINING_REWARD" -> Icons.Default.TrendingUp
                            "TRANSFER_IN" -> Icons.Default.ArrowDownward
                            "TRANSFER_OUT" -> Icons.Default.ArrowUpward
                            "TOKEN_SWAP" -> Icons.Default.SwapHoriz
                            else -> Icons.Default.TrendingUp
                        },
                        contentDescription = null,
                        tint = if (isIncoming) MobitikSuccess else MobitikOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when (tx.type) {
                            "MINING_REWARD" -> "12h Mining Reward"
                            "REFERRAL_REWARD" -> "Referral Team Bonus"
                            "TRANSFER_IN" -> "Incoming Transfer"
                            "TRANSFER_OUT" -> "Outgoing Transfer"
                            "TOKEN_SWAP" -> "Token Swap"
                            else -> "Transaction"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${tx.txHash.take(12)}... · ${sdf.format(Date(tx.timestamp))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncoming) "+" else "-"}${"%.4f".format(tx.amount)} ${tx.symbol}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isIncoming) MobitikSuccess else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = tx.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tx.status == "COMPLETED") MobitikSuccess else MobitikOrange
                )
            }
        }
    }
}

@Composable
fun MiningSessionCard(session: MiningSession) {
    val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
    val durationHours = 12

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MobitikOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${session.sessionNumber}",
                            fontWeight = FontWeight.Bold,
                            color = MobitikOrange,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mining Session #${session.sessionNumber}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Duration: $durationHours Hours",
                            style = MaterialTheme.typography.labelSmall,
                            color = MobitikGrayMedium
                        )
                    }
                }

                StatusBadge(
                    status = session.status,
                    isPositive = session.status in listOf("CLAIMED", "COMPLETED")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Started", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    Text(sdf.format(Date(session.startTimeMillis)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Reward", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    Text("+${session.rewardAmount} MOBITIK", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MobitikSuccess)
                }
            }
        }
    }
}
