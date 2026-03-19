package com.twenty.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twenty.app.ui.theme.GlassBackground

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = GlassBackground,
        border = BorderStroke(1.dp, Color(0x0FFFFFFF))
    ) {
        Column(content = content)
    }
}
