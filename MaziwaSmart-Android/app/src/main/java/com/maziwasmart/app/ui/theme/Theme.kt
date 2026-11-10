
package com.maziwasmart.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// MaziwaSmart Fresh Dairy Colour Palette

val Green = Color(0xFF8DBB91)
val GreenDeep = Color(0xFF315C45)
val GreenSoft = Color(0xFFE8F0E6)

val Yellow = Color(0xFFE8BD58)
val YellowSoft = Color(0xFFFFF4D9)
val YellowText = Color(0xFF9B701D)

val Orange = Color(0xFFE9A16B)
val OrangeSoft = Color(0xFFFFEBDC)
val OrangeText = Color(0xFFB96735)

val Brown = Color(0xFF303B34)
val BrownSoft = Color(0xFFE9EDE8)

val Cream = Color(0xFFF5F3E9)
val FieldBg = Color(0xFFFAFBF7)
val LineColor = Color(0xFFDCE4DA)
val Muted = Color(0xFF7B857D)

private val Colors = lightColorScheme(
    primary = GreenDeep,
    onPrimary = Color.White,

    secondary = Yellow,
    onSecondary = Brown,

    tertiary = Orange,
    onTertiary = Brown,

    background = Cream,
    onBackground = Brown,

    surface = Color.White,
    onSurface = Brown,

    surfaceVariant = FieldBg,
    onSurfaceVariant = Muted,

    outline = LineColor
)

@Composable
fun MaziwaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        content = content
    )
}
