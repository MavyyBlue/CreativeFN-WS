package dev.creativelogic.mobile.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BuilderColors = darkColorScheme(
    primary = Color(0xFFB4F578), onPrimary = Color(0xFF20320D),
    primaryContainer = Color(0xFF293C1C), onPrimaryContainer = Color(0xFFD3F6BA),
    secondary = Color(0xFFB7B1FF), onSecondary = Color(0xFF292349),
    secondaryContainer = Color(0xFF2E2C48), onSecondaryContainer = Color(0xFFE1DDFF),
    background = Color(0xFF101116), onBackground = Color(0xFFF1F0F6),
    surface = Color(0xFF101116), onSurface = Color(0xFFF1F0F6),
    surfaceVariant = Color(0xFF252631), onSurfaceVariant = Color(0xFFB6B6C6),
    surfaceContainer = Color(0xFF1A1B23), surfaceContainerLow = Color(0xFF15161D),
    surfaceContainerHigh = Color(0xFF242530), outline = Color(0xFF737484),
    outlineVariant = Color(0xFF343542), error = Color(0xFFFFB4AB),
)

@Composable
fun CreativeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BuilderColors,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp),
        ),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.7).sp),
            headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 28.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
            bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp),
            labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        ),
        content = content,
    )
}
