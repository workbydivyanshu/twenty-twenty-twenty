package com.twenty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.GlassBackground
import com.twenty.app.ui.theme.TextPrimary
import com.twenty.app.ui.theme.TextSecondary

@Composable
fun TopNavBar(
    onRecapClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AccentPrimary, RoundedCornerShape(10.dp))
                    .clickable { onHomeClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("20", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Text("Twenty ·³", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onRecapClick) {
                Icon(Icons.Default.BarChart, contentDescription = "Recap", tint = TextSecondary)
            }

            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
            }
        }
    }
}
