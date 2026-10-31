package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Design language: "Sculpted 3D Ceramic Alabaster & Champagne Gold"
// Clean, luminous light mode with multi-tier ambient occlusion shadows, specular 3D light bevels,
// and burnished champagne gold telemetry accents.

val SoftLightBackground = Color(0xFFF6F7FA)
val SoftLightRadialLift = Color(0xFFFFFFFF)

// Backward-compatibility aliases for existing imports
val SoftDarkBackground = SoftLightBackground
val SoftDarkRadialLift = SoftLightRadialLift

// Surface tokens: Pure ceramic white (Surface1) & sculpted matte container (Surface2)
val Surface1 = Color(0xFFFFFFFF)
val Surface2 = Color(0xFFEFF2F8)
val SurfaceSunken = Color(0xFFE4E8F2)

// Raised surface specular highlights and structural boundaries
val SurfaceHighlight = Color(0x99FFFFFF)
val HairlineColor = Color(0x180F172A)
val ShadowAmbient = Color(0x140E182A)
val ShadowOcclusion = Color(0x16070E1A)

// Text tokens: primary deep obsidian onyx #0F141E, secondary slate #555E6F, accessible tertiary slate #5C687A
val TextPrimary = Color(0xFF0F141E)
val TextSecondary = Color(0xFF555E6F)
val TextTertiary = Color(0xFF5C687A)

// Primary accent: darker champagne gold #946000, selected for readable text on ceramic surfaces.
val AccentGold = Color(0xFF946000)
val GoldButtonBackground = Color(0xFFDCAB4C)
val AccentGoldDim = Color(0x1CBF8828)
val AccentGoldGlow = Color(0x14BF8828)
// Dark brown label/icon color maintains contrast on the warm gold controls.
val GoldButtonText = Color(0xFF1F1606)

// Urgency: Ember crimson coral #EB442C (for overdue tasks)
val UrgencyCoral = Color(0xFFB42318)
val UrgencyCoralDim = Color(0x1EB42318)

// Category tints: luminous frosted pastel ceramic cards with rich contrasting foreground typography
val TintOrchidStart = Color(0xFFFAF4FC)
val TintOrchidEnd = Color(0xFFF1E3F7)
val TintOrchidFg = Color(0xFF882E9E)

val TintSageStart = Color(0xFFF1F9F3)
val TintSageEnd = Color(0xFFE1F2E6)
val TintSageFg = Color(0xFF1E7A3E)

val TintApricotStart = Color(0xFFFFF6ED)
val TintApricotEnd = Color(0xFFFEE6D2)
val TintApricotFg = Color(0xFFB55512)

val TintSlateStart = Color(0xFFF2F6FC)
val TintSlateEnd = Color(0xFFE3EDFA)
val TintSlateFg = Color(0xFF2658A0)

val TintRoseStart = Color(0xFFFFF2F4)
val TintRoseEnd = Color(0xFFFEDFE5)
val TintRoseFg = Color(0xFFA1233D)

// Swipe wash colors in light mode
val SwipeWashToday = Color(0x28BF8828)
val SwipeWashSnooze = Color(0x202658A0)
val SwipeWashDone = Color(0x201E7A3E)
