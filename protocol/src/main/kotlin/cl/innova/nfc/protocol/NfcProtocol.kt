package cl.innova.nfc.protocol

object NfcProtocol {
    const val AID = "F0494E4E4F564101"
    const val DEMO_PERSON_ID = "8F31A20C779145D4B522F10492AA610E"
    const val PERSON_ID_LENGTH = 16
    const val SUCCESS = 0x9000
    const val NOT_FOUND = 0x6A82
    const val INS_NOT_SUPPORTED = 0x6D00
    const val WRONG_LENGTH = 0x6700

    fun selectAid(): ByteArray = fromHex("00A4040008${AID}00")
    fun getPersonId(): ByteArray = fromHex("80CA000000")

    sealed interface Command {
        data object Select : Command
        data object GetPersonId : Command
        data class Rejected(val status: Int) : Command
    }

    fun parseCommand(apdu: ByteArray): Command {
        if (apdu.size < 4) return Command.Rejected(WRONG_LENGTH)
        val cla = apdu[0].toInt() and 0xFF
        val ins = apdu[1].toInt() and 0xFF
        if (cla == 0 && ins == 0xA4) {
            if (apdu.size < 5) return Command.Rejected(WRONG_LENGTH)
            val length = apdu[4].toInt() and 0xFF
            if (apdu.size != 5 + length && apdu.size != 6 + length) {
                return Command.Rejected(WRONG_LENGTH)
            }
            if (apdu[2] != 0x04.toByte() || apdu[3] != 0.toByte() ||
                !apdu.copyOfRange(5, 5 + length).contentEquals(fromHex(AID))) {
                return Command.Rejected(NOT_FOUND)
            }
            return Command.Select
        }
        if (cla == 0x80 && ins == 0xCA) {
            if (apdu.size != 5) return Command.Rejected(WRONG_LENGTH)
            return if (apdu.contentEquals(getPersonId())) Command.GetPersonId
            else Command.Rejected(INS_NOT_SUPPORTED)
        }
        return Command.Rejected(INS_NOT_SUPPORTED)
    }

    fun response(status: Int, data: ByteArray = byteArrayOf()): ByteArray {
        require(status in 0..0xFFFF)
        return data + byteArrayOf((status ushr 8).toByte(), status.toByte())
    }

    data class Response(val data: ByteArray, val status: Int)

    fun parseResponse(bytes: ByteArray): Response {
        require(bytes.size >= 2) { "Missing APDU status word" }
        val status = ((bytes[bytes.size - 2].toInt() and 0xFF) shl 8) or
            (bytes.last().toInt() and 0xFF)
        return Response(bytes.copyOfRange(0, bytes.size - 2), status)
    }

    fun parseSelectResponse(bytes: ByteArray) {
        val result = parseResponse(bytes)
        require(result.status == SUCCESS && result.data.isEmpty()) { "SELECT rejected or malformed" }
    }

    fun parsePersonIdResponse(bytes: ByteArray): String {
        val result = parseResponse(bytes)
        require(result.status == SUCCESS) { "GET_PERSON_ID rejected" }
        require(result.data.size == PERSON_ID_LENGTH) { "Person ID must be 16 bytes" }
        return toHex(result.data)
    }

    fun fromHex(hex: String): ByteArray {
        require(hex.length % 2 == 0 && hex.all { it in "0123456789abcdefABCDEF" })
        return ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
}

/** One selection state per RF session; reset when the link is deactivated. */
class CardSession {
    private var selected = false

    fun reset() { selected = false }

    fun process(apdu: ByteArray): ByteArray = when (val command = NfcProtocol.parseCommand(apdu)) {
        NfcProtocol.Command.Select -> {
            selected = true
            NfcProtocol.response(NfcProtocol.SUCCESS)
        }
        NfcProtocol.Command.GetPersonId -> if (selected) {
            NfcProtocol.response(NfcProtocol.SUCCESS, NfcProtocol.fromHex(NfcProtocol.DEMO_PERSON_ID))
        } else NfcProtocol.response(NfcProtocol.NOT_FOUND)
        is NfcProtocol.Command.Rejected -> {
            // A rejected SELECT must not preserve an earlier application selection.
            if (apdu.size >= 2 && apdu[1] == 0xA4.toByte()) selected = false
            NfcProtocol.response(command.status)
        }
    }
}
