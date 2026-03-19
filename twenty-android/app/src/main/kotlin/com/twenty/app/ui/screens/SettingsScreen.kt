package com.twenty.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.twenty.app.data.Settings
import com.twenty.app.ui.components.GlassCard
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.BorderMedium
import com.twenty.app.ui.theme.Danger
import com.twenty.app.ui.theme.GlassBackground
import com.twenty.app.ui.theme.Success
import com.twenty.app.ui.theme.TextMuted
import com.twenty.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    settings: Settings,
    onUpdateSettings: (Settings) -> Unit,
    onExportSessions: () -> Unit,
    onClearSessions: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val hasNotificationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onUpdateSettings(settings.copy(notificationsEnabled = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Notifications", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                SettingsRow("Enable Notifications") {
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(notificationsEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentPrimary)
                    )
                }
                SettingsRow("Permission") {
                    Text(
                        if (hasNotificationPermission) "Enabled" else "Not Set",
                        color = if (hasNotificationPermission) Success else TextMuted
                    )
                }
                TextButton(
                    onClick = {
                        if (!hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text(if (hasNotificationPermission) "Re-request Permission" else "Request Permission")
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sound", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                SettingsRow("Enable Sound") {
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentPrimary)
                    )
                }
                if (settings.soundEnabled) {
                    Column {
                        Text("Volume", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = settings.volume,
                            onValueChange = { onUpdateSettings(settings.copy(volume = it)) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentPrimary,
                                activeTrackColor = AccentPrimary
                            )
                        )
                    }
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Break Reminders", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                SettingsRow("Reminder Interval") {
                    Text("20 min", color = AccentPrimary)
                }
                SettingsRow("Break Duration") {
                    Text("20 sec", color = AccentPrimary)
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Data", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onExportSessions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Sessions")
                }
                Spacer(Modifier.height(8.dp))
                if (showClearConfirm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onClearSessions(); showClearConfirm = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Danger)
                        ) {
                            Text("Confirm Delete")
                        }
                        OutlinedButton(
                            onClick = { showClearConfirm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    TextButton(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Danger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Data")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        trailing()
    }
}
