package com.example.ui.views

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.example.ui.components.QuietEmptyState
import com.example.ui.components.QuietHairline
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.HapticHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DoneView(
    doneTasks: List<AnchorItem>,
    onRestore: (AnchorItem) -> Unit,
    onDelete: (AnchorItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Quiet Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPLETED ANCHORS",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 0.14.em
                )
                Text(
                    text = "${doneTasks.size} CLOSED",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    letterSpacing = 0.14.em
                )
            }
            QuietHairline(modifier = Modifier.padding(horizontal = 24.dp))
        }

        if (doneTasks.isEmpty()) {
            item {
                QuietEmptyState(message = "Nothing completed yet")
            }
        } else {
            items(doneTasks, key = { it.id }) { item ->
                val completedDateStr = item.completedAt?.let {
                    SimpleDateFormat("MMM d • HH:mm", Locale.getDefault()).format(Date(it)).uppercase()
                } ?: ""

                // Done is dimmed (65% opacity) with strikethrough-free styling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 5.dp)
                        .graphicsLayer { alpha = 0.65f }
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface1)
                        .drawBehind {
                            // 1px top highlight
                            drawLine(
                                color = SurfaceHighlight,
                                start = Offset(20.dp.toPx() * 0.5f, 0.5f),
                                end = Offset(size.width - 20.dp.toPx() * 0.5f, 0.5f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("done_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Completed thin ring check icon
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val stroke = 1.25.dp.toPx()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                color = TextTertiary,
                                radius = size.minDimension / 2f - 2f,
                                style = Stroke(width = stroke)
                            )
                            drawLine(
                                color = TextTertiary,
                                start = Offset(center.x - 3.5.dp.toPx(), center.y),
                                end = Offset(center.x - 1.dp.toPx(), center.y + 2.5f.dp.toPx()),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = TextTertiary,
                                start = Offset(center.x - 1.dp.toPx(), center.y + 2.5f.dp.toPx()),
                                end = Offset(center.x + 3.5.dp.toPx(), center.y - 2.5f.dp.toPx()),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Strikethrough-free styling!
                            Text(
                                text = item.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (completedDateStr.isNotBlank()) {
                                Text(
                                    text = completedDateStr,
                                    fontFamily = GeistMono,
                                    fontSize = 11.sp,
                                    color = TextTertiary,
                                    letterSpacing = 0.14.em
                                )
                            }
                        }

                        // Restore action in gold
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    HapticHelper.vibrate8ms(context)
                                    onRestore(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RESTORE",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = AccentGold,
                                letterSpacing = 0.14.em
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Delete action
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    HapticHelper.vibrate8ms(context)
                                    onDelete(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DEL",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = TextTertiary,
                                letterSpacing = 0.14.em
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}
