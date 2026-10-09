package cl.innova.nfc.reader

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.SystemClock
import cl.innova.nfc.protocol.NfcProtocol
import java.io.IOException

data class NfcDebug(
    val tagDetected: Boolean = false,
    val isoDepConnected: Boolean = false,
    val selectResult: String = "Not attempted",
    val personId: String? = null,
    val elapsedMs: Long = 0,
)

sealed interface ReaderState {
    data object Ready : ReaderState
    data object Reading : ReaderState
    data class ProfileFound(val profile: HealthProfile) : ReaderState
    data class UnknownPerson(val id: String) : ReaderState
    data class Error(val message: String) : ReaderState
}

data class ReadResult(val state: ReaderState, val debug: NfcDebug)

class NfcReader(private val repository: HealthRepository = HealthRepository()) {
    fun read(tag: Tag): ReadResult {
        val started = SystemClock.elapsedRealtime()
        var debug = NfcDebug(tagDetected = true)
        var isoDep: IsoDep? = null
        var id: String? = null
        var failure: String? = null
        try {
            isoDep = IsoDep.get(tag) ?: throw ReadFailure("Detected tag does not support ISO-DEP.")
            isoDep.timeout = 3000
            isoDep.connect()
            debug = debug.copy(isoDepConnected = true, selectResult = "Failure")
            NfcProtocol.parseSelectResponse(isoDep.transceive(NfcProtocol.selectAid()))
            debug = debug.copy(selectResult = "Success")
            id = NfcProtocol.parsePersonIdResponse(isoDep.transceive(NfcProtocol.getPersonId()))
            debug = debug.copy(personId = id)
        } catch (e: ReadFailure) {
            failure = e.message
        } catch (_: IllegalArgumentException) {
            failure = "Emitter returned an invalid NFC response."
        } catch (_: IOException) {
            failure = "NFC connection lost or timed out. Hold the phones together and try again."
        } catch (_: SecurityException) {
            failure = "NFC access is unavailable. Check device NFC settings."
        } finally {
            try { isoDep?.close() } catch (_: IOException) {
                if (failure == null) failure = "Could not close the NFC connection. Try again."
            }
        }
        // Resolve health information only after closing the RF connection.
        val state = if (failure != null) ReaderState.Error(failure)
        else if (id != null) repository.find(id)?.let(ReaderState::ProfileFound) ?: ReaderState.UnknownPerson(id)
        else ReaderState.Error("No person ID received.")
        return ReadResult(state, debug.copy(elapsedMs = SystemClock.elapsedRealtime() - started))
    }

    private class ReadFailure(message: String) : Exception(message)
}
