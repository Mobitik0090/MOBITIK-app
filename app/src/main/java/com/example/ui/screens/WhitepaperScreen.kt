package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitepaperScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val whitepaperSections = listOf(
        "1. Executive Summary" to "MOBITIK is an eco-friendly proof-of-presence cryptocurrency distribution protocol. It allows global smartphone pioneers to participate in decentralized currency distribution without costly mining rigs, battery degradation, or heavy data usage.",
        "2. Project Vision & Mission" to "Our mission is to establish a globally distributed, accessible Web3 network. Through daily human verification, MOBITIK bridges traditional internet users into digital sovereignty with seamless microtransactions, native token swaps, and automated Trust Wallet custody.",
        "3. Token Details & Fixed Total Supply" to """
• Token Name: MOBITIK Token
• Token Symbol: MOBITIK
• Total Supply (Hard Cap): 100,000,000 MOBITIK (Fixed Supply - No Inflation)
• Initial Circulating Supply: 12,500,000 MOBITIK (12.5%)
• Decimals: 18
• Standard: BEP-20 (BNB Smart Chain) & Native MOBITIK Layer-1
• Contract Address: 0x7120a85b9b1d981249b67484df1081549e3240bc
• Custody Support: Direct integration with Trust Wallet, MetaMask, and hardware wallets.
• Auto-Withdrawal: Built-in smart sweep triggers funds directly to your connected Trust Wallet address.
        """.trimIndent(),
        "4. Tokenomics & Distribution Breakdown" to """
The fixed total supply of 100,000,000 MOBITIK is allocated as follows:

1. Mobile Mining Pool: 60% (60,000,000 MOBITIK)
   Distributed to active human pioneers over multi-year halving eras.
2. DEX/CEX Liquidity & Auto-Withdrawal Reserve: 15% (15,000,000 MOBITIK)
   Ensures deep liquidity pools and immediate fund execution for Trust Wallet auto-withdrawals.
3. Referral & Team Growth: 10% (10,000,000 MOBITIK)
   Incentivizes genuine network word-of-mouth expansion with anti-sybil checks.
4. Core Infrastructure & Security Audits: 10% (10,000,000 MOBITIK)
   Funds ongoing protocol development, smart contract audits, and server node maintenance.
5. Ecosystem Treasury & Grants: 5% (5,000,000 MOBITIK)
   Reserved for developer grants, partnerships, and bug bounties (locked with 24-month timelock).
        """.trimIndent(),
        "5. How 12-Hour Mining Works" to "Rather than burning computational cycles (Proof-of-Work), MOBITIK relies on Proof-of-Presence. Each pioneer taps 'Start Mining' once every 12 hours. A cryptographic server timestamp anchors the session. Once the 12-hour period concludes, the user claims their 0.22 MOBITIK reward.",
        "6. Manual Session Cadence (No Bot Auto-Start)" to "To preserve genuine fairness, prevent bot scripts, and protect battery life, mining sessions cannot auto-start indefinitely. Pioneers must manually check in and re-assert human presence to begin a new 12-hour cycle.",
        "7. Halving Schedule & Deflationary Mechanism" to """
Mining emission undergoes scheduled halvings as pioneer adoption milestones are reached:
• Phase 1 (Genesis / Current): 0.22 MOBITIK / 12h (0.01833/h)
• Phase 2 (at 1,000,000 Pioneers): 0.11 MOBITIK / 12h (0.00917/h)
• Phase 3 (at 10,000,000 Pioneers): 0.055 MOBITIK / 12h (0.00458/h)
• Phase 4 (Open Mainnet Cap): 0.0275 MOBITIK / 12h, until 60M supply is minted.
        """.trimIndent(),
        "8. Trust Wallet Auto-Withdrawal Gateway" to """
Pioneers can bind their personal Trust Wallet address (BEP-20 / EVM). When enabled:
• Mined funds automatically transfer to Trust Wallet once your chosen balance threshold (5, 10, 25, 50, or 100 MOBITIK) is reached.
• Network gas is subsidized by the protocol's liquidity reserve for Pioneer accounts.
• Real on-chain and internal ledger receipts are recorded in your wallet history.
        """.trimIndent(),
        "9. Low Battery & Low Internet Consumption" to """
MOBITIK is built with a green, eco-friendly mobile architecture:
• 0% CPU Battery Drain: The countdown operates using delta time arithmetic rather than background CPU hashing.
• Low Internet Usage: The app uses Android SQLite (Room) for local-first persistence, minimizing redundant server roundtrips.
• Zero Cost & Open Architecture: Room SQLite requires zero subscription fees or external cloud dependencies.
        """.trimIndent(),
        "10. Referral Multipliers & Team Rewards" to "Inviting active friends grants a +25% mining speed boost for overlapping active sessions. Sybil rings and self-referrals are prevented via device fingerprinting and unique cryptographic account addresses.",
        "11. Decentralized Swaps & Wallet Utility" to "Users hold unique 0xMBTK ledger addresses. The integrated swap module allows exchange between MOBITIK, USDT, and BNB with transparent slippage and decentralized liquidity reserves.",
        "12. Security & Anti-Fraud Architecture" to "• Single account per physical user.\n• Cryptographic SHA-256 password hashing.\n• Optional Two-Factor Authentication (2FA).\n• Mandatory secondary PIN verification for withdrawals and transfers.",
        "13. Project Roadmap" to "• Phase 1 (Current): Pioneer distribution, 12h sessions, Trust Wallet auto-withdrawal gateway, local SQLite engine.\n• Phase 2: Decentralized KYC verification, EVM bridge testnet, expanded token swaps.\n• Phase 3: Open Mainnet launch, DEX listing, merchant gateway SDK.",
        "14. Risk Disclosures & Disclaimer" to "MOBITIK is a utility and distribution protocol. Token mining during bootstrap phases does not constitute an investment contract or guarantee of fiat profit. Pioneers participate voluntarily."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MOBITIK Whitepaper", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "MOBITIK Whitepaper & Tokenomics")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "MOBITIK Whitepaper: Total Supply 100,000,000 MOBITIK (BEP-20) with 12-Hour Mobile Mining & Trust Wallet Auto-Withdrawal. Read more at https://mobitik.network"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Whitepaper"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MobitikOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier.testTag("whitepaper_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HERO BANNER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MobitikOrange.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MOBITIK Protocol Whitepaper",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MobitikOrange
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MobitikOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "v2.0 • 2026",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MobitikOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Proof-of-Presence Mobile Consensus, Tokenomics, Fixed Total Supply & Trust Wallet Integration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium
                        )
                    }
                }
            }

            // PROMINENT TOKEN DETAILS & TOTAL SUPPLY CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MobitikOrange)
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
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🪙", fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Token Details & Total Supply",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Fixed Hard Cap Specification",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MobitikOrange
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MobitikSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MobitikSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TokenDetailRow("Token Name", "MOBITIK Token")
                        TokenDetailRow("Token Symbol", "MOBITIK")
                        TokenDetailRow("Total Supply", "100,000,000 MOBITIK", isHighlighted = true)
                        TokenDetailRow("Circulating Initial", "12,500,000 MOBITIK (12.5%)")
                        TokenDetailRow("Decimals", "18")
                        TokenDetailRow("Standard", "BEP-20 (BNB Smart Chain) & Layer-1")
                        TokenDetailRow("Contract Address", "0x7120a8...40bc", isAddress = true, fullAddress = "0x7120a85b9b1d981249b67484df1081549e3240bc")
                        TokenDetailRow("Trust Wallet Auto-Sweep", "Supported (Custom Thresholds)", isHighlighted = true)
                        TokenDetailRow("Battery & Data Footprint", "0% CPU Drain • Offline SQLite", isHighlighted = true)

                        Spacer(modifier = Modifier.height(14.dp))

                        // TOKEN ALLOCATION GRAPHICAL BREAKDOWN
                        Text(
                            text = "Token Allocation (100,000,000 MOBITIK)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AllocationBarItem("Community Mobile Mining (60%)", "60,000,000 MOBITIK", MobitikOrange, 0.60f)
                        AllocationBarItem("Liquidity & Auto-Withdrawal Reserve (15%)", "15,000,000 MOBITIK", MobitikSuccess, 0.15f)
                        AllocationBarItem("Referral & Pioneer Team Growth (10%)", "10,000,000 MOBITIK", MobitikOrange.copy(alpha = 0.7f), 0.10f)
                        AllocationBarItem("Core Dev & Security Audits (10%)", "10,000,000 MOBITIK", MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), 0.10f)
                        AllocationBarItem("Ecosystem Grants & Treasury (5%)", "5,000,000 MOBITIK", MobitikGrayMedium, 0.05f)
                    }
                }
            }

            // WHITEPAPER SECTIONS
            items(whitepaperSections.size) { index ->
                val (title, content) = whitepaperSections[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MobitikOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TokenDetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    isAddress: Boolean = false,
    fullAddress: String? = null
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MobitikGrayMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isHighlighted) MobitikOrange else MaterialTheme.colorScheme.onBackground
            )
            if (isAddress && fullAddress != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Contract Address", fullAddress)
                        clipboard.setPrimaryClip(clip)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MobitikOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AllocationBarItem(
    name: String,
    amount: String,
    barColor: androidx.compose.ui.graphics.Color,
    fraction: Float
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
            Text(amount, style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(5.dp)
                    .background(barColor, RoundedCornerShape(3.dp))
            )
        }
    }
}
