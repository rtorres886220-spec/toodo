package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GeistMono
import com.example.ui.theme.GoldButtonText
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.Surface2
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.HapticHelper

@Composable
fun CaptureBar(
    onCapture: (String) -> Unit,
    clipboardText: String?,
    onAcceptClipboard: () -> Unit,
    onDismissClipboard: () -> Unit,
    focusOnStart: Boolean = false,
    onFocusHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    LaunchedEffect(focusOnStart) {
        if (focusOnStart) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }

    // Breathing thin gold line animation under text on focus
    val infiniteTransition = rememberInfiniteTransition(label = "capture_breathe")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line_alpha"
    )

    // Gold orb voice recording pulse
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecording = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                HapticHelper.vibrate8ms(context)
                onCapture(spokenText)
            }
        }
    }

    fun submitAndClear() {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            HapticHelper.vibrate8ms(context)
            onCapture(trimmed)
            text = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        // Clipboard detection banner
        AnimatedVisibility(
            visible = clipboardText != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (clipboardText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface2)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("clipboard_banner"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLIP // ${clipboardText.take(24)}",
                        fontFamily = GeistMono,
                        fontWeight = FontWeight.Light,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 0.14.em,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "ANCHOR",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = AccentGold,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable {
                                HapticHelper.vibrate8ms(context)
                                onAcceptClipboard()
                            }
                            .testTag("clipboard_add_button")
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "DISMISS",
                        fontFamily = GeistMono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.14.em,
                        modifier = Modifier
                            .clickable { onDismissClipboard() }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Quick Intelligence suggestions on focus
        AnimatedVisibility(
            visible = isFocused && text.isEmpty() && clipboardText == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "What am I forgetting?",
                    "What can I finish in 20m?",
                    "What should I focus on?"
                ).forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface2)
                            .clickable {
                                HapticHelper.vibrate8ms(context)
                                onCapture(prompt)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            fontFamily = GeistMono,
                            fontSize = 10.sp,
                            color = AccentGold,
                            letterSpacing = 0.08.em
                        )
                    }
                }
            }
        }

        // Floating 3D Ceramic Glass Pill (28px radius, multi-tier ambient shadows, specular top bevel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                    // Subtle bottom occlusion edge
                    drawLine(
                        color = Color(0x10000000),
                        start = Offset(28.dp.toPx() * 0.5f, size.height - 0.5f),
                        end = Offset(size.width - (28.dp.toPx() * 0.5f), size.height - 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused }
                            .testTag("capture_input"),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 15.sp,
                            letterSpacing = 0.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(AccentGold),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitAndClear() }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        text = "Capture task, thought, or memo...",
                                        color = TextSecondary,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Thin gold line breathes beneath the text on focus
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        if (isFocused) {
                            drawLine(
                                color = AccentGold.copy(alpha = breathingAlpha),
                                start = Offset(0f, 1f),
                                end = Offset(size.width, 1f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (text.isNotBlank()) {
                    // 3D Metallic Send button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Color(0x30BF8828),
                                    radius = 23.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height / 2f + 3.dp.toPx())
                                )
                            }
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFDCAB4C), Color(0xFFB57D20))
                                )
                            )
                            .drawBehind {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.5f),
                                    radius = size.minDimension / 2f - 1.dp.toPx(),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                            .clickable { submitAndClear() }
                            .semantics {
                                contentDescription = "Save captured item"
                                role = Role.Button
                            }
                            .testTag("capture_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val stroke = 2.dp.toPx()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawLine(
                                color = GoldButtonText,
                                start = Offset(center.x - 5.dp.toPx(), center.y),
                                end = Offset(center.x + 5.dp.toPx(), center.y),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = GoldButtonText,
                                start = Offset(center.x + 1.dp.toPx(), center.y - 4.dp.toPx()),
                                end = Offset(center.x + 5.dp.toPx(), center.y),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = GoldButtonText,
                                start = Offset(center.x + 1.dp.toPx(), center.y + 4.dp.toPx()),
                                end = Offset(center.x + 5.dp.toPx(), center.y),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                } else {
                    // 44px 3D metallic gold orb containing microphone glyph
                    val orbScale = if (isRecording) micPulse else 1f
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Color(0x30BF8828),
                                    radius = 23.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height / 2f + 3.dp.toPx())
                                )
                            }
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFDCAB4C), Color(0xFFB57D20))
                                )
                            )
                            .graphicsLayer {
                                scaleX = orbScale
                                scaleY = orbScale
                            }
                            .clickable {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak anchor...")
                                    }
                                    val hasHandler = context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
                                    if (hasHandler) {
                                        isRecording = true
                                        voiceLauncher.launch(intent)
                                    } else {
                                        android.widget.Toast.makeText(context, "Voice input not supported on this device", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (_: Exception) {
                                    isRecording = false
                                    android.widget.Toast.makeText(context, "Voice input unavailable", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .semantics {
                                contentDescription = "Capture with voice"
                                role = Role.Button
                            }
                            .drawBehind {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.5f),
                                    radius = size.minDimension / 2f - 1.dp.toPx(),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                            .testTag("voice_capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val stroke = 1.5.dp.toPx()
                            val micW = 6.dp.toPx()
                            val micH = 10.dp.toPx()
                            val micTop = center.y - 6.5.dp.toPx()
                            val micLeft = center.x - micW / 2f

                            // Rounded mic capsule in crisp white
                            drawRoundRect(
                                color = GoldButtonText,
                                topLeft = Offset(micLeft, micTop),
                                size = Size(micW, micH),
                                cornerRadius = CornerRadius(micW / 2f, micW / 2f),
                                style = Stroke(width = stroke)
                            )

                            // Cradle arc around lower capsule
                            val cradleRadius = 5.5.dp.toPx()
                            drawArc(
                                color = GoldButtonText,
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(center.x - cradleRadius, center.y - 2.5.dp.toPx()),
                                size = Size(cradleRadius * 2, cradleRadius * 2),
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )

                            // Stem line
                            val stemTop = center.y - 2.5.dp.toPx() + cradleRadius
                            drawLine(
                                color = GoldButtonText,
                                start = Offset(center.x, stemTop),
                                end = Offset(center.x, stemTop + 3.5.dp.toPx()),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )

                            // Base horizontal foot
                            drawLine(
                                color = GoldButtonText,
                                start = Offset(center.x - 3.5.dp.toPx(), stemTop + 3.5.dp.toPx()),
                                end = Offset(center.x + 3.5.dp.toPx(), stemTop + 3.5.dp.toPx()),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
