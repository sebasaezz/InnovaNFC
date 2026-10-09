package cl.innova.nfc.reader

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val busy = AtomicBoolean(false)
    private val generation = AtomicInteger(0)
    private val reader = NfcReader()
    private var adapter: NfcAdapter? = null
    private var state by mutableStateOf<ReaderState>(ReaderState.Ready)
    private var debug by mutableStateOf(NfcDebug())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adapter = NfcAdapter.getDefaultAdapter(this)
        setContent { ReaderScreen(state, debug, ::reset) }
    }

    override fun onResume() {
        super.onResume()
        val current = generation.incrementAndGet()
        val nfc = adapter
        if (nfc == null) { state = ReaderState.Error("This device has no NFC adapter."); return }
        if (!nfc.isEnabled) { state = ReaderState.Error("Enable NFC in device settings, then return here."); return }
        if (state is ReaderState.Error || state == ReaderState.Reading) reset()
        try {
            nfc.enableReaderMode(this, { tag ->
                if (generation.get() == current && busy.compareAndSet(false, true)) {
                    executor.execute {
                        try {
                            if (generation.get() == current) {
                                runOnUiThread {
                                    if (generation.get() == current) {
                                        state = ReaderState.Reading
                                        debug = NfcDebug(tagDetected = true)
                                    }
                                }
                                val result = reader.read(tag)
                                runOnUiThread {
                                    if (generation.get() == current) {
                                        state = result.state
                                        debug = result.debug
                                    }
                                }
                            }
                        } finally { busy.set(false) }
                    }
                }
            }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        } catch (_: SecurityException) {
            state = ReaderState.Error("NFC access is unavailable. Check device settings.")
        } catch (_: IllegalStateException) {
            state = ReaderState.Error("Could not enable NFC reader mode. Return to this screen to retry.")
        }
    }

    override fun onPause() {
        generation.incrementAndGet()
        adapter?.disableReaderMode(this)
        super.onPause()
    }

    override fun onDestroy() { executor.shutdown(); super.onDestroy() }

    private fun reset() {
        state = ReaderState.Ready
        debug = NfcDebug()
    }
}
