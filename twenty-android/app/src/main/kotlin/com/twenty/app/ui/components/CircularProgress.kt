package com.twenty.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.AccentSecondary
import com.twenty.app.ui.theme.TextPrimary

@Composable
fun CircularProgressTimer(
	progress: Float,
	countdown: Int,
	modifier: Modifier = Modifier
)
{
	Box(
		modifier = modifier.size(220.dp),
		contentAlignment = Alignment.Center
	)
{
		Canvas(modifier = Modifier.fillMaxSize())
{
			val strokeWidth = 8.dp.toPx()
			val radius = (size.minDimension - strokeWidth) / 2
			val center = Offset(size.width / 2, size.height / 2)

			drawCircle(
				color = Color(0x1Affffff),
				radius = radius,
				center = center,
				style = Stroke(strokeWidth)
			)

			val sweepAngle = 360f * progress
			drawArc(
				brush = Brush.sweepGradient(
					0f to AccentSecondary,
					1f to AccentPrimary
				),
				startAngle = -90f,
				sweepAngle = sweepAngle,
				useCenter = false,
				topLeft = Offset(center.x - radius, center.y - radius),
				size = Size(radius * 2, radius * 2),
				style = Stroke(strokeWidth, cap = StrokeCap.Round)
			)
		}
		Text(
			text = countdown.toString(),
			style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
			color = TextPrimary
		)
	}
}
