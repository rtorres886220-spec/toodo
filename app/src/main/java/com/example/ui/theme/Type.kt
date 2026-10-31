package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.R

// Typography: Rounded geometric sans (Plus Jakarta Sans)
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Light),
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium)
)

// Metadata: Geist Mono
val GeistMono = FontFamily(
    Font(R.font.geist_mono, FontWeight.Light),
    Font(R.font.geist_mono, FontWeight.Normal)
)


val DisplayNumeralLarge = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Light,
    fontSize = 56.sp,
    lineHeight = 60.sp,
    letterSpacing = (-0.02).em
)

val DisplayNumeralMedium = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Light,
    fontSize = 44.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.02).em
)

// Screen title: weight 300 at 42px
val ScreenTitleLarge = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Light,
    fontSize = 42.sp,
    lineHeight = 46.sp,
    letterSpacing = (-0.02).em
)

// Titles: weight 400 at 17-20px
val TitleLarge = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.01).em
)

val TitleMedium = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.01).em
)

val TitleSmall = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.01).em
)

// Body: weight 300 at 15px minimum
val BodyRegular = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Light,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

val BodyLarge = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Light,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// Metadata: Geist Mono 300 at 11px uppercase, 0.14em tracking
val MetadataMono = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Light,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.14.em
)

val MetadataMonoMedium = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Light,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.14.em
)



val Typography = Typography(
    displayLarge = DisplayNumeralLarge,
    displayMedium = DisplayNumeralMedium,
    headlineMedium = ScreenTitleLarge,
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Light,
        fontSize = 24.sp,
        letterSpacing = (-0.02).em
    ),
    titleLarge = TitleLarge,
    titleMedium = TitleMedium,
    titleSmall = TitleSmall,
    bodyLarge = BodyLarge,
    bodyMedium = BodyRegular,
    bodySmall = BodyRegular,
    labelLarge = MetadataMonoMedium,
    labelMedium = MetadataMono,
    labelSmall = MetadataMono
)
