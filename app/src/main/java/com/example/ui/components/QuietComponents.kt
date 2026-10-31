package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.HairlineColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * 0.5px subtle hairline divider
 */
@Composable
fun QuietHairline(
    modifier: Modifier = Modifier,
    color: Color = HairlineColor,
    thickness: Dp = 0.5.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .drawBehind {
                drawRect(color = color)
            }
    )
}

/**
 * Empty state:
 * - A single thin-line gold SVG/Canvas ring or abstract anchor at 96px, with one calm line of light text.
 * - Same vertical position on every tab.
 */
@Composable
fun QuietEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_rotate")
    val angle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 110.dp, bottom = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(modifier = Modifier.size(96.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 6f
                val strokeWidth = 1.dp.toPx()

                // Static base ring in faint gold
                drawCircle(
                    color = AccentGold.copy(alpha = 0.2f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Slowly rotating gold arc of light
                val sweepGradient = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        AccentGold.copy(alpha = 0.25f),
                        AccentGold.copy(alpha = 0.95f),
                        Color.Transparent
                    ),
                    center = center
                )

                drawArc(
                    brush = sweepGradient,
                    startAngle = angle.value,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth + 0.25f, cap = StrokeCap.Round)
                )

                // Center precision dot
                drawCircle(
                    color = AccentGold.copy(alpha = 0.6f),
                    radius = 2.dp.toPx(),
                    center = center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = message,
                color = TextSecondary,
                fontFamily = PlusJakartaSans,
                fontSize = 15.sp,
                letterSpacing = 0.sp
            )
        }
    }
}
