package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Privacy Policy", "Terms & Conditions", "Mining Rules", "Risk Disclosure", "Token Disclaimer")

    val contents = listOf(
        // Privacy Policy
        """
        1. Information We Collect
        MOBITIK collects username, email address, password hashes (SHA-256), and cryptographic wallet public keys generated during registration. We do NOT collect device contacts, photos, or location telemetry.
        
        2. How Information Is Used
        Data is strictly utilized for consensus participation, 12-hour session verification, referral attribution, and fraud prevention.
        
        3. Data Security & Storage
        All account credentials are protected using industry-standard hashing and encryption. We never sell user data to third-party advertising networks.
        
        4. Right to Deletion
        Users can submit an account deletion request through the Profile Settings. All identifiable account records will be purged within 30 days.
        """.trimIndent(),

        // Terms & Conditions
        """
        1. Eligibility
        You must be at least 18 years of age or the age of legal majority in your jurisdiction to participate in the MOBITIK mining ecosystem.
        
        2. Account Ownership
        Each user is permitted one (1) account. The creation of automated bot farms, multiple accounts, or fraudulent referral rings will result in immediate suspension and forfeiture of unverified tokens.
        
        3. Intellectual Property
        The MOBITIK trademark, logo emblem, consensus protocols, and software are the exclusive intellectual property of MOBITIK Network.
        """.trimIndent(),

        // Mining Rules
        """
        1. 12-Hour Cycle
        Mining sessions require manual initiation every 12 hours. Auto-restart is optional but requires explicit opt-in.
        
        2. Base Reward
        The base reward is 0.22 MOBITIK per 12-hour session. Completed sessions must be claimed to finalize balance accounting.
        
        3. Fair Play
        Manipulating device system clocks or attempting replay attacks against API nonces will trigger automatic security freezes.
        """.trimIndent(),

        // Risk Disclosure
        """
        1. Digital Asset Volatility
        Cryptocurrency tokens are subject to high market volatility. Early-stage distribution tokens carry risk of valuation fluctuations.
        
        2. Regulatory Landscape
        Blockchain regulations vary by jurisdiction. You are responsible for ensuring compliance with your local laws.
        
        3. No Guaranteed Value
        MOBITIK does not guarantee token price, exchange listings, or financial returns.
        """.trimIndent(),

        // Token Disclaimer
        """
        MOBITIK tokens generated in mobile proof-of-presence sessions represent ecosystem utility tokens in alpha development.
        
        Tokens are NOT shares, equity, securities, or financial derivatives of any corporate entity. MOBITIK makes no representation of guaranteed capital growth or investment returns.
        """.trimIndent()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Legal & Policies", fontWeight = FontWeight.Bold) },
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
        modifier = modifier.testTag("legal_screen")
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = tabs[selectedTab],
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MobitikOrange
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = contents[selectedTab],
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
