package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikError
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikOrangeDark
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToWhitepaper: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val user = currentUser ?: return
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // PROFILE HERO CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MobitikOrangeDark)
                            .border(2.dp, MobitikOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.take(1).uppercase(),
                            color = MobitikWhite,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.fullName.ifBlank { user.username },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MobitikOrange
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (user.role == "ADMIN") MobitikOrange.copy(alpha = 0.2f) else MobitikSuccess.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (user.role == "ADMIN") "ADMINISTRATOR" else "VERIFIED PIONEER",
                            color = if (user.role == "ADMIN") MobitikOrange else MobitikSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ID & Address badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("User ID", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                            Text(user.id, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Member Since", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                            Text(sdf.format(Date(user.registeredAt)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // QUICK ACCOUNT SWITCHER (Admin <-> Pioneer)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val switchToAdmin = user.role != "ADMIN"
                        viewModel.switchAccount(toAdmin = switchToAdmin)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MobitikOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Switch Account Role",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (user.role == "ADMIN") "Currently: Admin → Switch to Pioneer" else "Currently: Pioneer → Switch to Admin",
                                style = MaterialTheme.typography.labelSmall,
                                color = MobitikGrayMedium
                            )
                        }
                    }

                    Text(
                        text = "Switch",
                        color = MobitikOrange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // IF ADMIN: ADMIN DASHBOARD BUTTON
        if (user.role == "ADMIN") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdmin() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MobitikOrangeDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = MobitikWhite, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Admin Control Dashboard",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MobitikWhite
                                )
                                Text(
                                    text = "Manage mining params, users, and tokens",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MobitikWhite.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MobitikWhite, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // SETTINGS & SECURITY SECTION
        item {
            Text(
                text = "Account Security & Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Brightness4,
                        title = "Dark Theme",
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { viewModel.toggleTheme() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MobitikWhite,
                                    checkedTrackColor = MobitikOrange
                                )
                            )
                        }
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.Mail,
                        title = "Email Verification",
                        subtitle = if (user.isEmailVerified) "Verified (${user.email})" else "Pending Verification",
                        trailing = {
                            Text(
                                text = if (user.isEmailVerified) "Verified" else "Verify Now",
                                color = if (user.isEmailVerified) MobitikSuccess else MobitikOrange,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = {
                            if (!user.isEmailVerified) {
                                viewModel.showMessage("Verification link simulated sent to ${user.email}!")
                            }
                        }
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Two-Factor Authentication",
                        subtitle = if (user.isTwoFactorEnabled) "Enabled (2FA)" else "Disabled",
                        trailing = {
                            Switch(
                                checked = user.isTwoFactorEnabled,
                                onCheckedChange = {
                                    // Toggle 2fa
                                    viewModel.showMessage("2FA status updated")
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MobitikWhite,
                                    checkedTrackColor = MobitikOrange
                                )
                            )
                        }
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Trust Wallet Auto-Withdrawal",
                        subtitle = if (!user.trustWalletAddress.isNullOrBlank())
                            "Linked: ${user.trustWalletAddress.take(6)}...${user.trustWalletAddress.takeLast(4)} (Min: ${"%.1f".format(user.autoWithdrawalThreshold)} MBTK)"
                        else "Not connected • Tap to configure",
                        trailing = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (user.isAutoWithdrawalEnabled) MobitikSuccess.copy(alpha = 0.2f)
                                        else if (!user.trustWalletAddress.isNullOrBlank()) MobitikOrange.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (user.isAutoWithdrawalEnabled) "ACTIVE"
                                           else if (!user.trustWalletAddress.isNullOrBlank()) "LINKED"
                                           else "OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (user.isAutoWithdrawalEnabled) MobitikSuccess
                                            else if (!user.trustWalletAddress.isNullOrBlank()) MobitikOrange
                                            else MobitikGrayMedium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        },
                        onClick = {
                            viewModel.showMessage(
                                if (!user.trustWalletAddress.isNullOrBlank()) "Manage Trust Wallet in Wallet tab > Trust Wallet"
                                else "Please open Wallet tab > Trust Wallet to bind your BEP-20 address"
                            )
                        }
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        onClick = { showPasswordDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // RESOURCES & SUPPORT SECTION
        item {
            Text(
                text = "Resources & Information",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Description,
                        title = "Whitepaper",
                        subtitle = "Vision, Consensus & Tokenomics",
                        onClick = onNavigateToWhitepaper
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.Help,
                        title = "Frequently Asked Questions",
                        subtitle = "17 common questions & answers",
                        onClick = onNavigateToFaq
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.Gavel,
                        title = "Legal & Privacy Policy",
                        subtitle = "Terms, Mining Rules & Disclaimers",
                        onClick = onNavigateToLegal
                    )
                    HorizontalDivider(color = MobitikBorder)

                    ProfileMenuRow(
                        icon = Icons.Default.SupportAgent,
                        title = "Contact Support",
                        subtitle = "Submit & track support tickets",
                        onClick = onNavigateToSupport
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // DANGER ZONE (Logout & Account Deletion)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.ExitToApp,
                        title = "Logout",
                        titleColor = MobitikOrange,
                        onClick = { viewModel.logout() }
                    )
                    HorizontalDivider(color = MobitikBorder)
                    ProfileMenuRow(
                        icon = Icons.Default.Delete,
                        title = "Request Account Deletion",
                        titleColor = MobitikError,
                        onClick = { showDeleteDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length >= 6 && newPass == confirmPass) {
                            showPasswordDialog = false
                            viewModel.resetPassword(user.email, newPass) {}
                        } else {
                            viewModel.showMessage("Passwords must match and be at least 6 characters")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite)
                ) {
                    Text("Save Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account", fontWeight = FontWeight.Bold, color = MobitikError) },
            text = {
                Text("Are you sure you want to request account deletion? All unmined rewards and local credentials will be scheduled for permanent erasure.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.showMessage("Deletion request submitted to administrators.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikError, contentColor = MobitikWhite)
                ) {
                    Text("Confirm Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = titleColor
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                }
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MobitikGrayMedium,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
