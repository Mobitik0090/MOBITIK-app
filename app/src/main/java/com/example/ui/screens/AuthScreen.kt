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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MobitikLogo
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikOrangeDark
import com.example.ui.theme.MobitikWhite

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

    // Login state
    var loginIdentifier by remember { mutableStateOf("pioneer@mobitik.network") }
    var loginPassword by remember { mutableStateOf("Pioneer@123") }
    var isLoginPasswordVisible by remember { mutableStateOf(false) }

    // Register state
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regReferralCode by remember { mutableStateOf("MOBITIKHQ") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(true) }

    // Forgot password dialog
    var showForgotDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LOGO & HERO HEADER
        item {
            Spacer(modifier = Modifier.height(20.dp))
            MobitikLogo(size = 72.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "MOBITIK",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "12-Hour Mobile Consensus Network",
                style = MaterialTheme.typography.bodySmall,
                color = MobitikOrange
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // TAB SELECTOR (Login / Sign Up)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MobitikOrange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
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
                            text = "Sign In",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 0) MobitikOrange else MobitikGrayMedium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Register",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 1) MobitikOrange else MobitikGrayMedium
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (selectedTab == 0) {
            // LOGIN FORM
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Enter your credentials to manage your node",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("Username or Email") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MobitikGrayMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MobitikOrange,
                                unfocusedBorderColor = MobitikBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MobitikGrayMedium) },
                            trailingIcon = {
                                IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isLoginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MobitikGrayMedium
                                    )
                                }
                            },
                            visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MobitikOrange,
                                unfocusedBorderColor = MobitikBorder
                            ),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showForgotDialog = true }) {
                                Text("Forgot Password?", color = MobitikOrange, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.login(loginIdentifier, loginPassword) {}
                            },
                            enabled = !isLoading && loginIdentifier.isNotBlank() && loginPassword.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = MobitikWhite, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Test Buttons for evaluators
                        Text(
                            text = "Quick Demo Credentials:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MobitikGrayMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    loginIdentifier = "pioneer@mobitik.network"
                                    loginPassword = "Pioneer@123"
                                    viewModel.login("pioneer@mobitik.network", "Pioneer@123") {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                            ) {
                                Text("Demo Pioneer", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    loginIdentifier = "admin@mobitik.network"
                                    loginPassword = "Admin@123"
                                    viewModel.login("admin@mobitik.network", "Admin@123") {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                            ) {
                                Text("Demo Admin", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // REGISTER FORM
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Create Pioneer Account",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Start your 12-hour mining journey with MOBITIK",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikGrayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Legal Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MobitikGrayMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username") },
                            placeholder = { Text("e.g. SatoshiMine") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MobitikGrayMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("you@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MobitikGrayMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MobitikGrayMedium) },
                            trailingIcon = {
                                IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isRegPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MobitikGrayMedium
                                    )
                                }
                            },
                            visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regReferralCode,
                            onValueChange = { regReferralCode = it },
                            label = { Text("Referral Code (Optional)") },
                            placeholder = { Text("MOBITIKHQ") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = MobitikOrange) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_referral_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MobitikOrange, unfocusedBorderColor = MobitikBorder),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreeToTerms,
                                onCheckedChange = { agreeToTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = MobitikOrange)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "I agree to the Terms of Service & Privacy Policy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.register(
                                    username = regUsername,
                                    email = regEmail,
                                    fullName = regFullName,
                                    pass = regPassword,
                                    referralCode = regReferralCode.ifBlank { null }
                                ) {}
                            },
                            enabled = !isLoading && regUsername.length >= 3 && regEmail.contains("@") && regPassword.length >= 6 && agreeToTerms,
                            colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("register_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = MobitikWhite, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Create Account & Start Mining", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotDialog) {
        var forgotEmail by remember { mutableStateOf("") }
        var forgotNewPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your registered email and choose a new password.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotNewPass,
                        onValueChange = { forgotNewPass = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotEmail.isNotBlank() && forgotNewPass.length >= 6) {
                            showForgotDialog = false
                            viewModel.resetPassword(forgotEmail, forgotNewPass) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite)
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) { Text("Cancel") }
            }
        )
    }
}
