package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.AnchorItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GoldButtonBackground
import com.example.ui.theme.GeistMono
import com.example.ui.theme.HairlineColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SoftDarkBackground
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UrgencyCoral
import com.example.ui.util.HapticHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteDetailSheet(
    item: AnchorItem,
    onSave: (AnchorItem) -> Unit,
    onDelete: (AnchorItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tint = remember(item) { getCategoryTint(item) }

    var title by remember { mutableStateOf(item.title) }
    var rawInput by remember { mutableStateOf(item.rawInput.ifBlank { item.title }) }
    var tags by remember { mutableStateOf(item.tags) }

    val dateFormatted = remember(item.createdAt) {
        SimpleDateFormat("MMMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(item.createdAt)).uppercase()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftDarkBackground.copy(alpha = 0.95f))
            .clickable(onClick = onDismiss)
            .testTag("note_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {}
        ) {
            // Tinted Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(brush = tint.gradientBrush)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            HapticHelper.vibrate8ms(context)
                            onDismiss()
                        }
                        .align(Alignment.TopStart)
                        .testTag("note_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val stroke = 1.5.dp.toPx()
                        drawLine(
                            color = tint.foreground,
                            start = Offset(size.width * 0.65f, size.height * 0.25f),
                            end = Offset(size.width * 0.35f, size.height * 0.5f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = tint.foreground,
                            start = Offset(size.width * 0.35f, size.height * 0.5f),
                            end = Offset(size.width * 0.65f, size.height * 0.75f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Copy to Clipboard Action top right
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            HapticHelper.vibrate8ms(context)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Anchor Note", "$title\n\n$rawInput")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .align(Alignment.TopEnd)
                        .testTag("note_copy_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val stroke = 1.5.dp.toPx()
                        val w = size.width
                        val h = size.height
                        // Two overlapping rounded rectangles
                        drawRoundRect(
                            color = tint.foreground.copy(alpha = 0.5f),
                            topLeft = Offset(w * 0.25f, h * 0.1f),
                            size = androidx.compose.ui.geometry.Size(w * 0.65f, h * 0.65f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                        )
                        drawRoundRect(
                            color = tint.foreground,
                            topLeft = Offset(w * 0.1f, h * 0.25f),
                            size = androidx.compose.ui.geometry.Size(w * 0.65f, h * 0.65f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "NOTE // MEMO",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = tint.foreground,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormatted,
                        fontFamily = GeistMono,
                        fontWeight = FontWeight.Light,
                        fontSize = 11.sp,
                        color = tint.foreground.copy(alpha = 0.8f),
                        letterSpacing = 0.14.em
                    )
                }
            }

            // Note Content Body Sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Surface1)
                    .drawBehind {
                        drawLine(
                            color = SurfaceHighlight,
                            start = Offset(32.dp.toPx(), 0.5f),
                            end = Offset(size.width - 32.dp.toPx(), 0.5f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 24.dp)
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Editable Title
                    SharedAttachmentButton(item.sharedUri)
                    Text(
                        text = "TITLE",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            color = TextPrimary
                        ),
                        cursorBrush = SolidColor(AccentGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(HairlineColor)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Body / Notes field
                    Text(
                        text = "CONTENT",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                            color = TextSecondary
                        ),
                        cursorBrush = SolidColor(AccentGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_content_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(HairlineColor)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Tags field
                    Text(
                        text = "TAGS",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.14.em
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        textStyle = TextStyle(
                            fontFamily = GeistMono,
                            fontSize = 13.sp,
                            color = AccentGold,
                            letterSpacing = 0.14.em
                        ),
                        cursorBrush = SolidColor(AccentGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_tags_input")
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Action buttons: Save changes & Delete note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(Color(0x15FF7A5C))
                                .clickable {
                                    HapticHelper.vibrate8ms(context)
                                    onDelete(item)
                                    onDismiss()
                                }
                                .testTag("note_delete_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DELETE NOTE",
                                fontFamily = GeistMono,
                                fontSize = 11.sp,
                                color = UrgencyCoral,
                                letterSpacing = 0.14.em
                            )
                        }

                        // Save Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(GoldButtonBackground)
                                .clickable {
                                    HapticHelper.vibrate8ms(context)
                                    onSave(
                                        item.copy(
                                            title = title.trim(),
                                            rawInput = rawInput.trim(),
                                            tags = tags.trim()
                                        )
                                    )
                                    onDismiss()
                                }
                                .testTag("note_save_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SAVE NOTE",
                                fontFamily = GeistMono,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = com.example.ui.theme.GoldButtonText,
                                letterSpacing = 0.14.em
                            )
                        }
                    }
                }
            }
        }
    }
}
