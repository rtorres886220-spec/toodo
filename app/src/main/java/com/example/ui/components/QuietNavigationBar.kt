package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.Surface2
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyCoral
import com.example.ui.util.HapticHelper
import com.example.viewmodel.AnchorTab

@Composable
fun QuietNavigationBar(
    activeTab: AnchorTab,
    onTabSelected: (AnchorTab) -> Unit,
    inboxCount: Int,
    todayCount: Int,
    hasOverdue: Boolean,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 120.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "nav_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, navOffset.roundToPx()) }
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp) // floating 12px above bottom edge
            .drawBehind {
                // Multi-tier 3D Ambient and contact drop shadows
                drawRoundRect(
                    color = Color(0x180E182A),
                    topLeft = Offset(0f, 8.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0x14070E1A),
                    topLeft = Offset(0f, 2.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .drawBehind {
                // 1.5px specular top highlight
                drawLine(
                    color = Color.White,
                    start = Offset(28.dp.toPx() * 0.5f, 1.dp.toPx()),
                    end = Offset(size.width - (28.dp.toPx() * 0.5f), 1.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Subtle bottom border
                drawLine(
                    color = Color(0x10000000),
                    start = Offset(28.dp.toPx() * 0.5f, size.height - 0.5f),
                    end = Offset(size.width - (28.dp.toPx() * 0.5f), size.height - 0.5f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .height(64.dp)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                AnchorTab.INBOX to "INBOX",
                AnchorTab.TODAY to "TODAY",
                AnchorTab.UPCOMING to "UPCOMING",
                AnchorTab.NOTES to "NOTES",
                AnchorTab.DONE to "DONE"
            )

            tabs.forEach { (tab, label) ->
                val isActive = activeTab == tab
                val testTagStr = when (tab) {
                    AnchorTab.INBOX -> "tab_inbox"
                    AnchorTab.TODAY -> "tab_today"
                    AnchorTab.UPCOMING -> "tab_upcoming"
                    AnchorTab.NOTES -> "tab_notes"
                    AnchorTab.DONE -> "tab_done"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .padding(horizontal = 2.dp)
                        .then(
                            if (isActive) {
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Surface2)
                                    .drawBehind {
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(10.dp.toPx(), 1.dp.toPx()),
                                            end = Offset(size.width - 10.dp.toPx(), 1.dp.toPx()),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticHelper.vibrate8ms(context)
                            onTabSelected(tab)
                        }
                        .semantics {
                            contentDescription = "$label tab"
                            selected = isActive
                            role = Role.Tab
                        }
                        .testTag(testTagStr),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 24px icon: Inactive at 65% opacity, active in AccentGold
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(22.dp)) {
                                val stroke = 1.35.dp.toPx()
                                val iconColor = if (isActive) {
                                    AccentGold
                                } else {
                                    TextSecondary.copy(alpha = 0.65f)
                                }
                                drawQuietTabIcon(tab, iconColor, stroke)
                            }

                            // Notification dot if needed
                            if (tab == AnchorTab.INBOX && inboxCount > 0 && !isActive) {
                                Canvas(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    drawCircle(color = AccentGold)
                                }
                            } else if (tab == AnchorTab.TODAY && hasOverdue && !isActive) {
                                Canvas(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    drawCircle(color = UrgencyCoral)
                                }
                            }
                        }

                        // Active icon in gold with small mono label (11px) and soft glow dot
                        if (isActive) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Canvas(modifier = Modifier.size(3.dp)) {
                                    drawCircle(color = AccentGold)
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = label,
                                    fontFamily = GeistMono,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = AccentGold,
                                    letterSpacing = 0.12.em
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawQuietTabIcon(tab: AnchorTab, color: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    when (tab) {
        AnchorTab.INBOX -> {
            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.25f)
                lineTo(w * 0.85f, h * 0.25f)
                lineTo(w * 0.85f, h * 0.75f)
                lineTo(w * 0.65f, h * 0.75f)
                lineTo(w * 0.58f, h * 0.88f)
                lineTo(w * 0.42f, h * 0.88f)
                lineTo(w * 0.35f, h * 0.75f)
                lineTo(w * 0.15f, h * 0.75f)
                close()
            }
            drawPath(path, color = color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        }
        AnchorTab.TODAY -> {
            val center = Offset(w / 2f, h / 2f)
            val radius = size.minDimension / 2f - 2f
            drawCircle(color = color, radius = radius, center = center, style = Stroke(width = strokeWidth))
            drawLine(color = color, start = center, end = Offset(center.x, center.y - radius * 0.55f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = center, end = Offset(center.x + radius * 0.45f, center.y), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
        AnchorTab.UPCOMING -> {
            val left = w * 0.18f
            val top = h * 0.22f
            val right = w * 0.82f
            val bottom = h * 0.82f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
            drawLine(color = color, start = Offset(left, top + (bottom - top) * 0.3f), end = Offset(right, top + (bottom - top) * 0.3f), strokeWidth = strokeWidth)
            drawLine(color = color, start = Offset(left + 3.dp.toPx(), top - 2.dp.toPx()), end = Offset(left + 3.dp.toPx(), top + 2.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(right - 3.dp.toPx(), top - 2.dp.toPx()), end = Offset(right - 3.dp.toPx(), top + 2.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
        AnchorTab.NOTES -> {
            val left = w * 0.22f
            val right = w * 0.78f
            drawLine(color = color, start = Offset(left, h * 0.3f), end = Offset(right, h * 0.3f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(left, h * 0.5f), end = Offset(right * 0.85f, h * 0.5f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(left, h * 0.7f), end = Offset(right * 0.65f, h * 0.7f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
        AnchorTab.DONE -> {
            val center = Offset(w / 2f, h / 2f)
            val radius = size.minDimension / 2f - 2f
            drawCircle(color = color, radius = radius, center = center, style = Stroke(width = strokeWidth))
            drawLine(color = color, start = Offset(center.x - 4.dp.toPx(), center.y), end = Offset(center.x - 1.dp.toPx(), center.y + 2.5f.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x - 1.dp.toPx(), center.y + 2.5f.dp.toPx()), end = Offset(center.x + 4.dp.toPx(), center.y - 2.5f.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
    }
}
