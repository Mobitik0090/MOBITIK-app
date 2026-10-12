package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import android.net.Uri
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletTransaction
import com.example.ui.MainViewModel
import com.example.ui.components.MobitikQrCode
import com.example.ui.components.WalletBalanceCard
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikOrangeDark
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf("Overview", "Send", "Receive", "Swap", "Trust Wallet")

    val user = currentUser ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("wallet_screen")
    ) {
        // HERO ACCUMULATED WALLET CARD WITH SIMULATED SWAP SHORTCUT
        Box(modifier = Modifier.padding(16.dp)) {
            WalletBalanceCard(
                totalBalance = user.totalBalance,
                availableBalance = user.availableBalance,
                totalMiningRewards = user.totalMiningRewards,
                totalReferralRewards = user.totalReferralRewards,
                walletAddress = user.walletAddress,
                onSwapClick = { selectedTab = 3 },
                onSendClick = { selectedTab = 1 },
                onReceiveClick = { selectedTab = 2 }
            )
        }

        // TABS
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
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selectedTab == index) MobitikOrange else MobitikGrayMedium
                        )
                    }
                )
            }
        }

        // TAB CONTENTS
        when (selectedTab) {
            0 -> WalletOverviewTab(
                user = user,
                transactions = transactions,
                onSendClick = { selectedTab = 1 },
                onReceiveClick = { selectedTab = 2 },
                onSwapClick = { selectedTab = 3 },
                onTrustWalletClick = { selectedTab = 4 }
            )
            1 -> WalletSendTab(viewModel, user, onSuccess = { selectedTab = 0 })
            2 -> WalletReceiveTab(user)
            3 -> WalletSwapTab(viewModel, user, onSuccess = { selectedTab = 0 })
            4 -> WalletTrustWalletTab(viewModel, user, transactions)
        }
    }
}

@Composable
fun WalletOverviewTab(
    user: com.example.data.model.User,
    transactions: List<WalletTransaction>,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onSwapClick: () -> Unit,
    onTrustWalletClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Balances breakdown
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceItemCard(
                    title = "Available Balance",
                    value = "%.4f".format(user.availableBalance),
                    unit = "MOBITIK",
                    modifier = Modifier.weight(1f),
                    color = MobitikSuccess
                )
                Spacer(modifier = Modifier.width(10.dp))
                BalanceItemCard(
                    title = "Locked / Reserved",
                    value = "0.0000",
                    unit = "MOBITIK",
                    modifier = Modifier.weight(1f),
                    color = MobitikOrange
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceItemCard(
                    title = "Total Mining Rewards",
                    value = "%.4f".format(user.totalMiningRewards),
                    unit = "MOBITIK",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(10.dp))
                BalanceItemCard(
                    title = "Referral Rewards",
                    value = "%.4f".format(user.totalReferralRewards),
                    unit = "MOBITIK",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onSendClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = MobitikOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onReceiveClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = MobitikOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Receive")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSwapClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = MobitikOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Swap")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // TRUST WALLET AUTO-WITHDRAWAL PROMINENT CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrustWalletClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MobitikOrange,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🛡️", fontSize = 18.sp)
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
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (user.isAutoWithdrawalEnabled) MobitikSuccess.copy(alpha = 0.2f) else MobitikOrange.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (user.isAutoWithdrawalEnabled) "ACTIVE"
                                               else if (!user.trustWalletAddress.isNullOrBlank()) "LINKED"
                                               else "SETUP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (user.isAutoWithdrawalEnabled) MobitikSuccess else MobitikOrange,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (!user.trustWalletAddress.isNullOrBlank())
                                    "Connected: ${user.trustWalletAddress?.take(6)}...${user.trustWalletAddress?.takeLast(4)} • Threshold: ${"%.1f".format(user.autoWithdrawalThreshold)} MBTK"
                                else "Connect your Trust Wallet to automate balance sweeps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MobitikGrayMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = onTrustWalletClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MobitikOrange,
                            contentColor = MobitikWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Configure", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // TOKEN DETAILS & TOTAL SUPPLY HIGHLIGHT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Token Details & Supply",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MobitikOrange.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "BEP-20 / L1",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MobitikOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Supply (Hard Cap)", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("100,000,000 MOBITIK", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MobitikOrange)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mobile Mining Pool (60%)", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("60,000,000 MOBITIK", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trust Wallet Auto-Reserve (15%)", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("15,000,000 MOBITIK", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Recent Transactions Header
        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
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
                            text = "No transactions yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MobitikGrayMedium
                        )
                        Text(
                            text = "Mining rewards and transfers will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium
                        )
                    }
                }
            }
        } else {
            items(transactions.take(10).size) { index ->
                val tx = transactions[index]
                TransactionRowItem(tx)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BalanceItemCard(
    title: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MobitikGrayMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MobitikGrayMedium
            )
        }
    }
}

@Composable
fun WalletReceiveTab(user: com.example.data.model.User) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Receive MOBITIK",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Scan QR code or copy address below to receive tokens",
                style = MaterialTheme.typography.bodySmall,
                color = MobitikGrayMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // QR Code
            MobitikQrCode(
                data = user.walletAddress,
                size = 220.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Address display card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Wallet Address",
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user.walletAddress,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MobitikOrange,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copy and Share buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("MOBITIK Address", user.walletAddress)
                        clipboard.setPrimaryClip(clip)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Address")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "My MOBITIK Wallet Address")
                            putExtra(Intent.EXTRA_TEXT, "Here is my MOBITIK wallet address: ${user.walletAddress}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Address"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Warning note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Text(
                    text = "⚠️ Notice: Send only MOBITIK network assets to this address. All incoming transfers are validated by cloud consensus verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MobitikGrayMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun WalletSendTab(
    viewModel: MainViewModel,
    user: com.example.data.model.User,
    onSuccess: () -> Unit
) {
    var recipientAddress by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var pinText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val networkFee = 0.001
    val totalRequired = amountDouble + networkFee
    val isAmountValid = amountDouble > 0 && totalRequired <= user.availableBalance
    val isAddressValid = recipientAddress.trim().startsWith("0x", ignoreCase = true) && recipientAddress.trim().length >= 15

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Send MOBITIK",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Recipient field
            OutlinedTextField(
                value = recipientAddress,
                onValueChange = { recipientAddress = it },
                label = { Text("Recipient Wallet Address") },
                placeholder = { Text("0xMBTK...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_recipient_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MobitikOrange,
                    unfocusedBorderColor = MobitikBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Amount field with MAX button
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount to Send") },
                placeholder = { Text("0.00") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_amount_input"),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                trailingIcon = {
                    TextButton(
                        onClick = {
                            val max = (user.availableBalance - networkFee).coerceAtLeast(0.0)
                            amountText = String.format(Locale.US, "%.4f", max)
                        }
                    ) {
                        Text("MAX", color = MobitikOrange, fontWeight = FontWeight.Bold)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MobitikOrange,
                    unfocusedBorderColor = MobitikBorder
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available: ${"%.4f".format(user.availableBalance)} MOBITIK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MobitikGrayMedium
                )
                Text(
                    text = "Network Fee: $networkFee MOBITIK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MobitikOrange
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("e.g. Payment for service") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MobitikOrange,
                    unfocusedBorderColor = MobitikBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Security PIN
            OutlinedTextField(
                value = pinText,
                onValueChange = { if (it.length <= 4) pinText = it },
                label = { Text("Transaction PIN (4 digits)") },
                placeholder = { Text("••••") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_pin_input"),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MobitikGrayMedium)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MobitikOrange,
                    unfocusedBorderColor = MobitikBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send Button
            Button(
                onClick = { showConfirmDialog = true },
                enabled = isAmountValid && isAddressValid && pinText.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Review & Send",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Transfer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Recipient:")
                    Text(recipientAddress, color = MobitikOrange, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Transfer Amount: $amountDouble MOBITIK")
                    Text("Network Fee: $networkFee MOBITIK")
                    Text("Total Deducted: $totalRequired MOBITIK", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.sendTokens(
                            recipientAddress = recipientAddress,
                            amount = amountDouble,
                            note = noteText.ifBlank { null },
                            pin = pinText,
                            onSuccess = onSuccess
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite)
                ) {
                    Text("Confirm & Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WalletSwapTab(
    viewModel: MainViewModel,
    user: com.example.data.model.User,
    onSuccess: () -> Unit
) {
    var fromToken by remember { mutableStateOf("MOBITIK") }
    var toToken by remember { mutableStateOf("USDT") }
    var amountText by remember { mutableStateOf("") }
    var slippage by remember { mutableStateOf("0.5") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val rate = if (fromToken == "MOBITIK") 0.15 else (1.0 / 0.15)
    val slippageDouble = slippage.toDoubleOrNull() ?: 0.5
    val estimatedOutput = amountDouble * rate * (1.0 - (slippageDouble / 100.0))
    val fee = amountDouble * 0.002

    val isValid = amountDouble > 0 && (fromToken != "MOBITIK" || amountDouble <= user.availableBalance)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Swap Tokens",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Simulated decentralized exchange liquidity pool",
                style = MaterialTheme.typography.bodySmall,
                color = MobitikGrayMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // From Token Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("You Pay", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                        Text("Balance: ${"%.4f".format(user.availableBalance)} $fromToken", style = MaterialTheme.typography.labelSmall, color = MobitikOrange)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            placeholder = { Text("0.0") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MobitikOrange,
                                unfocusedBorderColor = MobitikBorder
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MobitikOrange.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange)
                        ) {
                            Text(
                                text = fromToken,
                                fontWeight = FontWeight.Bold,
                                color = MobitikOrange,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Invert Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromToken
                        fromToken = toToken
                        toToken = temp
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MobitikBorder, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Invert Tokens", tint = MobitikOrange)
                }
            }

            // To Token Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("You Receive (Estimated)", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (amountDouble > 0) "%.4f".format(estimatedOutput) else "0.0",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                        ) {
                            Text(
                                text = toToken,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Swap Details & Slippage
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
                        Text("Rate", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("1 $fromToken ≈ ${"%.4f".format(rate)} $toToken", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Swap Fee (0.2%)", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("${"%.4f".format(fee)} $fromToken", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Slippage Tolerance", style = MaterialTheme.typography.bodySmall, color = MobitikGrayMedium)
                        Text("$slippage%", style = MaterialTheme.typography.bodySmall, color = MobitikOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showConfirmDialog = true },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Swap Tokens", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Token Swap", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Swapping: $amountDouble $fromToken")
                    Text("Estimated: ${"%.4f".format(estimatedOutput)} $toToken")
                    Text("Fee: ${"%.4f".format(fee)} $fromToken")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.executeSwap(
                            fromToken = fromToken,
                            toToken = toToken,
                            amount = amountDouble,
                            slippage = slippageDouble,
                            onSuccess = onSuccess
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite)
                ) {
                    Text("Execute Swap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionRowItem(tx: WalletTransaction) {
    val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
    val isIncoming = tx.type in listOf("MINING_REWARD", "REFERRAL_REWARD", "TRANSFER_IN")

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
                        .background(if (isIncoming) MobitikSuccess.copy(alpha = 0.15f) else MobitikOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncoming) MobitikSuccess else MobitikOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when (tx.type) {
                            "MINING_REWARD" -> "Mining Reward"
                            "REFERRAL_REWARD" -> "Referral Bonus"
                            "TRANSFER_IN" -> "Received Transfer"
                            "TRANSFER_OUT" -> "Sent Transfer"
                            "TOKEN_SWAP" -> "Token Swap"
                            else -> "Transaction"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = sdf.format(Date(tx.timestamp)),
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
fun WalletTrustWalletTab(
    viewModel: MainViewModel,
    user: com.example.data.model.User,
    transactions: List<WalletTransaction>
) {
    val context = LocalContext.current
    var inputAddress by remember { mutableStateOf(user.trustWalletAddress ?: "") }
    var addressError by remember { mutableStateOf<String?>(null) }

    val twTxs = remember(transactions, user.trustWalletAddress) {
        transactions.filter {
            it.type == "TRANSFER_OUT" && (it.note?.contains("Trust Wallet", ignoreCase = true) == true || it.toAddress == user.trustWalletAddress)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wallet_trust_wallet_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER HERO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MobitikOrange.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MobitikOrange,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🛡️", fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Trust Wallet Gateway",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "BNB Smart Chain (BEP-20) Auto-Withdraw",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MobitikOrange
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!user.trustWalletAddress.isNullOrBlank()) MobitikSuccess.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (!user.trustWalletAddress.isNullOrBlank()) MobitikSuccess else MobitikBorder
                            )
                        ) {
                            Text(
                                text = if (!user.trustWalletAddress.isNullOrBlank()) "CONNECTED" else "NOT LINKED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (!user.trustWalletAddress.isNullOrBlank()) MobitikSuccess else MobitikGrayMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Connect your Trust Wallet to automate fund withdrawals. Mined tokens are swept directly to your BEP-20 wallet when your designated balance threshold is reached with zero gas fees.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MobitikGrayMedium
                    )
                }
            }
        }

        // CONNECT OR MANAGE WALLET SECTION
        if (user.trustWalletAddress.isNullOrBlank()) {
            // NOT CONNECTED: SHOW INPUT FORM
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Connect Your Trust Wallet Address",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your Trust Wallet EVM address (starts with 0x, 42 characters).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = inputAddress,
                            onValueChange = {
                                inputAddress = it
                                addressError = null
                            },
                            label = { Text("Trust Wallet BEP-20 Address") },
                            placeholder = { Text("0x...") },
                            isError = addressError != null,
                            supportingText = {
                                if (addressError != null) {
                                    Text(addressError!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Supports Trust Wallet, MetaMask & EVM addresses", color = MobitikGrayMedium)
                                }
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            inputAddress = clip.getItemAt(0).text.toString().trim()
                                            addressError = null
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Paste", tint = MobitikOrange)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trust_wallet_address_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MobitikOrange,
                                cursorColor = MobitikOrange
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    inputAddress = "0x712a8F9C34005b630eBe9472314Ac1271dB6A2C1"
                                    addressError = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MobitikOrange),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.4f))
                            ) {
                                Text("Fill Demo Address", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("trust://"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://trustwallet.com/download"))
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Trust App", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val clean = inputAddress.trim()
                                if (!clean.startsWith("0x", ignoreCase = true) || clean.length != 42) {
                                    addressError = "Please enter a valid 42-character 0x BEP-20 address"
                                } else {
                                    viewModel.connectTrustWallet(clean)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("connect_trust_wallet_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite)
                        ) {
                            Text("Connect Trust Wallet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // CONNECTED: SHOW WALLET CARD & AUTO-WITHDRAWAL SETTINGS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connected Trust Wallet",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            TextButton(
                                onClick = { viewModel.disconnectTrustWallet() }
                            ) {
                                Text("Disconnect", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("BEP-20 Destination Address", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                                    Text(
                                        text = user.trustWalletAddress ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MobitikOrange,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Trust Wallet Address", user.trustWalletAddress ?: "")
                                        clipboard.setPrimaryClip(clip)
                                        viewModel.showMessage("Trust Wallet address copied!")
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MobitikOrange, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AUTO-WITHDRAWAL SWITCH
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MobitikOrange.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Withdrawal on Threshold",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = if (user.isAutoWithdrawalEnabled) "Funds auto-transfer to Trust Wallet at ${"%.1f".format(user.autoWithdrawalThreshold)} MOBITIK"
                                               else "Toggle ON to automate withdrawals",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MobitikGrayMedium
                                    )
                                }

                                Switch(
                                    checked = user.isAutoWithdrawalEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.updateAutoWithdrawalSettings(enabled, user.autoWithdrawalThreshold)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MobitikWhite,
                                        checkedTrackColor = MobitikOrange
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // THRESHOLD SELECTION CHIPS
                        Text(
                            text = "Auto-Withdrawal Trigger Threshold",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val thresholds = listOf(5.0, 10.0, 25.0, 50.0)
                            thresholds.forEach { thresh ->
                                val isSelected = (user.autoWithdrawalThreshold == thresh)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateAutoWithdrawalSettings(user.isAutoWithdrawalEnabled, thresh)
                                    },
                                    label = { Text("${thresh.toInt()} MBTK", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MobitikOrange,
                                        selectedLabelColor = MobitikWhite,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MobitikGrayMedium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // THRESHOLD PROGRESS BAR
                        val progressFraction = (user.availableBalance / user.autoWithdrawalThreshold).coerceIn(0.0, 1.0).toFloat()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Balance: ${"%.4f".format(user.availableBalance)} MOBITIK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MobitikGrayMedium
                            )
                            Text(
                                text = "Target: ${"%.1f".format(user.autoWithdrawalThreshold)} MOBITIK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MobitikOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (progressFraction >= 1.0f) MobitikSuccess else MobitikOrange,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // IMMEDIATE WITHDRAW ACTION BUTTON
                        Button(
                            onClick = {
                                viewModel.triggerAutoWithdrawalNow()
                            },
                            enabled = user.availableBalance > 0.0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("trigger_trust_wallet_withdrawal_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MobitikOrange,
                                contentColor = MobitikWhite
                            )
                        ) {
                            Text(
                                text = if (user.availableBalance > 0.0) "Withdraw ${"%.4f".format(user.availableBalance)} MOBITIK to Trust Wallet Now"
                                       else "Insufficient Balance to Withdraw",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // LOW BATTERY & LOW INTERNET ARCHITECTURE NOTICE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MobitikOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Free Local Data Storage & Eco Efficiency",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "User data and wallet balances are stored directly in Android SQLite (Room). Zero cloud server costs, zero background battery drain, and offline-first persistence.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium
                        )
                    }
                }
            }
        }

        // TRUST WALLET WITHDRAWAL HISTORY
        item {
            Text(
                text = "Trust Wallet Withdrawal Records",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (twTxs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Trust Wallet auto-withdrawals yet.\nWithdrawals will be logged here with TX hashes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(twTxs.size) { index ->
                TransactionRowItem(twTxs[index])
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
