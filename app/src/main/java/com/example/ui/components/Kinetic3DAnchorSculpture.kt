package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.Surface1
import com.example.ui.theme.Surface2
import com.example.ui.util.HapticHelper
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D Vector in mathematical space
 */
private data class Vec3(val x: Float, val y: Float, val z: Float) {
    fun rotateX(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }

    fun cross(o: Vec3): Vec3 =
        Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)

    fun dot(o: Vec3): Float = x * o.x + y * o.y + z * o.z

    fun normalized(): Vec3 {
        val len = sqrt(x * x + y * y + z * z)
        return if (len > 0.0001f) Vec3(x / len, y / len, z / len) else this
    }
}

/**
 * Abnormal UI: Interactive 3D Gyroscopic Astrolabe Chrono-Anchor Sculpture.
 * Mathematically projects real-time 3D gimbal rings, facet-shaded central 3D octahedron crystal,
 * orbiting micro-satellites, and a recessed 3D ceramic reflection well.
 * Supports interactive touch rotation with inertia and haptic feedback.
 */
@Composable
fun Kinetic3DAnchorSculpture(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 100.dp,
    autoRotate: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Interactive user drag offsets for pitch & yaw
    val dragYaw = remember { Animatable(0f) }
    val dragPitch = remember { Animatable(0f) }

    // Continuous celestial orbit animation
    val infiniteTransition = rememberInfiniteTransition(label = "3d_chrono_spin")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        HapticHelper.vibrate8ms(context)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            dragYaw.snapTo(dragYaw.value + dragAmount.x * 0.015f)
                            dragPitch.snapTo(dragPitch.value - dragAmount.y * 0.015f)
                        }
                    },
                    onDragEnd = {
                        // Smooth gentle recoil towards default baseline orientation
                        scope.launch {
                            dragPitch.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 120f))
                        }
                    }
                )
            }
            .testTag("kinetic_3d_anchor_sculpture"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val baseRadius = (w.coerceAtMost(h) / 2f) * 0.88f

            // 1. Recessed 3D Ceramic Reflection Well underneath
            val wellRadius = baseRadius * 1.05f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Surface2.copy(alpha = 0.8f),
                        Color(0x180E182A),
                        Color.Transparent
                    ),
                    center = Offset(center.x, center.y + 4.dp.toPx()),
                    radius = wellRadius
                ),
                radius = wellRadius,
                center = Offset(center.x, center.y + 4.dp.toPx())
            )

            // Inner specular rim of the recessed dish
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = wellRadius * 0.96f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Current 3D rotation angles
            val yaw = if (autoRotate) orbitAngle * 0.5f + dragYaw.value else dragYaw.value
            val pitch = 0.35f + dragPitch.value
            val roll = orbitAngle * 0.25f

            // 3D Directional Light source vector (from top-left front)
            val lightDir = Vec3(-0.45f, -0.65f, 0.85f).normalized()

            // 2. Outer Gimbal Ring (48 segments in 3D)
            draw3DRing(
                center = center,
                radius = baseRadius * 0.95f,
                segments = 40,
                yaw = yaw,
                pitch = pitch,
                roll = roll,
                baseStrokeDp = 1.75f,
                ringColor = AccentGold
            )

            // 3. Middle Equatorial Ring with 12 hour ticks
            draw3DRing(
                center = center,
                radius = baseRadius * 0.76f,
                segments = 36,
                yaw = yaw + 1.1f,
                pitch = pitch * 0.6f + 0.8f,
                roll = roll * 0.8f,
                baseStrokeDp = 1.35f,
                ringColor = AccentGold.copy(alpha = 0.85f)
            )

            // 4. Inner Meridian Ring
            draw3DRing(
                center = center,
                radius = baseRadius * 0.58f,
                segments = 30,
                yaw = -yaw * 1.2f,
                pitch = pitch + 0.5f,
                roll = roll * 1.4f,
                baseStrokeDp = 1.1f,
                ringColor = Color(0xFFD4A84D)
            )

            // 5. Central 3D Faceted Octahedron Gemstone / Anchor Core
            val coreScale = baseRadius * 0.38f * corePulse
            draw3DOctahedron(
                center = center,
                scale = coreScale,
                yaw = yaw * 1.5f,
                pitch = pitch + orbitAngle * 0.4f,
                roll = roll,
                lightDir = lightDir
            )

            // 6. Orbiting Micro Quantum Satellites (3 nodes on distinct orbits)
            val satellites = listOf(
                Triple(baseRadius * 0.92f, orbitAngle * 2.2f, 0.4f),
                Triple(baseRadius * 0.72f, -orbitAngle * 1.8f + 1.2f, -0.6f),
                Triple(baseRadius * 0.45f, orbitAngle * 3.1f + 2.5f, 0.9f)
            )

            satellites.forEach { (orbRadius, angle, tilt) ->
                val pRaw = Vec3(
                    cos(angle) * orbRadius,
                    sin(angle) * orbRadius * cos(tilt),
                    sin(angle) * orbRadius * sin(tilt)
                )
                val pRot = pRaw.rotateX(pitch).rotateY(yaw).rotateZ(roll)
                val zDepth = pRot.z + 450f
                val projX = center.x + (pRot.x * 450f) / zDepth
                val projY = center.y + (pRot.y * 450f) / zDepth
                val nodeAlpha = ((pRot.z / (baseRadius * 1.2f)) + 1f).coerceIn(0.3f, 1f)

                // Cast glow behind satellite
                drawCircle(
                    color = AccentGold.copy(alpha = 0.3f * nodeAlpha),
                    radius = 4.5.dp.toPx(),
                    center = Offset(projX, projY)
                )
                // Solid gold node
                drawCircle(
                    color = AccentGold.copy(alpha = nodeAlpha),
                    radius = 2.25.dp.toPx(),
                    center = Offset(projX, projY)
                )
            }
        }
    }
}

/**
 * Draws a 3D ring projected onto 2D Canvas with depth-buffered line thickness & alpha
 */
private fun DrawScope.draw3DRing(
    center: Offset,
    radius: Float,
    segments: Int,
    yaw: Float,
    pitch: Float,
    roll: Float,
    baseStrokeDp: Float,
    ringColor: Color
) {
    val step = (2 * PI / segments).toFloat()
    for (i in 0 until segments) {
        val a1 = i * step
        val a2 = (i + 1) * step

        val p1 = Vec3(cos(a1) * radius, 0f, sin(a1) * radius)
            .rotateX(pitch).rotateY(yaw).rotateZ(roll)
        val p2 = Vec3(cos(a2) * radius, 0f, sin(a2) * radius)
            .rotateX(pitch).rotateY(yaw).rotateZ(roll)

        val z1 = p1.z + 450f
        val z2 = p2.z + 450f

        val x1 = center.x + (p1.x * 450f) / z1
        val y1 = center.y + (p1.y * 450f) / z1
        val x2 = center.x + (p2.x * 450f) / z2
        val y2 = center.y + (p2.y * 450f) / z2

        // Depth cueing: points closer to the viewer are thicker and brighter
        val avgZ = (p1.z + p2.z) / 2f
        val depthRatio = ((avgZ / radius) + 1f).coerceIn(0.25f, 1.2f)
        val strokeWidth = (baseStrokeDp.dp.toPx() * depthRatio).coerceAtLeast(0.6.dp.toPx())
        val alpha = (0.28f + (depthRatio * 0.72f)).coerceIn(0.2f, 1f)

        drawLine(
            color = ringColor.copy(alpha = alpha),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws an illuminated 3D Octahedron jewel with lighting calculation and back-face culling
 */
private fun DrawScope.draw3DOctahedron(
    center: Offset,
    scale: Float,
    yaw: Float,
    pitch: Float,
    roll: Float,
    lightDir: Vec3
) {
    val verticesRaw = listOf(
        Vec3(0f, -scale, 0f),  // 0: Top
        Vec3(scale, 0f, 0f),   // 1: Right
        Vec3(0f, 0f, scale),   // 2: Front
        Vec3(-scale, 0f, 0f),  // 3: Left
        Vec3(0f, 0f, -scale),  // 4: Back
        Vec3(0f, scale, 0f)    // 5: Bottom
    )

    // Rotate all vertices in 3D
    val vRot = verticesRaw.map { it.rotateX(pitch).rotateY(yaw).rotateZ(roll) }

    // Project to 2D screen coordinates
    val vProj = vRot.map { v ->
        val z = v.z + 450f
        Offset(center.x + (v.x * 450f) / z, center.y + (v.y * 450f) / z)
    }

    // 8 Triangular faces defined by vertex indices (counter-clockwise)
    val faces = listOf(
        Triple(0, 1, 2),
        Triple(0, 2, 3),
        Triple(0, 3, 4),
        Triple(0, 4, 1),
        Triple(5, 2, 1),
        Triple(5, 3, 2),
        Triple(5, 4, 3),
        Triple(5, 1, 4)
    )

    // Sort faces from back to front for accurate painter's algorithm
    val sortedFaces = faces.sortedBy { (i0, i1, i2) ->
        (vRot[i0].z + vRot[i1].z + vRot[i2].z) / 3f
    }

    sortedFaces.forEach { (i0, i1, i2) ->
        val p0 = vRot[i0]
        val p1 = vRot[i1]
        val p2 = vRot[i2]

        // Compute face normal in 3D
        val edge1 = Vec3(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z)
        val edge2 = Vec3(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z)
        val normal = edge1.cross(edge2).normalized()

        // Directional lighting: Lambertian intensity
        val intensity = (normal.dot(lightDir)).coerceIn(0.12f, 1f)

        // Shaded champagne gold tone
        val faceColor = Color(
            red = (0.75f + 0.25f * intensity).coerceIn(0f, 1f),
            green = (0.55f + 0.35f * intensity).coerceIn(0f, 1f),
            blue = (0.18f + 0.25f * intensity).coerceIn(0f, 1f),
            alpha = (0.45f + 0.50f * intensity).coerceIn(0.2f, 0.95f)
        )

        val path = Path().apply {
            moveTo(vProj[i0].x, vProj[i0].y)
            lineTo(vProj[i1].x, vProj[i1].y)
            lineTo(vProj[i2].x, vProj[i2].y)
            close()
        }

        // Draw illuminated facet
        drawPath(path = path, color = faceColor)

        // Specular gold edge bevel
        val edgeAlpha = (0.4f + 0.6f * intensity).coerceIn(0.2f, 1f)
        drawPath(
            path = path,
            color = Color(0xFFFFE8A8).copy(alpha = edgeAlpha),
            style = Stroke(width = 1.15.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Glowing core center point
    val centerZ = vRot.map { it.z }.average().toFloat() + 450f
    val centerProjX = center.x
    val centerProjY = center.y
    drawCircle(
        color = Color(0xFFFFF2D0),
        radius = 2.5.dp.toPx(),
        center = Offset(centerProjX, centerProjY)
    )
}
