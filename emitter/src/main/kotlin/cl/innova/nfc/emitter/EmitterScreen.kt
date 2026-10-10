package cl.innova.nfc.emitter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import cl.innova.nfc.protocol.NfcProtocol

@Composable
internal fun EmitterScreen(diagnostics: Diagnostics, onRefresh: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val active = diagnostics.available && diagnostics.enabled && diagnostics.hce
    val title = when {
        !diagnostics.available -> "NFC no disponible"
        !diagnostics.hce -> "Identidad no compatible"
        !diagnostics.enabled -> "Activa NFC para continuar"
        else -> "Identidad de\nemergencia activa"
    }
    InnovaTheme {
        Scaffold(containerColor = InnovaColors.Background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp).padding(top = 20.dp, bottom = 24.dp)) {
                BrandHeader("ASISTENTE")
                Spacer(Modifier.height(46.dp))
                StatusIndicator(if (active) "IDENTIDAD DISPONIBLE" else "REQUIERE ATENCIÓN",
                    if (active) InnovaColors.Success else InnovaColors.Lavender)
                Spacer(Modifier.height(14.dp))
                Text(title, style = MaterialTheme.typography.headlineLarge)
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    NfcTarget(Modifier.widthIn(max = 256.dp).fillMaxWidth().aspectRatio(1f), animate = active)
                }
                Text(if (active) "Acerca tu teléfono al lector" else "Revisa la compatibilidad de tu teléfono",
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(if (active) "Tu identidad sigue disponible al salir de la app." else
                    if (!diagnostics.enabled && diagnostics.available && diagnostics.hce) "Activa NFC en los ajustes del dispositivo."
                    else "Este dispositivo necesita NFC y emulación de tarjeta.",
                    color = InnovaColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(32.dp))
                OperationalLabel("IDENTIFICADOR DE DEMOSTRACIÓN")
                Spacer(Modifier.height(8.dp))
                Text(NfcProtocol.DEMO_PERSON_ID.chunked(16).joinToString("\n"),
                    color = InnovaColors.SecondaryText, fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(28.dp))
                HorizontalDivider(color = InnovaColors.Border)
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(vertical = 16.dp)) {
                    Text(if (expanded) "Diagnóstico NFC  −" else "Diagnóstico NFC  +",
                        color = InnovaColors.SecondaryText)
                }
                if (expanded) {
                    DiagnosticRow("NFC disponible", diagnostics.available.yesNo())
                    DiagnosticRow("NFC activado", diagnostics.enabled.yesNo())
                    DiagnosticRow("HCE compatible", diagnostics.hce.yesNo())
                    DiagnosticRow("NFC seguro compatible", diagnostics.secureSupported.yesNo())
                    DiagnosticRow("NFC seguro activado", diagnostics.secureEnabled.yesNo())
                    Spacer(Modifier.height(10.dp))
                    Text("El bloqueo y la pantalla apagada dependen de NFC seguro y de los ajustes del dispositivo.",
                        style = MaterialTheme.typography.bodyMedium, color = InnovaColors.SecondaryText)
                    TextButton(onClick = onRefresh, contentPadding = PaddingValues(vertical = 12.dp)) { Text("Actualizar diagnóstico") }
                }
            }
        }
    }
}

private fun Boolean?.yesNo() = when (this) { true -> "Sí"; false -> "No"; null -> "No disponible" }
