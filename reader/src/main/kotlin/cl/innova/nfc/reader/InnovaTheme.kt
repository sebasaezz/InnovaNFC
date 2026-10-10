package cl.innova.nfc.reader

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object InnovaColors {
    val Background = Color(0xFF12091F)
    val Surface = Color(0xFF1D1230)
    val Purple = Color(0xFF6C3BFF)
    val Violet = Color(0xFF8B5CF6)
    val Lavender = Color(0xFFC4A7FF)
    val Text = Color.White
    val SecondaryText = Color(0xFFC9C3D5)
    val Border = Color.White.copy(alpha = 0.10f)
    val Critical = Color(0xFFFF4D6D)
    val Success = Color(0xFF5DE2B5)
}

private val palette = darkColorScheme(
    primary = InnovaColors.Lavender, onPrimary = InnovaColors.Background,
    primaryContainer = InnovaColors.Purple, onPrimaryContainer = InnovaColors.Text,
    secondary = InnovaColors.Violet, onSecondary = InnovaColors.Text,
    secondaryContainer = InnovaColors.Surface, onSecondaryContainer = InnovaColors.Lavender,
    tertiary = InnovaColors.Lavender, onTertiary = InnovaColors.Background,
    tertiaryContainer = InnovaColors.Surface, onTertiaryContainer = InnovaColors.Text,
    background = InnovaColors.Background, onBackground = InnovaColors.Text,
    surface = InnovaColors.Background, onSurface = InnovaColors.Text,
    surfaceVariant = InnovaColors.Surface, onSurfaceVariant = InnovaColors.SecondaryText,
    surfaceTint = InnovaColors.Purple, outline = InnovaColors.Border, outlineVariant = InnovaColors.Border,
    error = InnovaColors.Critical, onError = InnovaColors.Background,
    errorContainer = InnovaColors.Surface, onErrorContainer = InnovaColors.Critical,
    inverseSurface = InnovaColors.Text, inverseOnSurface = InnovaColors.Background,
    inversePrimary = InnovaColors.Purple, scrim = InnovaColors.Background,
)

private val type = Typography(
    displayLarge = TextStyle(fontSize = 56.sp, lineHeight = 58.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp),
    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp),
)

@Composable
internal fun InnovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette, typography = type, content = content)
}

@Composable
internal fun BrandHeader(role: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Row {
            Text("innova", style = MaterialTheme.typography.titleLarge)
            Text("NFC", color = InnovaColors.Lavender, style = MaterialTheme.typography.titleLarge)
        }
        Text(role, color = InnovaColors.SecondaryText, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun OperationalLabel(text: String, color: Color = InnovaColors.SecondaryText) {
    Text(text, color = color, style = MaterialTheme.typography.labelSmall)
}

@Composable
internal fun StatusIndicator(text: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Canvas(Modifier.size(6.dp)) { drawCircle(color) }
        OperationalLabel(text, color)
    }
}

/** Interrupted rings and a paired-device mark; no remote assets or icon dependency. */
@Composable
internal fun NfcTarget(modifier: Modifier = Modifier, animate: Boolean = false, reading: Boolean = false) {
    val pulse = if (animate) {
        val transition = rememberInfiniteTransition(label = "NFC proximity")
        val value by transition.animateFloat(initialValue = 0.25f, targetValue = 0.7f,
            animationSpec = infiniteRepeatable(tween(if (reading) 900 else 2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "Signal opacity")
        value
    } else 0.35f
    Canvas(modifier.semantics { contentDescription = if (reading) "Leyendo identidad NFC" else "Zona de contacto NFC" }) {
        val unit = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        fun arc(radius: Float, start: Float, sweep: Float, color: Color, width: Float) {
            drawArc(color, start, sweep, false, center - Offset(radius, radius), Size(radius * 2, radius * 2),
                style = Stroke(width = width, cap = StrokeCap.Round))
        }
        drawCircle(InnovaColors.Surface, unit * 0.34f, center)
        arc(unit * 0.46f, -75f, 145f, InnovaColors.Violet.copy(alpha = pulse), 1.5.dp.toPx())
        arc(unit * 0.46f, 105f, 145f, InnovaColors.Violet.copy(alpha = pulse), 1.5.dp.toPx())
        arc(unit * 0.37f, -52f, 110f, InnovaColors.Purple, 2.dp.toPx())
        arc(unit * 0.37f, 128f, 110f, InnovaColors.Purple.copy(alpha = 0.55f), 2.dp.toPx())
        arc(unit * 0.27f, -48f, 96f, InnovaColors.Lavender.copy(alpha = 0.65f), 2.dp.toPx())
        arc(unit * 0.27f, 132f, 96f, InnovaColors.Lavender.copy(alpha = 0.65f), 2.dp.toPx())
        val stroke = 3.dp.toPx()
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(InnovaColors.Lavender,
            center + Offset(x1 * unit, y1 * unit), center + Offset(x2 * unit, y2 * unit), stroke, StrokeCap.Round)
        line(-0.10f, 0.12f, -0.10f, -0.12f)
        line(-0.10f, -0.12f, 0.05f, 0.06f)
        line(0.10f, -0.12f, 0.10f, 0.12f)
        line(0.10f, 0.12f, -0.05f, -0.06f)
    }
}

@Composable
internal fun DiagnosticRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), color = InnovaColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
    }
}

