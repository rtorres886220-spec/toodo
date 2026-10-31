# Release readiness

## Build prerequisites

This archive does not contain `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`, and the editing environment has no installed Gradle. The wrapper distribution is configured for Gradle 9.3.1. With Gradle 9.3.1 installed, restore the standard wrapper files from the project root:

```sh
gradle wrapper --gradle-version 9.3.1 --distribution-type bin
```

The current AI Studio debug signing configuration also expects a project-root `debug.keystore`, which is intentionally ignored by Git. Supply the AI Studio keystore or adapt the debug signing setup for your local environment before building.

Then verify the project with:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleRelease
```

## Firebase and signing

- Add the Firebase Android configuration for application ID `com.aistudio.anchor.kxvqpa` at `app/google-services.json`.
- Put the Firebase App Check reCAPTCHA site key in the ignored root `.env` file as `recaptcha_site_key=...`. The key is required for release App Check initialization; do not use the debug provider in a release build.
- Provide the upload keystore outside version control and set `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` for release builds. The alias configured by the Android build is `upload`.
- Keep `.env`, keystores, and signing properties out of source control.

## Device verification before publishing

- Test reminders on Android 12 and later with exact alarm access both enabled and denied. Denial uses inexact alarms, so delivery can be delayed.
- Test Android 13+ notification permission granted and denied, and verify settings can reopen the system permission screen.
- Test task completion, editing/removing due dates, snoozing across midnight, device reboot, manual time changes, and time-zone changes.
- Test both daily review notification actions, the new-task and voice shortcuts, and speech services that are unavailable or cancel recognition.
- Check Today at 320 dp width and larger font sizes, plus TalkBack navigation for tabs and capture controls.
- Confirm cloud backup and device transfer restore the Room database, Anchor preferences, and private shared-image attachments.

The build and device matrix still require a machine with the Android SDK, Gradle wrapper, Firebase configuration, and a test device or emulator.
