package cl.innova.nfc.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReaderScreen(state: ReaderState, debug: NfcDebug, onReset: () -> Unit) {
    var showDebug by remember { mutableStateOf(false) }
    // Open a completed emergency snapshot at its identity, regardless of previous scrolling.
    val scroll = key(state::class) { rememberScrollState() }
    InnovaTheme {
        Scaffold(containerColor = InnovaColors.Background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll)
                .padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 24.dp)) {
                BrandHeader("SEGURIDAD")
                Spacer(Modifier.height(32.dp))
                when (state) {
                    ReaderState.Ready -> ScanState(false)
                    ReaderState.Reading -> ScanState(true)
                    is ReaderState.ProfileFound -> ProfileSnapshot(state.profile)
                    is ReaderState.UnknownPerson -> OperationalMessage("SIN PERFIL LOCAL", "Identidad no registrada",
                        "No hay un perfil asociado a esta identidad.", false)
                    is ReaderState.Error -> OperationalMessage("LECTURA INTERRUMPIDA", "No pudimos leer\nla identidad",
                        spanishError(state.message), true)
                }
                if (state != ReaderState.Ready && state != ReaderState.Reading) {
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onReset, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = InnovaColors.Purple, contentColor = InnovaColors.Text)) {
                        Text(if (state is ReaderState.ProfileFound) "Nueva lectura" else "Volver a intentar")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Separa los teléfonos y vuelve a acercarlos.", color = InnovaColors.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(28.dp))
                HorizontalDivider(color = InnovaColors.Border)
                TextButton(onClick = { showDebug = !showDebug }, contentPadding = PaddingValues(vertical = 16.dp)) {
                    Text(if (showDebug) "Diagnóstico NFC  −" else "Diagnóstico NFC  +", color = InnovaColors.SecondaryText)
                }
                if (showDebug) {
                    DiagnosticRow("Etiqueta detectada", if (debug.tagDetected) "Sí" else "No")
                    DiagnosticRow("IsoDep conectado", if (debug.isoDepConnected) "Sí" else "No")
                    DiagnosticRow("SELECT", when (debug.selectResult) { "Success" -> "Correcto"; "Failure" -> "Falló"; else -> "Sin iniciar" })
                    Text("ID recibido", color = InnovaColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    Text(debug.personId?.chunked(16)?.joinToString("\n") ?: "Ninguno",
                        fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    DiagnosticRow("Tiempo transcurrido", "${debug.elapsedMs} ms")
                }
            }
        }
    }
}

@Composable
private fun ScanState(reading: Boolean) {
    StatusIndicator(if (reading) "CONTACTO DETECTADO" else "LECTOR DISPONIBLE",
        if (reading) InnovaColors.Lavender else InnovaColors.Success)
    Spacer(Modifier.height(16.dp))
    Text(if (reading) "Leyendo\nidentidad" else "Acerca el teléfono\ndel asistente", style = MaterialTheme.typography.headlineLarge)
    Box(Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 30.dp), contentAlignment = Alignment.Center) {
        NfcTarget(Modifier.widthIn(max = 272.dp).fillMaxWidth().aspectRatio(1f), animate = true, reading = reading)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (reading) "02" else "01", color = InnovaColors.Lavender, fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleMedium)
        Text(if (reading) "Mantén los teléfonos juntos" else "Alinea la parte posterior de ambos teléfonos",
            color = InnovaColors.SecondaryText, style = MaterialTheme.typography.bodyLarge)
    }
    Spacer(Modifier.height(36.dp))
    OperationalLabel("PERFILES LOCALES · DEMOSTRACIÓN")
}

@Composable
private fun ProfileSnapshot(profile: HealthProfile) {
    StatusIndicator("IDENTIDAD CONFIRMADA", InnovaColors.Success)
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(profile.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text("${profile.age} años", style = MaterialTheme.typography.titleMedium, color = InnovaColors.SecondaryText)
        }
        Column(horizontalAlignment = Alignment.End) {
            OperationalLabel("SANGRE")
            Text(profile.bloodType, color = InnovaColors.Lavender, style = MaterialTheme.typography.headlineLarge)
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("Perfil ficticio de demostración", color = InnovaColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(28.dp))
    if (profile.allergies.isNotEmpty()) {
        Column(Modifier.fillMaxWidth().background(InnovaColors.Critical.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
            .padding(18.dp)) {
            OperationalLabel("ALERGIAS · ATENCIÓN", InnovaColors.Critical)
            Spacer(Modifier.height(8.dp))
            Text(profile.allergies.joinToString("\n") { spanishMedical(it) },
                style = MaterialTheme.typography.titleLarge, color = InnovaColors.Text)
        }
    } else {
        OperationalLabel("ALERGIAS")
        Spacer(Modifier.height(6.dp))
        Text("Sin alergias registradas", style = MaterialTheme.typography.titleMedium)
    }
    Spacer(Modifier.height(26.dp))
    MedicalSection("CONDICIONES RELEVANTES", profile.conditions, emphasized = true)
    Spacer(Modifier.height(22.dp))
    MedicalSection("MEDICAMENTOS", profile.medications)
    Spacer(Modifier.height(28.dp))
    HorizontalDivider(color = InnovaColors.Border)
    Spacer(Modifier.height(22.dp))
    OperationalLabel("CONTACTO DE EMERGENCIA", InnovaColors.Lavender)
    Spacer(Modifier.height(10.dp))
    Text(profile.emergencyContactName, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))
    Text(profile.emergencyContactPhone, style = MaterialTheme.typography.headlineMedium)
}

@Composable
private fun MedicalSection(label: String, values: List<String>, emphasized: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        if (emphasized && values.isNotEmpty()) {
            Box(Modifier.width(3.dp).height(48.dp).background(InnovaColors.Lavender))
        }
        Column {
            OperationalLabel(label)
            Spacer(Modifier.height(8.dp))
            Text(values.joinToString("\n") { spanishMedical(it) }.ifEmpty { "Sin registros" },
                style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun OperationalMessage(label: String, title: String, message: String, error: Boolean) {
    OperationalLabel(label, if (error) InnovaColors.Critical else InnovaColors.Lavender)
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.headlineLarge)
    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        NfcTarget(Modifier.size(180.dp))
    }
    Text(message, style = MaterialTheme.typography.bodyLarge, color = InnovaColors.SecondaryText)
}

// Presentation-only translations; repository data and NFC result messages stay unchanged.
private fun spanishMedical(value: String): String = when (value) {
    "Penicillin" -> "Penicilina"
    "Peanuts" -> "Maní"
    "Asthma" -> "Asma"
    "Type 2 diabetes" -> "Diabetes tipo 2"
    "Epilepsy" -> "Epilepsia"
    "Salbutamol inhaler as prescribed" -> "Inhalador de salbutamol según prescripción"
    "Metformin as prescribed" -> "Metformina según prescripción"
    "Levetiracetam as prescribed" -> "Levetiracetam según prescripción"
    else -> value
}

private fun spanishError(message: String): String = when (message) {
    "This device has no NFC adapter." -> "Este teléfono no tiene NFC."
    "Enable NFC in device settings, then return here." -> "Activa NFC en ajustes y vuelve a esta pantalla."
    "Detected tag does not support ISO-DEP." -> "La tarjeta detectada no es compatible."
    "Emitter returned an invalid NFC response." -> "La respuesta NFC no es válida. Vuelve a acercar los teléfonos."
    "NFC connection lost or timed out. Hold the phones together and try again." -> "Se perdió la conexión. Mantén los teléfonos juntos y reintenta."
    "NFC access is unavailable. Check device NFC settings.", "NFC access is unavailable. Check device settings." -> "Revisa el acceso NFC en los ajustes del teléfono."
    "Could not enable NFC reader mode. Return to this screen to retry." -> "Vuelve a esta pantalla para activar el lector NFC."
    "Could not close the NFC connection. Try again." -> "La conexión NFC no se cerró correctamente. Reintenta."
    "No person ID received." -> "No recibimos una identidad. Vuelve a acercar los teléfonos."
    else -> "No se pudo completar la lectura. Vuelve a intentar."
}
