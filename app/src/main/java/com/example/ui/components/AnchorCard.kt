package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.Surface2
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SwipeWashDone
import com.example.ui.theme.SwipeWashSnooze
import com.example.ui.theme.SwipeWashToday
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UrgencyCoral
import com.example.ui.util.HapticHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorCard(
    item: AnchorItem,
    isOverdue: Boolean = false,
    onMoveToToday: () -> Unit,
    onSnooze: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSnoozeCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCollapsing by remember { mutableStateOf(false) }
    val isDone = item.status == AnchorItem.STATUS_DONE

    // Completion ring animated progress (0f -> 1f)
    val completeAnim = remember { Animatable(if (isDone) 1f else 0f) }

    fun triggerComplete() {
        if (isDone) {
            onComplete()
            return
        }
        scope.launch {
            HapticHelper.vibrate8ms(context)
            completeAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            delay(120)
            isCollapsing = true
            delay(240)
            onComplete()
        }
    }

    val isTodayTask = item.status == AnchorItem.STATUS_TODAY

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.35f },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    HapticHelper.vibrate8ms(context)
                    if (isTodayTask) {
                        triggerComplete()
                    } else {
                        onMoveToToday()
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    HapticHelper.vibrate8ms(context)
                    onSnooze()
                    false
                }
                else -> false
            }
        }
    )

    var hasHapticFiredForSwipe by remember { mutableStateOf(false) }
    LaunchedEffect(dismissState.progress) {
        if (dismissState.progress > 0.35f && !hasHapticFiredForSwipe) {
            HapticHelper.vibrate8ms(context)
            hasHapticFiredForSwipe = true
        } else if (dismissState.progress <= 0.35f) {
            hasHapticFiredForSwipe = false
        }
    }

    AnimatedVisibility(
        visible = !isCollapsing,
        exit = fadeOut(tween(200)) + shrinkVertically(tween(240)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp)
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val progress = dismissState.progress.coerceIn(0f, 1f)

                val washColor = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> if (isTodayTask) SwipeWashDone else SwipeWashToday
                    SwipeToDismissBoxValue.EndToStart -> SwipeWashSnooze
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(washColor.copy(alpha = (washColor.alpha * progress * 2.2f).coerceAtMost(0.4f)))
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    val iconScale = (progress * 1.3f).coerceIn(0.4f, 1f)
                    Canvas(
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(
                                scaleX = iconScale,
                                scaleY = iconScale,
                                alpha = progress
                            )
                    ) {
                        val stroke = 1.25.dp.toPx()
                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            if (isTodayTask) {
                                // Done checkmark icon
                                val center = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(color = AccentGold, radius = size.minDimension / 2f - 2f, style = Stroke(width = stroke))
                                drawLine(color = AccentGold, start = Offset(center.x - 4.dp.toPx(), center.y), end = Offset(center.x - 1.dp.toPx(), center.y + 3.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
                                drawLine(color = AccentGold, start = Offset(center.x - 1.dp.toPx(), center.y + 3.dp.toPx()), end = Offset(center.x + 4.dp.toPx(), center.y - 3.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
                            } else {
                                // Gold Today instrument icon
                                val center = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(color = AccentGold, radius = size.minDimension / 2f - 2f, style = Stroke(width = stroke))
                                drawLine(color = AccentGold, start = center, end = Offset(center.x, center.y - 5.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
                                drawLine(color = AccentGold, start = center, end = Offset(center.x + 4.dp.toPx(), center.y), strokeWidth = stroke, cap = StrokeCap.Round)
                            }
                        } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                            // Slate Snooze chevron
                            val w = size.width
                            val h = size.height
                            drawLine(color = TextSecondary, start = Offset(w * 0.35f, h * 0.25f), end = Offset(w * 0.65f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                            drawLine(color = TextSecondary, start = Offset(w * 0.65f, h * 0.5f), end = Offset(w * 0.35f, h * 0.75f), strokeWidth = stroke, cap = StrokeCap.Round)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            // 3D Sculpted Ceramic Row with 20px radius, multi-tier shadows, and specular highlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (isDone) alpha = 0.65f // Dimmed 65% opacity for Done, strikethrough-free
                    }
                    .drawBehind {
                        // 3D Ambient & contact drop shadows
                        drawRoundRect(
                            color = Color(0x100E182A),
                            topLeft = Offset(0f, 6.dp.toPx()),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0x12070E1A),
                            topLeft = Offset(0f, 1.5.dp.toPx()),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface1)
                    .drawBehind {
                        // 1.5px specular top highlight
                        drawLine(
                            color = Color.White,
                            start = Offset(10.dp.toPx(), 1.dp.toPx()),
                            end = Offset(size.width - 10.dp.toPx(), 1.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        // Subtle bottom occlusion boundary
                        drawLine(
                            color = Color(0x0E000000),
                            start = Offset(10.dp.toPx(), size.height - 0.5f),
                            end = Offset(size.width - 10.dp.toPx(), size.height - 0.5f),
                            strokeWidth = 1.dp.toPx()
                        )
                        // Overdue left ember hairline
                        if (isOverdue) {
                            drawLine(
                                color = UrgencyCoral,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }
                    .clickable { onEdit() }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("anchor_item_${item.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox: Thin ring drawing closed on completion (48dp touch target)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { triggerComplete() }
                        .testTag("complete_button_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val strokeWidth = 1.25.dp.toPx()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 2f

                        drawCircle(
                            color = if (isOverdue) UrgencyCoral.copy(alpha = 0.5f) else TextTertiary,
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        if (completeAnim.value > 0f) {
                            val strokeColor = if (isOverdue) UrgencyCoral else AccentGold
                            drawArc(
                                color = strokeColor,
                                startAngle = -90f,
                                sweepAngle = completeAnim.value * 360f,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth + 0.5f, cap = StrokeCap.Round)
                            )
                            if (completeAnim.value > 0.85f) {
                                val checkAlpha = ((completeAnim.value - 0.85f) / 0.15f).coerceIn(0f, 1f)
                                drawLine(
                                    color = strokeColor.copy(alpha = checkAlpha),
                                    start = Offset(center.x - 4.dp.toPx(), center.y),
                                    end = Offset(center.x - 1.dp.toPx(), center.y + 3.dp.toPx()),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = strokeColor.copy(alpha = checkAlpha),
                                    start = Offset(center.x - 1.dp.toPx(), center.y + 3.dp.toPx()),
                                    end = Offset(center.x + 4.dp.toPx(), center.y - 3.dp.toPx()),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // Subtle vertical divider separating checkbox control from text content
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title & encapsulated tags
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = when {
                            isDone -> TextSecondary
                            isOverdue -> TextPrimary
                            else -> TextPrimary
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.snoozeCount >= 3 && !isDone) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(UrgencyCoral.copy(alpha = 0.12f))
                                .clickable { onSnoozeCoach() }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "POSTPONED ${item.snoozeCount}X • BREAK DOWN",
                                fontFamily = GeistMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = UrgencyCoral,
                                letterSpacing = 0.1.em
                            )
                        }
                    } else if (item.tagList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item.tagList.take(3).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontFamily = GeistMono,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        letterSpacing = 0.08.em
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right side: Insulated sculpted telemetry capsule
                val timeText = buildString {
                    if (isOverdue) append("OVERDUE ")
                    if (!item.dueTime.isNullOrBlank()) append(item.dueTime)
                    else if (!item.dueDate.isNullOrBlank()) append(item.dueDate)
                    if (item.estimatedMinutes != null) {
                        if (isNotEmpty()) append("  ")
                        append("${item.estimatedMinutes}M")
                    }
                }.trim()

                if (timeText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface2)
                            .drawBehind {
                                drawRoundRect(
                                    color = if (isOverdue) UrgencyCoral.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = timeText.uppercase(),
                            fontFamily = GeistMono,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = when {
                                isOverdue -> UrgencyCoral
                                isDone -> TextTertiary
                                else -> TextSecondary
                            },
                            letterSpacing = 0.12.em
                        )
                    }
                }
            }
        }
    }
}
