package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.util.HapticHelper
import kotlinx.coroutines.delay

@Composable
fun BentoGridCard(
    item: AnchorItem,
    tint: CategoryTint,
    minHeight: Dp = 150.dp,
    staggerIndex: Int = 0,
    onCardClick: () -> Unit,
    onReminderClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Staggered spring rise animation
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(28f) }

    LaunchedEffect(item.id) {
        delay((staggerIndex * 40L).coerceAtMost(300L))
        val springSpec = spring<Float>(stiffness = 300f, dampingRatio = 0.85f)
        alphaAnim.animateTo(1f, springSpec)
    }

    LaunchedEffect(item.id) {
        delay((staggerIndex * 40L).coerceAtMost(300L))
        val springSpec = spring<Float>(stiffness = 300f, dampingRatio = 0.85f)
        offsetYAnim.animateTo(0f, springSpec)
    }

    // Determine big bottom text: Date or Time in tint's foreground
    val bigDisplayStr = remember(item.dueDate, item.dueTime, item.createdAt) {
        when {
            !item.dueTime.isNullOrBlank() -> item.dueTime
            !item.dueDate.isNullOrBlank() -> {
                // e.g. "2026-10-30" -> "Oct 30"
                try {
                    val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val d = sdfIn.parse(item.dueDate)
                    if (d != null) {
                        java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(d)
                    } else item.dueDate
                } catch (_: Exception) {
                    item.dueDate
                }
            }
            else -> {
                val d = java.util.Date(item.createdAt)
                java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(d)
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetYAnim.value.dp.roundToPx()) }
            .graphicsLayer { alpha = alphaAnim.value }
            .fillMaxWidth()
            .height(minHeight)
            // 28px continuous corner radii, stacked shadows + 1px inset highlight
            .softDimensionalDepth(
                cornerRadius = 28.dp,
                backgroundColor = tint.bgStart,
                faintTintGlow = tint.bgEnd,
                insetHighlight = true
            )
            .clip(RoundedCornerShape(28.dp))
            .drawBehind {
                // Gentle top-left to bottom-right gradient (about 8% lightness change)
                drawRect(brush = tint.gradientBrush)

                // 1.5px specular top bevel
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(28.dp.toPx() * 0.4f, 1.dp.toPx()),
                    end = Offset(size.width - (28.dp.toPx() * 0.4f), 1.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            .clickable {
                HapticHelper.vibrate8ms(context)
                onCardClick()
            }
            .padding(18.dp)
            .testTag("bento_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.matchParentSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: 28px thin line icon on left, 36px reminder bell on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thin-stroke line icon top-left (1.5px stroke, rounded caps, 28px)
                Canvas(modifier = Modifier.size(28.dp)) {
                    drawCategoryIcon(tint.iconType, color = tint.foreground, strokeWidth = 1.5.dp.toPx())
                }

                // Small circular reminder-bell button top-right (Surface-tinted, 36px)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            HapticHelper.vibrate8ms(context)
                            onReminderClick()
                        }
                        .drawBehind {
                            drawCircle(color = Color(0x33000000))
                            drawCircle(
                                color = tint.foreground.copy(alpha = 0.18f),
                                radius = size.minDimension / 2f - 0.5.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val stroke = 1.25.dp.toPx()
                        val w = size.width
                        val h = size.height
                        // Bell outline
                        val bellPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.5f, h * 0.2f)
                            cubicTo(w * 0.35f, h * 0.2f, w * 0.28f, h * 0.45f, w * 0.22f, h * 0.65f)
                            lineTo(w * 0.78f, h * 0.65f)
                            cubicTo(w * 0.72f, h * 0.45f, w * 0.65f, h * 0.2f, w * 0.5f, h * 0.2f)
                            close()
                        }
                        drawPath(bellPath, color = tint.foreground, style = Stroke(width = stroke, cap = StrokeCap.Round))
                        // Bell clapper
                        drawArc(
                            color = tint.foreground,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(w * 0.42f, h * 0.65f),
                            size = Size(w * 0.16f, h * 0.15f),
                            style = Stroke(width = stroke)
                        )
                    }
                }
            }

            // Bottom section: Large light date or time in tint foreground, title beneath in primary text
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bigDisplayStr,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    color = tint.foreground,
                    letterSpacing = (-0.02).em
                )

                // Clean subtle divider separating date/time numeral from task text
                Box(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .width(32.dp)
                        .height(1.dp)
                        .background(tint.foreground.copy(alpha = 0.35f))
                )

                Text(
                    text = item.title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
