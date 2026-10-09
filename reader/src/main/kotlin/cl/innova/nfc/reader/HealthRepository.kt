package cl.innova.nfc.reader

import cl.innova.nfc.protocol.NfcProtocol

data class HealthProfile(
    val name: String,
    val age: Int,
    val bloodType: String,
    val allergies: List<String>,
    val conditions: List<String>,
    val medications: List<String>,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
)

/** Fictional demonstration records, held exclusively on this reader. */
class HealthRepository {
    private val profiles = mapOf(
        NfcProtocol.DEMO_PERSON_ID to HealthProfile(
            "Elena Demo", 34, "O+", listOf("Penicillin"), listOf("Asthma"),
            listOf("Salbutamol inhaler as prescribed"), "Tomás Demo", "+56 9 0000 0001",
        ),
        "00000000000000000000000000000002" to HealthProfile(
            "Martín Ejemplo", 57, "A−", listOf("Peanuts"), listOf("Type 2 diabetes"),
            listOf("Metformin as prescribed"), "Lucía Ejemplo", "+56 9 0000 0002",
        ),
        "00000000000000000000000000000003" to HealthProfile(
            "Sofía Ficticia", 26, "AB+", emptyList(), listOf("Epilepsy"),
            listOf("Levetiracetam as prescribed"), "Diego Ficticio", "+56 9 0000 0003",
        ),
    )

    fun find(personId: String): HealthProfile? = profiles[personId]
}
