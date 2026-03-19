package com.twenty.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twenty.app.ui.theme.BgBase

@Composable
fun AmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 30f, animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "orb1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -20f, animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "orb2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        Box(
            modifier = Modifier
                .size(600.dp)
                .offset(x = (-150).dp, y = (-200).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x40131EB2), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (100).dp, y = (offset2 * 2).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x336366F1), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-50).dp, y = (400).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2D8B5CF6), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}
