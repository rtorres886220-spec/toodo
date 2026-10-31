package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.AnchorItem
import com.example.ui.theme.TintApricotEnd
import com.example.ui.theme.TintApricotFg
import com.example.ui.theme.TintApricotStart
import com.example.ui.theme.TintOrchidEnd
import com.example.ui.theme.TintOrchidFg
import com.example.ui.theme.TintOrchidStart
import com.example.ui.theme.TintRoseEnd
import com.example.ui.theme.TintRoseFg
import com.example.ui.theme.TintRoseStart
import com.example.ui.theme.TintSageEnd
import com.example.ui.theme.TintSageFg
import com.example.ui.theme.TintSageStart
import com.example.ui.theme.TintSlateEnd
import com.example.ui.theme.TintSlateFg
import com.example.ui.theme.TintSlateStart

data class CategoryTint(
    val name: String,
    val bgStart: Color,
    val bgEnd: Color,
    val foreground: Color,
    val iconType: CategoryIconType
) {
    val gradientBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(bgStart, bgEnd),
            start = Offset.Zero,
            end = Offset.Infinite
        )
}

enum class CategoryIconType {
    CELEBRATION, // Cake / Event
    WELLNESS,    // Cat / Health / Pet
    LEARNING,    // Video / Book / Course
    WORK,        // Briefcase / Meetings
    INSPIRATION  // Note / Star / Thought
}

val OrchidTint = CategoryTint(
    name = "Orchid",
    bgStart = TintOrchidStart,
    bgEnd = TintOrchidEnd,
    foreground = TintOrchidFg,
    iconType = CategoryIconType.CELEBRATION
)

val SageTint = CategoryTint(
    name = "Sage",
    bgStart = TintSageStart,
    bgEnd = TintSageEnd,
    foreground = TintSageFg,
    iconType = CategoryIconType.WELLNESS
)

val ApricotTint = CategoryTint(
    name = "Apricot",
    bgStart = TintApricotStart,
    bgEnd = TintApricotEnd,
    foreground = TintApricotFg,
    iconType = CategoryIconType.LEARNING
)

val SlateTint = CategoryTint(
    name = "Slate",
    bgStart = TintSlateStart,
    bgEnd = TintSlateEnd,
    foreground = TintSlateFg,
    iconType = CategoryIconType.WORK
)

val RoseTint = CategoryTint(
    name = "Rose",
    bgStart = TintRoseStart,
    bgEnd = TintRoseEnd,
    foreground = TintRoseFg,
    iconType = CategoryIconType.INSPIRATION
)

val AllCategoryTints = listOf(OrchidTint, SageTint, ApricotTint, SlateTint, RoseTint)

fun getCategoryTint(item: AnchorItem): CategoryTint {
    val lower = (item.tags + " " + item.title).lowercase()
    return when {
        lower.contains("birthday") || lower.contains("celebrat") || lower.contains("party") || lower.contains("dinner") -> OrchidTint
        lower.contains("health") || lower.contains("wellness") || lower.contains("doctor") || lower.contains("dentist") || lower.contains("gym") || lower.contains("vet") -> SageTint
        lower.contains("course") || lower.contains("learn") || lower.contains("read") || lower.contains("watch") || lower.contains("order") || lower.contains("studio") -> ApricotTint
        lower.contains("meeting") || lower.contains("work") || lower.contains("doc") || lower.contains("arch") || lower.contains("review") || lower.contains("client") -> SlateTint
        lower.contains("idea") || lower.contains("note") || lower.contains("philosophy") || lower.contains("research") || lower.contains("personal") -> RoseTint
        else -> {
            val idx = (item.id.toInt().coerceAtLeast(0)) % AllCategoryTints.size
            AllCategoryTints[idx]
        }
    }
}

/**
 * Draws thin-stroke line icons (1.5px stroke, rounded caps, 28px).
 */
fun DrawScope.drawCategoryIcon(type: CategoryIconType, color: Color, strokeWidth: Float = 1.5.dp.toPx()) {
    val w = size.width
    val h = size.height

    when (type) {
        CategoryIconType.CELEBRATION -> {
            // Cake / celebration candles line icon
            // Base layer
            val baseLeft = w * 0.18f
            val baseRight = w * 0.82f
            val baseTop = h * 0.55f
            val baseBottom = h * 0.85f
            drawRoundRect(
                color = color,
                topLeft = Offset(baseLeft, baseTop),
                size = Size(baseRight - baseLeft, baseBottom - baseTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Decorative line on cake
            drawLine(
                color = color,
                start = Offset(baseLeft + 2.dp.toPx(), h * 0.7f),
                end = Offset(baseRight - 2.dp.toPx(), h * 0.7f),
                strokeWidth = strokeWidth * 0.8f,
                cap = StrokeCap.Round
            )
            // 3 candles
            val candle1X = w * 0.32f
            val candle2X = w * 0.50f
            val candle3X = w * 0.68f
            val candleTop = h * 0.35f
            drawLine(color = color, start = Offset(candle1X, candleTop), end = Offset(candle1X, baseTop), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(candle2X, candleTop), end = Offset(candle2X, baseTop), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(candle3X, candleTop), end = Offset(candle3X, baseTop), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // 3 flames
            drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(candle1X, candleTop - 3.dp.toPx()))
            drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(candle2X, candleTop - 3.dp.toPx()))
            drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(candle3X, candleTop - 3.dp.toPx()))
        }
        CategoryIconType.WELLNESS -> {
            // Cat / Pet face line icon with ears and whiskers
            val cx = w * 0.5f
            val cy = h * 0.56f
            val r = w * 0.34f
            // Head circle
            drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
            // Left ear
            val earL = Path().apply {
                moveTo(cx - r * 0.85f, cy - r * 0.4f)
                lineTo(cx - r * 0.95f, cy - r * 1.05f)
                lineTo(cx - r * 0.3f, cy - r * 0.92f)
            }
            drawPath(earL, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // Right ear
            val earR = Path().apply {
                moveTo(cx + r * 0.85f, cy - r * 0.4f)
                lineTo(cx + r * 0.95f, cy - r * 1.05f)
                lineTo(cx + r * 0.3f, cy - r * 0.92f)
            }
            drawPath(earR, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // Eyes
            drawCircle(color = color, radius = 1.25.dp.toPx(), center = Offset(cx - r * 0.38f, cy - r * 0.05f))
            drawCircle(color = color, radius = 1.25.dp.toPx(), center = Offset(cx + r * 0.38f, cy - r * 0.05f))
            // Nose & mouth
            drawLine(color = color, start = Offset(cx, cy + r * 0.2f), end = Offset(cx, cy + r * 0.42f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            // Whiskers
            drawLine(color = color, start = Offset(cx - r * 0.6f, cy + r * 0.25f), end = Offset(cx - r * 1.1f, cy + r * 0.15f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(cx + r * 0.6f, cy + r * 0.25f), end = Offset(cx + r * 1.1f, cy + r * 0.15f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
        CategoryIconType.LEARNING -> {
            // Video / Display screen with play symbol
            val left = w * 0.18f
            val top = h * 0.24f
            val right = w * 0.82f
            val bottom = h * 0.76f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Play triangle inside
            val playPath = Path().apply {
                val pcx = w * 0.48f
                val pcy = h * 0.5f
                moveTo(pcx - 3.dp.toPx(), pcy - 4.5.dp.toPx())
                lineTo(pcx + 5.dp.toPx(), pcy)
                lineTo(pcx - 3.dp.toPx(), pcy + 4.5.dp.toPx())
                close()
            }
            drawPath(playPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        CategoryIconType.WORK -> {
            // Briefcase / Portfolio
            val left = w * 0.18f
            val top = h * 0.38f
            val right = w * 0.82f
            val bottom = h * 0.82f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Handle at top
            val handlePath = Path().apply {
                val hLeft = w * 0.38f
                val hRight = w * 0.62f
                val hTop = h * 0.24f
                moveTo(hLeft, top)
                lineTo(hLeft, hTop)
                lineTo(hRight, hTop)
                lineTo(hRight, top)
            }
            drawPath(handlePath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // Center lock line
            drawLine(
                color = color,
                start = Offset(left, h * 0.58f),
                end = Offset(right, h * 0.58f),
                strokeWidth = strokeWidth * 0.8f
            )
        }
        CategoryIconType.INSPIRATION -> {
            // Abstract clean anchor / spark / star
            val cx = w * 0.5f
            val cy = h * 0.5f
            val path = Path().apply {
                moveTo(cx, cy - 8.dp.toPx())
                lineTo(cx + 2.5.dp.toPx(), cy - 2.5.dp.toPx())
                lineTo(cx + 8.dp.toPx(), cy)
                lineTo(cx + 2.5.dp.toPx(), cy + 2.5.dp.toPx())
                lineTo(cx, cy + 8.dp.toPx())
                lineTo(cx - 2.5.dp.toPx(), cy + 2.5.dp.toPx())
                lineTo(cx - 8.dp.toPx(), cy)
                lineTo(cx - 2.5.dp.toPx(), cy - 2.5.dp.toPx())
                close()
            }
            drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
