package com.twenty.app.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.twenty.app.data.Session
import com.twenty.app.data.Settings
import com.twenty.app.domain.SessionViewModel
import com.twenty.app.domain.TimerViewModel
import com.twenty.app.domain.formatDurationLong
import com.twenty.app.ui.components.AmbientBackground
import com.twenty.app.ui.components.TopNavBar
import com.twenty.app.ui.screens.BreakOverlayScreen
import com.twenty.app.ui.screens.BreakStats
import com.twenty.app.ui.screens.OnboardingScreen
import com.twenty.app.ui.screens.RecapScreen
import com.twenty.app.ui.screens.SessionBadge
import com.twenty.app.ui.screens.SessionSummaryScreen
import com.twenty.app.ui.screens.SettingsScreen
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.Danger
import com.twenty.app.ui.theme.TextPrimary
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TwentyApp(
    settings: Settings,
    sessions: List<Session>,
    timerViewModel: TimerViewModel,
    sessionViewModel: SessionViewModel,
    onUpdateSettings: (Settings) -> Unit,
    onSaveSessions: (List<Session>) -> Unit,
    onStartSession: () -> Unit,
    onEndSession: () -> Unit,
    onBreakConfirmed: () -> Unit,
    onBreakSkipped: () -> Unit
) {
    var view by remember { mutableIntStateOf(0) }

    val elapsed by timerViewModel.elapsed.collectAsState()
    val isRunning by timerViewModel.isRunning.collectAsState()
    val isBreakActive by timerViewModel.isBreakActive.collectAsState()
    val nextBreakIn by timerViewModel.nextBreakIn.collectAsState()
    val breakCountdown by timerViewModel.breakCountdown.collectAsState()

    val sessionState by sessionViewModel.sessionState.collectAsState()
    val breaksTaken by sessionViewModel.breaksTaken.collectAsState()
    val breaksSkipped by sessionViewModel.breaksSkipped.collectAsState()
    val completedSession by sessionViewModel.completedSession.collectAsState()

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopNavBar(
                    onRecapClick = { view = if (view == 1) 0 else 1 },
                    onSettingsClick = { view = if (view == 2) 0 else 2 },
                    onHomeClick = { view = 0 }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    isBreakActive -> BreakOverlayScreen(
                        countdown = breakCountdown,
                        onConfirm = { sessionViewModel.handleBreakConfirm() },
                        onSkip = { sessionViewModel.handleBreakSkip() },
                        onEndSession = { sessionViewModel.handleEnd(sessions, onSaveSessions) }
                    )

                    completedSession != null -> SessionSummaryScreen(
                        session = completedSession!!,
                        onDone = { sessionViewModel.handleDismissSummary() },
                        onShare = {
                            val session = completedSession!!
                            shareSessionImage(context, session)
                        }
                    )

                    view == 1 -> RecapScreen(sessions = sessions)

                    view == 2 -> SettingsScreen(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings,
                        onExportSessions = { exportSessions(context, sessions) },
                        onClearSessions = { onSaveSessions(emptyList()) }
                    )

                    else -> HomeContent(
                        elapsed = elapsed,
                        isRunning = isRunning,
                        sessionState = sessionState,
                        nextBreakIn = nextBreakIn,
                        breaksTaken = breaksTaken,
                        breaksSkipped = breaksSkipped,
                        onStart = { sessionViewModel.handleStart() },
                        onEnd = { sessionViewModel.handleEnd(sessions, onSaveSessions) }
                    )
                }
            }
        }

        if (!settings.onboardingComplete) {
            OnboardingScreen(
                onComplete = { onUpdateSettings(settings.copy(onboardingComplete = true)) }
            )
        }
    }
}

@Composable
fun HomeContent(
    elapsed: Long,
    isRunning: Boolean,
    sessionState: String,
    nextBreakIn: Int?,
    breaksTaken: Int,
    breaksSkipped: Int,
    onStart: () -> Unit,
    onEnd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = formatTime(elapsed),
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary
        )

        SessionBadge(state = sessionState)

        Spacer(Modifier.height(24.dp))

        BreakStats(taken = breaksTaken, skipped = breaksSkipped)

        if (isRunning && nextBreakIn != null) {
            Text(
                text = "Next break in ${formatNextBreak(nextBreakIn)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { if (isRunning) onEnd() else onStart() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Danger else AccentPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(
                text = if (isRunning) "End Session" else "Start Session",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
    } else {
        "$minutes:${String.format("%02d", seconds)}"
    }
}

fun formatNextBreak(seconds: Int): String {
    return if (seconds >= 60) {
        "${seconds / 60}m ${seconds % 60}s"
    } else {
        "${seconds}s"
    }
}

fun shareSessionImage(context: android.content.Context, session: Session) {
    try {
        val width = 600
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF0a0a14.toInt(), 0xFF0f0f24.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val accentPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                intArrayOf(0xFF818cf8.toInt(), 0xFF6366f1.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 4f, accentPaint)

        val titlePaint = Paint().apply {
            color = 0xFF818cf8.toInt()
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText("Twenty Session Complete", 40f, 70f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = 0xFF94a3b8.toInt()
            textSize = 14f
            isAntiAlias = true
        }
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        canvas.drawText(dateFormat.format(Date(session.startTime)), 40f, 96f, subtitlePaint)

        val cards = listOf(
            Triple("TOTAL TIME", formatDurationLong(session.durationMs), 0xFFf1f5f9.toInt()),
            Triple("BREAKS TAKEN", session.breaksTaken.toString(), 0xFF34d399.toInt()),
            Triple("BREAKS SKIPPED", session.breaksSkipped.toString(), 0xFF64748b.toInt()),
            Triple("COMPLIANCE", "${(session.complianceRate * 100).toInt()}%", 0xFF818cf8.toInt())
        )

        cards.forEachIndexed { i, (label, value, accentColor) ->
            val x = 40 + i * 130
            val y = 140

            val cardPaint = Paint().apply {
                color = 0x0Dffffff
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(x.toFloat(), y.toFloat(), (x + 120).toFloat(), (y + 90).toFloat(), 12f, 12f, cardPaint)

            val valuePaint = Paint().apply {
                this.color = accentColor
                textSize = 26f
                isAntiAlias = true
                isFakeBoldText = true
            }
            canvas.drawText(value, (x + 12).toFloat(), (y + 42).toFloat(), valuePaint)

            val labelPaint = Paint().apply {
                this.color = 0xFF64748b.toInt()
                textSize = 11f
                isAntiAlias = true
            }
            canvas.drawText(label, (x + 12).toFloat(), (y + 66).toFloat(), labelPaint)
        }

        val footerPaint = Paint().apply {
            color = 0xFF475569.toInt()
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("20-20-20 Rule · Twenty ·³", 40f, 370f, footerPaint)

        val file = File(context.cacheDir, "twenty-session-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Session"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun exportSessions(context: android.content.Context, sessions: List<Session>) {
    try {
        val json = kotlinx.serialization.json.Json { prettyPrint = true }
        val jsonString = json.encodeToString(ListSerializer(serializer<Session>()), sessions)
        val file = File(context.cacheDir, "twenty-sessions-${System.currentTimeMillis()}.json")
        file.writeText(jsonString)
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Sessions"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
