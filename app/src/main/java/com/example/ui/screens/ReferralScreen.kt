package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
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
fun ReferralScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val referredUsers by viewModel.referredUsers.collectAsState()
    val allUsers by viewModel.adminAllUsers.collectAsState()

    val user = currentUser ?: return
    val referralLink = "https://mobitik.network/join?ref=${user.referralCode}"

    val activeCount = referredUsers.count { it.status == "ACTIVE" }
    val bonusRate = activeCount * 0.00458 // +25% boost per teammate

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("referral_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // TOP BANNER
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
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Referral Team",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Boost your mining rate by inviting friends",
                                style = MaterialTheme.typography.bodySmall,
                                color = MobitikGrayMedium
                            )
                        }

                        // Circular Active Count Indicator (from Zai/Pi design screenshot)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(2.dp, MobitikOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$activeCount",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MobitikOrange
                                )
                                Text(
                                    text = "Mining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MobitikGrayMedium,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You have invited ${referredUsers.size} Pioneers. Your mining rate is boosted by +${"%.4f".format(bonusRate)} MOBITIK/h for every actively mining member.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ping Inactive Button (matches user's screenshot request)
                    Button(
                        onClick = { viewModel.pingInactiveReferrals() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MobitikOrange,
                            contentColor = MobitikWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("ping_inactive_button")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ping Inactive Pioneers",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // REFERRAL CODE & LINK CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Your Invitation Code",
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MobitikOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.referralCode,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = MobitikOrange
                        )

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("MOBITIK Code", user.referralCode)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showMessage("Referral code copied!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MobitikOrange, contentColor = MobitikWhite),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Code")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Share button
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join my MOBITIK Mining Team")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Mine MOBITIK cryptocurrency every 12 hours from your phone! Use my referral code ${user.referralCode} to get a mining speed boost: $referralLink"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Invitation"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MobitikOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Personal Invite Link")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // STATS SUMMARY ROW
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Total Invited",
                    value = "${referredUsers.size}",
                    subtitle = "Team Size",
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Referral Rewards",
                    value = "%.3f".format(user.totalReferralRewards),
                    subtitle = "MOBITIK Earned",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f),
                    isHighlighted = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // TEAM MEMBERS LIST
        item {
            Text(
                text = "Your Team Members (${referredUsers.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        if (referredUsers.isEmpty()) {
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
                            text = "No one has joined your referral team yet!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MobitikGrayMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share your referral code to invite friends and multiply your earnings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobitikOrange,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(referredUsers.size) { index ->
                val member = referredUsers[index]
                TeamMemberRow(member)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // LEADERBOARD SECTION
        item {
            Text(
                text = "Top Pioneers Leaderboard",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val sortedUsers = allUsers.sortedByDescending { it.totalBalance }.take(5)
            sortedUsers.forEachIndexed { rank, topUser ->
                LeaderboardRow(rank = rank + 1, topUser = topUser)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun TeamMemberRow(member: User) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val isMining = member.status == "ACTIVE"

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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MobitikOrangeDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.username.take(1).uppercase(),
                        color = MobitikWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = member.username,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Joined ${sdf.format(Date(member.registeredAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    status = if (isMining) "MINING" else "IDLE",
                    isPositive = isMining
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${member.successfulSessionsCount} Sessions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MobitikGrayMedium
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, topUser: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MobitikBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = when (rank) {
                        1 -> MobitikOrange
                        2 -> MobitikOrangeDark
                        else -> MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MobitikWhite
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = topUser.username,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = topUser.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = MobitikGrayMedium
                    )
                }
            }

            Text(
                text = "${"%.2f".format(topUser.totalBalance)} MOBITIK",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MobitikOrange
            )
        }
    }
}
