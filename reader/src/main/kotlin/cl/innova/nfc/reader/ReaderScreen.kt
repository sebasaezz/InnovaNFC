package cl.innova.nfc.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReaderScreen(state: ReaderState, debug: NfcDebug, onReset: () -> Unit) {
    var showDebug by remember { mutableStateOf(false) }
    MaterialTheme {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("InnovaNFC · Reader", style = MaterialTheme.typography.headlineMedium)
                Text("Offline emergency profile · Fictional demo", style = MaterialTheme.typography.bodyMedium)
                when (state) {
                    ReaderState.Ready -> {
                        Text("Ready", style = MaterialTheme.typography.headlineSmall)
                        Text("Hold the back of this Galaxy S25 near the emitter’s NFC antenna.")
                    }
                    ReaderState.Reading -> {
                        Text("Reading…", style = MaterialTheme.typography.headlineSmall)
                        CircularProgressIndicator()
                        Text("Keep both phones still.")
                    }
                    is ReaderState.ProfileFound -> {
                        Text("Profile found", style = MaterialTheme.typography.labelLarge)
                        ProfileCard(state.profile)
                    }
                    is ReaderState.UnknownPerson -> {
                        Text("Unknown person", style = MaterialTheme.typography.headlineSmall)
                        Text("The received ID has no profile in this local demo repository.")
                    }
                    is ReaderState.Error -> {
                        Text("Error", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                        Text(state.message)
                    }
                }
                if (state != ReaderState.Reading && state != ReaderState.Ready) {
                    Button(onClick = onReset) { Text("Clear profile") }
                    Text("Remove the phones, then tap again to read another ID.")
                }
                HorizontalDivider()
                TextButton(onClick = { showDebug = !showDebug }) {
                    Text(if (showDebug) "Hide NFC diagnostics" else "Show NFC diagnostics")
                }
                if (showDebug) {
                    Text("Tag detected: ${debug.tagDetected}")
                    Text("IsoDep connected: ${debug.isoDepConnected}")
                    Text("SELECT: ${debug.selectResult}")
                    Text("ID received: ${debug.personId ?: "None"}")
                    Text("Elapsed time: ${debug.elapsedMs} ms")
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(profile: HealthProfile) {
    Text(profile.name, style = MaterialTheme.typography.headlineLarge)
    Text("Age ${profile.age}", style = MaterialTheme.typography.titleMedium)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Blood type: ${profile.bloodType}", style = MaterialTheme.typography.headlineMedium)
            ProfileField("Allergies", profile.allergies.joinToString("\n").ifEmpty { "None recorded" }, true)
            ProfileField("Relevant conditions", profile.conditions.joinToString("\n").ifEmpty { "None recorded" })
            ProfileField("Medications", profile.medications.joinToString("\n").ifEmpty { "None recorded" })
            HorizontalDivider()
            ProfileField("Emergency contact", "${profile.emergencyContactName}\n${profile.emergencyContactPhone}")
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, prominent: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.titleLarge,
            color = if (prominent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}
