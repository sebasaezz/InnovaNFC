package cl.innova.nfc.emitter

import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.innova.nfc.protocol.NfcProtocol

private data class Diagnostics(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val hce: Boolean = false,
    val secureSupported: Boolean? = null,
    val secureEnabled: Boolean? = null,
)

class MainActivity : ComponentActivity() {
    private var diagnostics by mutableStateOf(Diagnostics())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("InnovaNFC · Emitter", style = MaterialTheme.typography.headlineMedium)
                        Text("Galaxy S22 · Opaque identity card", style = MaterialTheme.typography.titleMedium)
                        Text("NFC available: ${diagnostics.available.yesNo()}")
                        Text("NFC enabled: ${diagnostics.enabled.yesNo()}")
                        Text("HCE supported: ${diagnostics.hce.yesNo()}")
                        Text("Secure NFC supported: ${diagnostics.secureSupported.yesNo()}")
                        Text("Secure NFC enabled: ${diagnostics.secureEnabled.yesNo()}")
                        Text("Demo person ID", style = MaterialTheme.typography.titleMedium)
                        Text(NfcProtocol.DEMO_PERSON_ID, style = MaterialTheme.typography.bodyLarge)
                        Text("Only this ID is transmitted. Health profiles stay on the reader.")
                        Text("The NFC service works without this screen open. Device settings may restrict locked or screen-off operation.")
                        Button(onClick = ::refresh) { Text("Refresh diagnostics") }
                    }
                }
            }
        }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        diagnostics = Diagnostics(
            available = adapter != null,
            enabled = adapter?.isEnabled == true,
            hce = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION),
            secureSupported = if (Build.VERSION.SDK_INT >= 29) adapter?.isSecureNfcSupported else null,
            secureEnabled = if (Build.VERSION.SDK_INT >= 29) adapter?.isSecureNfcEnabled else null,
        )
    }
}

private fun Boolean?.yesNo() = when (this) { true -> "Yes"; false -> "No"; null -> "Unavailable" }
