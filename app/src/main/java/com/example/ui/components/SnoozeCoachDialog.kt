package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.SnoozeBreakdown
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UrgencyCoral
import com.example.ui.util.HapticHelper

@Composable
fun SnoozeCoachDialog(
    item: AnchorItem?,
    breakdown: SnoozeBreakdown?,
    isLoading: Boolean,
    onDemand: Boolean = false,
    onApplySubtasks: (AnchorItem, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    if (item == null) return
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .drawBehind {
                    drawRect(color = Surface1)
                    // 1px top highlight
                    drawLine(
                        color = SurfaceHighlight,
                        start = Offset(0f, 0.5f),
                        end = Offset(size.width, 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(24.dp)
                .testTag("snooze_coach_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (onDemand) "TASK BREAKDOWN" else "SNOOZE COACH",
                        fontFamily = GeistMono,
                        fontSize = 12.sp,
                        color = UrgencyCoral,
                        letterSpacing = 0.14.em
                    )

                    Text(
                        text = "CLOSE",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Item info
                Text(
                    text = item.title,
                    fontFamily = PlusJakartaSans,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (onDemand) "A SMALLER WAY TO BEGIN" else "POSTPONED ${item.snoozeCount} TIMES",
                    fontFamily = GeistMono,
                    fontSize = 11.sp,
                    color = UrgencyCoral,
                    letterSpacing = 0.14.em
                )

                Spacer(modifier = Modifier.height(16.dp))
                QuietHairline()
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading || breakdown == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = UrgencyCoral,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (onDemand) "Finding a gentle first step..." else "Distilling into 5-minute micro steps...",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                letterSpacing = 0.14.em
                            )
                        }
                    }
                } else {
                    Text(
                        text = breakdown.empathyPrompt,
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SUGGESTED MICRO FIRST STEPS",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = AccentGold,
                        letterSpacing = 0.14.em
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    breakdown.subtasks.forEachIndexed { idx, subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "0${idx + 1}",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = AccentGold,
                                letterSpacing = 0.14.em
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = subtask,
                                fontFamily = PlusJakartaSans,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (item.aiBreakdownSteps != null) {
                        Text(
                            text = "Applying this plan replaces the current micro-steps and linked subtask tasks.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticHelper.vibrate8ms(context)
                                onApplySubtasks(item, breakdown.subtasks)
                            }
                            .testTag("apply_subtasks_button")
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "APPLY MICRO STEPS",
                            fontFamily = GeistMono,
                            fontSize = 12.sp,
                            color = AccentGold,
                            letterSpacing = 0.14.em
                        )
                    }
                }
            }
        }
    }
}
