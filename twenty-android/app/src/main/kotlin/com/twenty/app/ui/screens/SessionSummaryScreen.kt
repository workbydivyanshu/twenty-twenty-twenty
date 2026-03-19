package com.twenty.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twenty.app.data.Session
import com.twenty.app.domain.SessionStats
import com.twenty.app.domain.formatDurationLong
import com.twenty.app.ui.components.GlassCard
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.Danger
import com.twenty.app.ui.theme.GlassBackground
import com.twenty.app.ui.theme.Success
import com.twenty.app.ui.theme.TextMuted
import com.twenty.app.ui.theme.TextPrimary
import com.twenty.app.ui.theme.TextSecondary
import com.twenty.app.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionSummaryScreen(
    session: Session,
    onDone: () -> Unit,
    onShare: () -> Unit
) {
    val totalBreaks = session.breaksTaken + session.breaksSkipped

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Success, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", style = MaterialTheme.typography.headlineLarge, color = Color.Black)
            }
            Spacer(Modifier.height(16.dp))
            Text("Session Complete", style = MaterialTheme.typography.headlineLarge)
            Text("Great work taking care of your eyes", color = TextMuted)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(formatDurationLong(session.durationMs), style = MaterialTheme.typography.headlineLarge)
                        Text("Total Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(session.breaksTaken.toString(), style = MaterialTheme.typography.headlineLarge, color = Success)
                        Text("Breaks Taken", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(session.breaksSkipped.toString(), style = MaterialTheme.typography.headlineLarge, color = TextMuted)
                        Text("Breaks Skipped", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${(session.complianceRate * 100).toInt()}%", style = MaterialTheme.typography.headlineLarge, color = AccentPrimary)
                        Text("Compliance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        if (totalBreaks > 0) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Compliance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${(session.complianceRate * 100).toInt()}%", color = AccentPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { session.complianceRate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentPrimary,
                            trackColor = Color(0x1Affffff),
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Share as Image")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) {
                    Text("Done")
                }
            }
        }
    }
}
