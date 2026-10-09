# InnovaNFC project contract

Work directly in this repository. Build an offline Android MVP with modules `:protocol`, `:emitter`, and `:reader`, using Kotlin, Compose, Material 3, and Gradle Kotlin DSL. Prefer compile/target SDK 36 and minimum SDK 26. Use stable compatible tool versions and minimal dependencies.

Packages: `cl.innova.nfc.protocol`, `cl.innova.nfc.emitter`, `cl.innova.nfc.reader`.

Reader device: Galaxy S25 / Android 16. Emitter: Galaxy S22 / Android 14. Emitter uses HostApduService, category other, AID `F0494E4E4F564101`, requireDeviceUnlock=false, requireDeviceScreenOn=false. HCE must work independently of the activity. Only transmit opaque person ID, never health data.

Protocol: SELECT AID returns 9000; GET_PERSON_ID `80 CA 00 00 00` returns 16 bytes plus 9000. Demo ID `8F31A20C779145D4B522F10492AA610E`. Status words: SUCCESS 9000, NOT_FOUND 6A82, INS_NOT_SUPPORTED 6D00, WRONG_LENGTH 6700. Implement creation/parsing and focused JVM tests in protocol.

Reader uses enableReaderMode with NFC_A and SKIP_NDEF_CHECK, IsoDep, and background I/O. Connect, SELECT, validate success, GET ID, validate exact length/status, close, then resolve locally. States: Ready, Reading, Profile found, Unknown person, Error.

Keep exactly three fictional local profiles with name, age, bloodType, allergies, conditions, medications, emergencyContactName, emergencyContactPhone. Demo ID resolves to one. Display emergency fields prominently. Expandable diagnostics contain only tag detection, IsoDep connection, SELECT result, ID received, elapsed time. Never log health information.

Emitter diagnostics show NFC availability/enabled, HCE support, Secure NFC support/enabled where supported, and demo ID.

No networking, Firebase, Room, Retrofit, login/accounts, DI frameworks, Bluetooth, Android Beam, phone-to-phone NDEF, or third-party NFC libraries.

Implement in order: skeleton; protocol/tests; emitter HCE; reader NFC; local repository/UI; build verification. Fix implementation issues as found. Run `./gradlew :protocol:test :emitter:assembleDebug :reader:assembleDebug` when SDK available. Do not provision a full Android environment if absent; report local verification requirement. README must document phone/APK mapping, build/install, physical test sequence. Do not ask for confirmation absent a real blocker.
