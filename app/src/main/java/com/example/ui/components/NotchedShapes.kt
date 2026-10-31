package com.example.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Surface1
import com.example.ui.theme.SurfaceHighlight
import kotlin.random.Random

/**
 * 3D Ceramic Depth: Multi-tier ambient occlusion shadows,
 * pure specular top bevel (1.5px White), bottom contact edge, and optional tint radiance.
 */
fun Modifier.softDimensionalDepth(
    cornerRadius: Dp = 28.dp,
    backgroundColor: Color = Surface1,
    faintTintGlow: Color? = null,
    insetHighlight: Boolean = true
): Modifier = this.drawBehind {
    val r = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    // 1. Wide diffuse 3D ambient drop shadow
    drawRoundRect(
        color = Color(0x120E182A),
        topLeft = Offset(0f, 7.dp.toPx()),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r + 3.dp.toPx(), r + 3.dp.toPx())
    )

    // 2. Crisp contact occlusion shadow
    drawRoundRect(
        color = Color(0x16070E1A),
        topLeft = Offset(0f, 2.dp.toPx()),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
    )

    // 3. Faint tint radiance if specified
    if (faintTintGlow != null && faintTintGlow != Color.Transparent) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(faintTintGlow.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.25f, 0f),
                radius = w * 0.75f
            )
        )
    }

    // Base background surface
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
    )

    // 1.5px crisp specular top bevel
    if (insetHighlight) {
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(r * 0.4f, 1.dp.toPx()),
            end = Offset(w - (r * 0.4f), 1.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        // Subtle bottom occlusion hairline
        drawLine(
            color = Color(0x0E000000),
            start = Offset(r * 0.4f, h - 0.5f),
            end = Offset(w - (r * 0.4f), h - 0.5f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * Abnormal UI: Machined debossed 3D well (sunken channel with inner top shadow and bottom light reflection)
 */
fun Modifier.sculpted3DSunkenWell(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color(0xFFEFF2F8)
): Modifier = this.drawBehind {
    val r = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    // Base sunken fill
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
    )

    // Inner top/left debossed shadow
    drawLine(
        color = Color(0x180E182A),
        start = Offset(r * 0.3f, 1.dp.toPx()),
        end = Offset(w - (r * 0.3f), 1.dp.toPx()),
        strokeWidth = 1.5.dp.toPx()
    )

    // Bottom inner specular bounce reflection
    drawLine(
        color = Color.White.copy(alpha = 0.95f),
        start = Offset(r * 0.3f, h - 1.dp.toPx()),
        end = Offset(w - (r * 0.3f), h - 1.dp.toPx()),
        strokeWidth = 1.dp.toPx()
    )
}

/**
 * Creates the continuous notched tab merge where the selected day tab (e.g. "Today")
 * flows into the card below it through concave inverse-radius corners.
 */
class NotchedTabMergeShape(
    private val tabWidth: Dp = 94.dp,
    private val tabHeight: Dp = 44.dp,
    private val cardRadius: Dp = 28.dp,
    private val tabRadius: Dp = 22.dp,
    private val inverseRadius: Dp = 20.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val tW = with(density) { tabWidth.toPx() }
        val tH = with(density) { tabHeight.toPx() }
        val cR = with(density) { cardRadius.toPx() }
        val tabR = with(density) { tabRadius.toPx() }
        val invR = with(density) { inverseRadius.toPx() }

        val path = Path().apply {
            // Start at bottom-left corner of the card
            moveTo(0f, h - cR)
            arcTo(
                rect = Rect(0f, h - 2 * cR, 2 * cR, h),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            // Bottom edge
            lineTo(w - cR, h)
            arcTo(
                rect = Rect(w - 2 * cR, h - 2 * cR, w, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            // Right edge up to card top
            lineTo(w, tH + cR)
            arcTo(
                rect = Rect(w - 2 * cR, tH, w, tH + 2 * cR),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            // Top edge of card moving left towards the tab
            lineTo(tW + invR, tH)

            // Concave inverse-radius corner: curves up from (tW + invR, tH) into (tW, tH - invR)
            arcTo(
                rect = Rect(tW, tH - 2 * invR, tW + 2 * invR, tH),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge of the tab moving up to tab top-right
            lineTo(tW, tabR)
            arcTo(
                rect = Rect(tW - 2 * tabR, 0f, tW, 2 * tabR),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )

            // Top edge of the tab moving left
            lineTo(tabR, 0f)
            arcTo(
                rect = Rect(0f, 0f, 2 * tabR, 2 * tabR),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )

            // Left edge down to bottom-left
            lineTo(0f, h - cR)
            close()
        }

        return Outline.Generic(path)
    }
}

/**
 * Concave notch at the task detail sheet's top-right cradling the 56px circular edit button.
 * Sheet has 32px top radius, notch dips down smoothly with inverse corners.
 */
class NotchedSheetWithCradleShape(
    private val topRadius: Dp = 32.dp,
    private val buttonCenterOffsetRight: Dp = 56.dp, // distance of cradle center from right edge
    private val cradleRadius: Dp = 36.dp,             // radius of circular notch cutout
    private val inverseRadius: Dp = 16.dp            // inverse transition radius
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val tR = with(density) { topRadius.toPx() }
        val cR = with(density) { cradleRadius.toPx() }
        val invR = with(density) { inverseRadius.toPx() }
        val cx = w - with(density) { buttonCenterOffsetRight.toPx() }
        val cy = 0f // top edge line

        val path = Path().apply {
            // Start at bottom-left
            moveTo(0f, h)
            lineTo(0f, tR)
            // Top-left round corner
            arcTo(
                rect = Rect(0f, 0f, 2 * tR, 2 * tR),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Line along top towards cradle entrance
            val cradleEntranceX = cx - cR - invR
            lineTo(cradleEntranceX, 0f)

            // Left inverse curve into cradle
            arcTo(
                rect = Rect(cradleEntranceX - invR, 0f, cradleEntranceX + invR, 2 * invR),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Concave circular cradle arc (under the button)
            arcTo(
                rect = Rect(cx - cR, cy - cR * 0.4f, cx + cR, cy + cR * 1.6f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )

            // Right inverse curve out of cradle
            val cradleExitX = cx + cR
            arcTo(
                rect = Rect(cradleExitX, 0f, cradleExitX + 2 * invR, 2 * invR),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Line towards top-right corner
            lineTo(w - tR, 0f)
            // Top-right corner
            arcTo(
                rect = Rect(w - 2 * tR, 0f, w, 2 * tR),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Down right edge
            lineTo(w, h)
            close()
        }

        return Outline.Generic(path)
    }
}

/**
 * Subtle film-grain noise overlay at 3% opacity to avoid banding and add richness.
 */
fun Modifier.subtleFilmGrainOverlay(): Modifier = this.drawWithContent {
    drawContent()
    // Stippled fine architectural stipple points (subtle texture on light ceramic)
    val dotColor = Color(0x05000000) // ~2% opacity architectural stipple
    val random = Random(42)
    val widthInt = size.width.toInt().coerceAtLeast(1)
    val heightInt = size.height.toInt().coerceAtLeast(1)

    val step = 14 // grid spacing for high performance
    for (x in 0 until widthInt step step) {
        for (y in 0 until heightInt step step) {
            val offsetX = x + random.nextInt(step)
            val offsetY = y + random.nextInt(step)
            drawCircle(
                color = dotColor,
                radius = 1f,
                center = Offset(offsetX.toFloat(), offsetY.toFloat())
            )
        }
    }
}
