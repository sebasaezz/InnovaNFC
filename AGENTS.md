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

## Visual identity and UI design rules

- Both apps share a dark-first, premium but youthful nightlife/event-safety identity: technological, calm, trustworthy, restrained, and legible at arm's length in dark venues. Avoid hospital, banking, chatbot, crypto, Material-sample, and generic rounded-card dashboard aesthetics.
- Palette: background `#12091F`; elevated surface `#1D1230`; primary purple `#6C3BFF`; secondary violet `#8B5CF6`; soft lavender `#C4A7FF`; primary text `#FFFFFF`; secondary text `#C9C3D5`; borders/dividers white at 10% opacity; emergency `#FF4D6D`; success `#5DE2B5`. No unrelated colors. Purple brands the product; pink/red marks medical alerts or actual critical states; green marks confirmed/healthy system states.
- Prioritize composition, negative space, strong typography, compact labels, hierarchy, and contrast. Uppercase only small operational/emergency labels. Use intentional 14–20 dp major surface corners, subtle elevated depth, sparse borders, and restrained purple accents. Avoid excessive cards, capsules, outlines, shadows, glow, gradients, explanatory copy, nested containers, glassmorphism, random blobs, floating spheres, and rainbow/purple-blue neon. Never put every field in an identical card.
- Recognizable signature: sparse concentric NFC waves, arcs, interrupted circles, and proximity/signal geometry drawn with native Compose. No emoji icons, remote image assets, external UI frameworks, or animation libraries. Native subtle breathing motion is acceptable. Use icons sparingly.
- Emitter is a passive attendee credential: small brand, “Identidad de emergencia activa”, central NFC identity object, short state/instruction, discreet person ID, collapsed secondary diagnostics. Healthy NFC/HCE gets a restrained success indicator; unavailable/disabled states remain clear. No giant enclosing card or diagnostic/settings-screen focus.
- Reader is operational and denser. Ready says “Acerca el teléfono del asistente” beside a distinctive scan target; Reading transitions that target instead of showing a generic full-screen spinner. Profile hierarchy: name/age/blood type, relevant pink critical allergy/warning band, conditions and medications, quickly identifiable emergency contact. Serious conditions must stand out. Unknown is a neutral operational state; Error uses concise human-readable Spanish copy and an easy retry/reset action.
- All UI copy is concise, natural Spanish without marketing language. Preserve functionality and accessible but secondary technical-only diagnostics. Keep protocol, HCE, NFC reader logic, repository behavior, and tests unchanged except a necessary UI compile fix. Reuse genuinely useful local theme/primitives consistently; do not add architectural modules/dependencies merely to share UI with the JVM protocol.
- Use Compose, support Galaxy S22/S25 screen sizes with scrolling/insets, and integrate status/navigation bars into the dark theme. Verify compilation/previews and run the existing protocol tests and both debug APK builds after UI changes.
