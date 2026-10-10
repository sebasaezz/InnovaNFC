package cl.innova.nfc.emitter

import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.*

internal data class Diagnostics(
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            EmitterScreen(diagnostics, ::refresh)
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
