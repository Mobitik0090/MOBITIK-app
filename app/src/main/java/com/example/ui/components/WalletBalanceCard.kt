package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikOrangeDark
import com.example.ui.theme.MobitikOrangeLight
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikWhite
import java.util.Locale

/**
 * Reusable Wallet Component
 * Displays the user's current accumulated tokens, formatted balance string,
 * breakdown of mining & referral yields, and a prominent simulated 'Swap' shortcut button.
 */
@Composable
fun WalletBalanceCard(
    totalBalance: Double,
    availableBalance: Double,
    totalMiningRewards: Double,
    totalReferralRewards: Double,
    walletAddress: String? = null,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSendClick: (() -> Unit)? = null,
    onReceiveClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Pulsing gradient glow for simulated swap badge
    val infiniteTransition = rememberInfiniteTransition(label = "wallet_badge_anim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Formatted balance strings
    val formattedTotal = String.format(Locale.US, "%.4f", totalBalance)
    val formattedAvailable = String.format(Locale.US, "%.4f", availableBalance)
    val formattedMining = String.format(Locale.US, "%.4f", totalMiningRewards)
    val formattedReferral = String.format(Locale.US, "%.4f", totalReferralRewards)
    val simulatedUsdValue = String.format(Locale.US, "%.2f", totalBalance * 0.15) // $0.15 per MOBITIK

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wallet_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Label, Icon, and Simulated Liquidity Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MobitikOrange.copy(alpha = 0.18f))
                            .border(1.dp, MobitikOrange.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MobitikOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Accumulated Balance",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MobitikGrayMedium
                    )
                }

                // Simulated DEX Alpha Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MobitikOrange.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = glowAlpha))
                ) {
                    Text(
                        text = "DEX POOL v1",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = MobitikOrange,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Formatted Balance String
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formattedTotal,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MOBITIK",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MobitikOrange,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "≈ $$simulatedUsdValue USD (Simulated DEX Rate 1 MBTK = $0.15)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MobitikGrayMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Token Yield Accumulation Breakdown (Mining vs Referral vs Unlocked)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MobitikBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mining Yield", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    Text(
                        text = "+$formattedMining",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MobitikSuccess
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(MobitikBorder)
                )

                Column {
                    Text("Referral Yield", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    Text(
                        text = "+$formattedReferral",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MobitikOrange
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(MobitikBorder)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("Available", style = MaterialTheme.typography.labelSmall, color = MobitikGrayMedium)
                    Text(
                        text = formattedAvailable,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Wallet Address Preview (if provided)
            if (!walletAddress.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Address: ${walletAddress.take(12)}...${walletAddress.takeLast(6)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MOBITIK Address", walletAddress)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Address",
                            tint = MobitikOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Simulated Swap Shortcut + Optional Send/Receive
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PRIMARY SIMULATED 'SWAP' SHORTCUT BUTTON
                Button(
                    onClick = onSwapClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("wallet_swap_shortcut_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MobitikOrange,
                        contentColor = MobitikWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Swap Tokens",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "Simulated DEX",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MobitikWhite.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                if (onSendClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSendClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("wallet_send_shortcut_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = MobitikOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                if (onReceiveClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onReceiveClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("wallet_receive_shortcut_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = MobitikOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receive", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
