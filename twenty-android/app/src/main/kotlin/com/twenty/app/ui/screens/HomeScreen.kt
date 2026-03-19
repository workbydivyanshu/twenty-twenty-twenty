package com.twenty.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.AccentSecondary
import com.twenty.app.ui.theme.BgBase
import com.twenty.app.ui.theme.Danger
import com.twenty.app.ui.theme.GlassBackground
import com.twenty.app.ui.theme.Success
import com.twenty.app.ui.theme.TextMuted
import com.twenty.app.ui.theme.TextPrimary
import com.twenty.app.ui.theme.TextSecondary
import com.twenty.app.ui.theme.Warning

@Composable
fun BreakOverlayScreen(
    countdown: Int,
    onConfirm: () -> Unit,
    onSkip: () -> Unit,
    onEndSession: () -> Unit
) {
    var showEndSessionDialog by remember { mutableStateOf(false) }
    
    val isCountdownActive = countdown > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isCountdownActive) {
                com.twenty.app.ui.components.CircularProgressTimer(
                    progress = countdown / 20f,
                    countdown = countdown
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Look at something",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "20 feet away",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AccentPrimary
                )
                Text(
                    text = "for 20 seconds",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Rest your eyes · Follow the 20-20-20 rule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                Text(
                    text = "Did you rest your eyes?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Looked at something 20 feet away for 20 seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(Modifier.height(48.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { showEndSessionDialog = true },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("No, I didn't")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("Yes, I did ✓", color = BgBase)
                    }
                }
            }
        }
    }

    if (showEndSessionDialog) {
        AlertDialog(
            onDismissRequest = { showEndSessionDialog = false },
            containerColor = GlassBackground,
            title = { Text("End session?", color = TextPrimary) },
            text = { 
                Text(
                    "Your break will be counted as skipped.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndSessionDialog = false
                        onEndSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Text("End Session")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndSessionDialog = false
                        onSkip()
                    }
                ) {
                    Text("Continue anyway", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SessionBadge(state: String) {
    val (text, color) = when (state) {
        "active" -> "Active Session" to AccentPrimary
        "break" -> "Taking a Break" to Warning
        "summary" -> "Session Complete" to Success
        else -> "Ready" to TextMuted
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .background(GlassBackground, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state == "active" || state == "break") {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color.copy(alpha = pulseAlpha), CircleShape)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
fun BreakStats(taken: Int, skipped: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = taken.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Success
            )
            Text(
                text = " taken",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(Modifier.width(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = skipped.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = TextMuted
            )
            Text(
                text = " skipped",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
