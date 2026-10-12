package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FaqScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.NotificationCenterDialog
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.WhitepaperScreen
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikWhite
import com.example.ui.theme.MyApplicationTheme

enum class MainDestination {
    HOME, WALLET, REFERRALS, HISTORY, PROFILE
}

enum class SubDestination {
    NONE, WHITEPAPER, FAQ, LEGAL, SUPPORT, ADMIN
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            val snackbarMessage by viewModel.snackbarMessage.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearSnackbar()
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                var currentTab by rememberSaveable { mutableStateOf(MainDestination.HOME) }
                var currentSubScreen by rememberSaveable { mutableStateOf(SubDestination.NONE) }
                var walletInitialTab by rememberSaveable { mutableIntStateOf(0) }
                var showNotificationCenter by remember { mutableStateOf(false) }

                // Handle Android system back button
                if (currentSubScreen != SubDestination.NONE) {
                    BackHandler {
                        currentSubScreen = SubDestination.NONE
                    }
                } else if (currentTab != MainDestination.HOME) {
                    BackHandler {
                        currentTab = MainDestination.HOME
                    }
                }

                if (currentUser == null) {
                    // AUTHENTICATION SCREEN
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        AuthScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    // MAIN APP EXPERIENCE
                    if (currentSubScreen != SubDestination.NONE) {
                        // Sub-screens (Whitepaper, FAQ, Legal, Support, Admin)
                        when (currentSubScreen) {
                            SubDestination.WHITEPAPER -> WhitepaperScreen(onBack = { currentSubScreen = SubDestination.NONE })
                            SubDestination.FAQ -> FaqScreen(viewModel = viewModel, onBack = { currentSubScreen = SubDestination.NONE })
                            SubDestination.LEGAL -> LegalScreen(onBack = { currentSubScreen = SubDestination.NONE })
                            SubDestination.SUPPORT -> SupportScreen(viewModel = viewModel, onBack = { currentSubScreen = SubDestination.NONE })
                            SubDestination.ADMIN -> AdminScreen(viewModel = viewModel, onBack = { currentSubScreen = SubDestination.NONE })
                            SubDestination.NONE -> {}
                        }
                    } else {
                        // Primary Tabs (Home, Wallet, Referrals, History, Profile)
                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .testTag("main_bottom_nav"),
                                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == MainDestination.HOME,
                                        onClick = { currentTab = MainDestination.HOME },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == MainDestination.HOME) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = { Text("Home", fontWeight = if (currentTab == MainDestination.HOME) FontWeight.Bold else FontWeight.Normal) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MobitikOrange,
                                            selectedTextColor = MobitikOrange,
                                            indicatorColor = MobitikOrange.copy(alpha = 0.2f),
                                            unselectedIconColor = MobitikGrayMedium,
                                            unselectedTextColor = MobitikGrayMedium
                                        ),
                                        modifier = Modifier.testTag("nav_home")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == MainDestination.WALLET,
                                        onClick = { currentTab = MainDestination.WALLET },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == MainDestination.WALLET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = "Wallet"
                                            )
                                        },
                                        label = { Text("Wallet", fontWeight = if (currentTab == MainDestination.WALLET) FontWeight.Bold else FontWeight.Normal) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MobitikOrange,
                                            selectedTextColor = MobitikOrange,
                                            indicatorColor = MobitikOrange.copy(alpha = 0.2f),
                                            unselectedIconColor = MobitikGrayMedium,
                                            unselectedTextColor = MobitikGrayMedium
                                        ),
                                        modifier = Modifier.testTag("nav_wallet")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == MainDestination.REFERRALS,
                                        onClick = { currentTab = MainDestination.REFERRALS },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == MainDestination.REFERRALS) Icons.Filled.Group else Icons.Outlined.Group,
                                                contentDescription = "Referrals"
                                            )
                                        },
                                        label = { Text("Referrals", fontWeight = if (currentTab == MainDestination.REFERRALS) FontWeight.Bold else FontWeight.Normal) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MobitikOrange,
                                            selectedTextColor = MobitikOrange,
                                            indicatorColor = MobitikOrange.copy(alpha = 0.2f),
                                            unselectedIconColor = MobitikGrayMedium,
                                            unselectedTextColor = MobitikGrayMedium
                                        ),
                                        modifier = Modifier.testTag("nav_referrals")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == MainDestination.HISTORY,
                                        onClick = { currentTab = MainDestination.HISTORY },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == MainDestination.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                                contentDescription = "History"
                                            )
                                        },
                                        label = { Text("History", fontWeight = if (currentTab == MainDestination.HISTORY) FontWeight.Bold else FontWeight.Normal) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MobitikOrange,
                                            selectedTextColor = MobitikOrange,
                                            indicatorColor = MobitikOrange.copy(alpha = 0.2f),
                                            unselectedIconColor = MobitikGrayMedium,
                                            unselectedTextColor = MobitikGrayMedium
                                        ),
                                        modifier = Modifier.testTag("nav_history")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == MainDestination.PROFILE,
                                        onClick = { currentTab = MainDestination.PROFILE },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == MainDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                                contentDescription = "Profile"
                                            )
                                        },
                                        label = { Text("Profile", fontWeight = if (currentTab == MainDestination.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MobitikOrange,
                                            selectedTextColor = MobitikOrange,
                                            indicatorColor = MobitikOrange.copy(alpha = 0.2f),
                                            unselectedIconColor = MobitikGrayMedium,
                                            unselectedTextColor = MobitikGrayMedium
                                        ),
                                        modifier = Modifier.testTag("nav_profile")
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    MainDestination.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToWalletTab = {
                                            walletInitialTab = 0
                                            currentTab = MainDestination.WALLET
                                        },
                                        onNavigateToWalletSwap = {
                                            walletInitialTab = 3
                                            currentTab = MainDestination.WALLET
                                        },
                                        onNavigateToTrustWallet = {
                                            walletInitialTab = 4
                                            currentTab = MainDestination.WALLET
                                        },
                                        onNavigateToReferralsTab = { currentTab = MainDestination.REFERRALS },
                                        onNavigateToHistoryTab = { currentTab = MainDestination.HISTORY },
                                        onOpenNotifications = { showNotificationCenter = true },
                                        onOpenProfile = { currentTab = MainDestination.PROFILE }
                                    )
                                    MainDestination.WALLET -> WalletScreen(viewModel = viewModel, initialTab = walletInitialTab)
                                    MainDestination.REFERRALS -> ReferralScreen(viewModel = viewModel)
                                    MainDestination.HISTORY -> HistoryScreen(viewModel = viewModel)
                                    MainDestination.PROFILE -> ProfileScreen(
                                        viewModel = viewModel,
                                        onNavigateToWhitepaper = { currentSubScreen = SubDestination.WHITEPAPER },
                                        onNavigateToFaq = { currentSubScreen = SubDestination.FAQ },
                                        onNavigateToLegal = { currentSubScreen = SubDestination.LEGAL },
                                        onNavigateToSupport = { currentSubScreen = SubDestination.SUPPORT },
                                        onNavigateToAdmin = { currentSubScreen = SubDestination.ADMIN }
                                    )
                                }
                            }
                        }
                    }

                    // In-App Notification Center Modal
                    if (showNotificationCenter) {
                        NotificationCenterDialog(
                            viewModel = viewModel,
                            onDismiss = { showNotificationCenter = false }
                        )
                    }
                }
            }
        }
    }
}
