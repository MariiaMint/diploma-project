package frontend.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = Primary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val ArianTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 57.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 45.sp,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 16.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    labelMedium = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ArianFontFamily,
        fontSize = 12.sp
    )
)

@Composable
fun MyMedBookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = ArianTypography
    ) {
        Surface(
            color = Color.Transparent,
            contentColor = DarkColors.onBackground
        ) {
            content()
        }
    }
}