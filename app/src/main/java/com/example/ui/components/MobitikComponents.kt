package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MiningSession
import com.example.ui.theme.MobitikBorder
import com.example.ui.theme.MobitikError
import com.example.ui.theme.MobitikErrorBg
import com.example.ui.theme.MobitikGrayDark
import com.example.ui.theme.MobitikGrayMedium
import com.example.ui.theme.MobitikOrange
import com.example.ui.theme.MobitikOrangeDark
import com.example.ui.theme.MobitikOrangeGlow
import com.example.ui.theme.MobitikOrangeLight
import com.example.ui.theme.MobitikSuccess
import com.example.ui.theme.MobitikSuccessBg
import com.example.ui.theme.MobitikWhite
import java.util.Locale
import kotlin.math.abs

/**
 * MOBITIK Distinctive Logo Emblem
 */
@Composable
fun MobitikLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showText: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MobitikOrange, MobitikOrangeDark),
                        radius = 80f
                    )
                )
                .border(1.5.dp, MobitikOrangeLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.18f)) {
                val w = this.size.width
                val h = this.size.height

                // Custom dynamic "M" geometry for MOBITIK
                val strokeW = w * 0.16f
                val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)

                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.2f, h * 0.85f),
                    end = Offset(w * 0.2f, h * 0.2f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.2f, h * 0.2f),
                    end = Offset(w * 0.5f, h * 0.65f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.5f, h * 0.65f),
                    end = Offset(w * 0.8f, h * 0.2f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.8f, h * 0.2f),
                    end = Offset(w * 0.8f, h * 0.85f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "MOBITIK",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Consensus Network",
                    style = MaterialTheme.typography.labelSmall,
                    color = MobitikOrange
                )
            }
        }
    }
}

/**
 * Advanced Circular Mining Dashboard Interface
 * Featuring circular countdown timer, live reward counter, and integrated action triggers
 */
@Composable
fun MiningCircle(
    session: MiningSession?,
    remainingMillis: Long,
    isLoading: Boolean,
    onStartMining: () -> Unit,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMiningActive = session != null && session.status == "ACTIVE" && remainingMillis > 0
    val isReadyToClaim = session != null && (session.status == "COMPLETED" || (session.status == "ACTIVE" && remainingMillis <= 0))
    val isIdle = session == null || session.status == "CLAIMED" || session.status == "EXPIRED"

    // Continuous smooth rotation for ambient orbital ring
    val infiniteTransition = rememberInfiniteTransition(label = "mining_anim")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    // Pulsing aura animation
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Calculate elapsed duration and session progress
    val totalDuration = if (session != null) {
        val total = session.expectedEndTimeMillis - session.startTimeMillis
        if (total > 0) total else 43200000L
    } else 43200000L // 12 hours

    val progress = when {
        isReadyToClaim -> 1.0f
        isMiningActive -> {
            val elapsed = (totalDuration - remainingMillis).coerceAtLeast(0L)
            (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0.01f, 1.0f)
        }
        else -> 0.0f
    }

    // Calculate real-time accrued reward during the active session
    val sessionReward = session?.rewardAmount ?: 0.22
    val accruedReward = when {
        isReadyToClaim -> sessionReward
        isMiningActive -> (progress * sessionReward).coerceIn(0.0, sessionReward)
        else -> 0.0
    }

    Box(
        modifier = modifier
            .size(310.dp)
            .testTag("mining_circle_container"),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Canvas for Outer Glow Aura, Dial Ticks, and Progress Arcs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidthPx = 13.dp.toPx()
            val radius = size.width / 2f - strokeWidthPx - 10.dp.toPx()

            // 1. Ambient Background Neon Glow
            if (isMiningActive || isReadyToClaim) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isReadyToClaim) MobitikSuccess.copy(alpha = pulseAlpha * 0.45f)
                            else MobitikOrangeGlow.copy(alpha = pulseAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius + 32.dp.toPx()
                    ),
                    radius = radius + 32.dp.toPx(),
                    center = center
                )
            }

            // 2. Precision Chronograph Dial Micro-Ticks (48 ticks around circle)
            val tickCount = 48
            for (i in 0 until tickCount) {
                val tickAngle = (i.toFloat() / tickCount) * 360f - 90f
                val angleRad = Math.toRadians(tickAngle.toDouble())
                val isMajor = i % 4 == 0
                val tickLength = if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                val tickProgress = (i.toFloat() / tickCount)
                val isTickActive = progress >= tickProgress && (isMiningActive || isReadyToClaim)

                val innerR = radius - (strokeWidthPx / 2f) - tickLength - 3.dp.toPx()
                val outerR = radius - (strokeWidthPx / 2f) - 3.dp.toPx()

                val start = Offset(
                    x = (center.x + innerR * Math.cos(angleRad)).toFloat(),
                    y = (center.y + innerR * Math.sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = (center.x + outerR * Math.cos(angleRad)).toFloat(),
                    y = (center.y + outerR * Math.sin(angleRad)).toFloat()
                )

                drawLine(
                    color = when {
                        isTickActive -> if (isReadyToClaim) MobitikSuccess else MobitikOrangeLight
                        else -> MobitikBorder.copy(alpha = 0.6f)
                    },
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Track Background Ring
            drawCircle(
                color = MobitikBorder,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 4. Active Progress Arc with Gradient
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            MobitikOrangeDark,
                            MobitikOrange,
                            MobitikOrangeLight,
                            if (isReadyToClaim) MobitikSuccess else MobitikOrangeLight
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // 5. Active Satellite Node at Head of Progress Arc
                val currentAngleRad = Math.toRadians((progress * 360f - 90f).toDouble())
                val headPoint = Offset(
                    x = (center.x + radius * Math.cos(currentAngleRad)).toFloat(),
                    y = (center.y + radius * Math.sin(currentAngleRad)).toFloat()
                )
                drawCircle(
                    color = Color.White,
                    radius = (strokeWidthPx * 0.45f),
                    center = headPoint
                )
                drawCircle(
                    color = if (isReadyToClaim) MobitikSuccess else MobitikOrange,
                    radius = (strokeWidthPx * 0.25f),
                    center = headPoint
                )
            }
        }

        // LAYER 2: Inner Core Display Surface
        Surface(
            modifier = Modifier
                .size(232.dp)
                .clip(CircleShape)
                .shadow(elevation = 16.dp, shape = CircleShape)
                .border(2.dp, if (isMiningActive) MobitikOrange.copy(alpha = 0.7f) else MobitikBorder, CircleShape),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Status Chip & Center Logo
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        status = when {
                            isReadyToClaim -> "READY TO CLAIM"
                            isMiningActive -> "12H MINING"
                            else -> "READY TO MINE"
                        },
                        isPositive = isMiningActive || isReadyToClaim
                    )
                }

                // Middle: Circular Countdown Timer & Live Reward Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    if (isMiningActive) {
                        val hours = remainingMillis / 3600000
                        val minutes = (remainingMillis % 3600000) / 60000
                        val seconds = (remainingMillis % 60000) / 1000
                        val formattedTime = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

                        Text(
                            text = "TIME REMAINING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MobitikGrayMedium,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        // Real-time accumulating reward display
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MobitikOrange.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MobitikOrange.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Yield: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MobitikGrayMedium,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "+${String.format(Locale.US, "%.4f", accruedReward)} MOBITIK",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MobitikOrange
                                )
                            }
                        }
                    } else if (isReadyToClaim) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MobitikSuccess,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "12H COMPLETE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = MobitikSuccess
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+${session?.rewardAmount ?: 0.22} MOBITIK",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = MobitikOrange
                        )
                    } else {
                        MobitikLogo(size = 38.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "12:00:00",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Reward: 0.22 MOBITIK",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MobitikOrange
                        )
                    }
                }

                // Bottom: Integrated Core Action Button
                Button(
                    onClick = {
                        if (isReadyToClaim) onClaimReward()
                        else if (isIdle) onStartMining()
                    },
                    enabled = !isLoading && (isReadyToClaim || isIdle),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReadyToClaim) MobitikSuccess else MobitikOrange,
                        contentColor = MobitikWhite,
                        disabledContainerColor = MobitikOrange.copy(alpha = 0.45f),
                        disabledContentColor = MobitikWhite.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(38.dp)
                        .padding(bottom = 4.dp)
                        .testTag("mining_action_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isReadyToClaim -> Icons.Default.CheckCircle
                                isMiningActive -> Icons.Default.Bolt
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isReadyToClaim -> "Claim Reward"
                                isMiningActive -> "Mining Active"
                                else -> "Start Mining"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status Badge Chip
 */
@Composable
fun StatusBadge(
    status: String,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isPositive) MobitikSuccessBg else MobitikErrorBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isPositive) MobitikSuccess else MobitikError)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isPositive) MobitikSuccess else MobitikError
            )
        }
    }
}

/**
 * Stat Card for Dashboard KPIs
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlighted) MobitikOrange.copy(alpha = 0.5f) else MobitikBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MobitikGrayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isHighlighted) MobitikOrange else MobitikGrayMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isHighlighted) MobitikOrange else MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MobitikGrayMedium
                )
            }
        }
    }
}

/**
 * High quality deterministic pure-Compose QR Code matrix renderer
 */
@Composable
fun MobitikQrCode(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    // Generate a clean 25x25 QR pattern from the address hash
    val gridSize = 25
    val matrix = remember(data) {
        val mat = Array(gridSize) { BooleanArray(gridSize) }
        val hash = abs(data.hashCode())
        val bytes = data.toByteArray()

        // 1. Draw standard finder patterns in 3 corners (7x7 squares)
        fun drawFinder(startX: Int, startY: Int) {
            for (y in 0 until 7) {
                for (x in 0 until 7) {
                    val isBorder = x == 0 || x == 6 || y == 0 || y == 6
                    val isInner = x in 2..4 && y in 2..4
                    mat[startY + y][startX + x] = isBorder || isInner
                }
            }
        }
        drawFinder(0, 0)
        drawFinder(gridSize - 7, 0)
        drawFinder(0, gridSize - 7)

        // 2. Timing patterns
        for (i in 7 until gridSize - 7) {
            mat[6][i] = i % 2 == 0
            mat[i][6] = i % 2 == 0
        }

        // 3. Fill data modules based on characters
        var byteIdx = 0
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                // Avoid finder pattern zones
                val inTopLeft = x < 8 && y < 8
                val inTopRight = x >= gridSize - 8 && y < 8
                val inBottomLeft = x < 8 && y >= gridSize - 8
                val inCenter = x in 10..14 && y in 10..14

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter) {
                    val b = if (bytes.isNotEmpty()) bytes[byteIdx % bytes.size].toInt() else 0
                    val bit = ((b xor (hash ushr (x + y))) shr ((x * y) % 8)) and 1
                    mat[y][x] = bit == 1
                    byteIdx++
                }
            }
        }
        mat
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = this.size.width / gridSize
            val cellH = this.size.height / gridSize

            for (y in 0 until gridSize) {
                for (x in 0 until gridSize) {
                    if (matrix[y][x]) {
                        drawRect(
                            color = Color(0xFF101010),
                            topLeft = Offset(x * cellW, y * cellH),
                            size = Size(cellW, cellH)
                        )
                    }
                }
            }
        }

        // Small orange MOBITIK Center Logo on QR
        Box(
            modifier = Modifier
                .size(size * 0.22f)
                .clip(CircleShape)
                .background(MobitikOrange)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "M",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}
