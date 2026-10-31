package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.IntelligenceAnswer
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface1
import com.example.ui.theme.Surface2
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.HapticHelper

@Composable
fun IntelligenceDialog(
    query: String,
    answer: IntelligenceAnswer?,
    isLoading: Boolean,
    onItemClick: (AnchorItem) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Surface1)
                .drawBehind {
                    // 1px top highlight
                    drawLine(
                        color = SurfaceHighlight,
                        start = Offset(0f, 0.5f),
                        end = Offset(size.width, 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(24.dp)
                .testTag("intelligence_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANCHOR INTELLIGENCE",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = AccentGold,
                        letterSpacing = 0.14.em
                    )

                    Text(
                        text = "CLOSE",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Query
                Text(
                    text = "\"$query\"",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Light,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))
                QuietHairline()
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading || answer == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentGold,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Evaluating your connected anchors...",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                letterSpacing = 0.14.em
                            )
                        }
                    }
                } else {
                    // Headline
                    Text(
                        text = answer.headline.uppercase(),
                        fontFamily = GeistMono,
                        fontSize = 12.sp,
                        color = AccentGold,
                        letterSpacing = 0.14.em
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Answer body
                    Text(
                        text = answer.answer,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = TextPrimary
                    )

                    // Relevant Items (if any)
                    if (answer.relevantItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "CONNECTED RECORDS",
                            fontFamily = GeistMono,
                            fontSize = 11.sp,
                            color = TextTertiary,
                            letterSpacing = 0.14.em
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        answer.relevantItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Surface2)
                                    .clickable {
                                        HapticHelper.vibrate8ms(context)
                                        onItemClick(item)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.type == AnchorItem.TYPE_NOTE) "NOTE" else "TASK",
                                    fontFamily = GeistMono,
                                    fontSize = 10.sp,
                                    color = AccentGold,
                                    letterSpacing = 0.14.em
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Suggested actions
                    if (answer.suggestedActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            answer.suggestedActions.forEach { act ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Surface2)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = act,
                                        fontFamily = GeistMono,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        letterSpacing = 0.1.em
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
