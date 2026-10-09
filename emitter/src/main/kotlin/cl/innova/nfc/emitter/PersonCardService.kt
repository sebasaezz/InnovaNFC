package cl.innova.nfc.emitter

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import cl.innova.nfc.protocol.CardSession
import cl.innova.nfc.protocol.NfcProtocol

class PersonCardService : HostApduService() {
    private val session = CardSession()

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray =
        commandApdu?.let(session::process) ?: NfcProtocol.response(NfcProtocol.WRONG_LENGTH)

    override fun onDeactivated(reason: Int) { session.reset() }
}
