# Bazar Zaatar — Working status V7.1

## Completed in this pass
- Refreshed Android Firebase configuration installed and previous file backed up.
- Checked project ID, app ID/package name, OAuth client records, and bundled debug keystore SHA-1.
- Reviewed Firestore rules, Web order flow, Android order model, and Android admin order rendering.
- Fixed Web/Android order item field mismatch.
- Fixed Web behavior that could store a local demo order after a configured Firebase session failed.
- JavaScript syntax check passed (`node --check web/app.js`).

## Not completed / not claimed
- Firestore emulator tests: not run (npm dependency installation timed out).
- Live Firebase access and real order round-trip: not tested.
- Android APK: not built (no Gradle wrapper JAR/scripts, system Gradle, or Android SDK in this environment).
- Google Sign-In on physical device: not tested.
- Vercel deployment: not performed.

## Protected setting
- Keep `mildous15@gmail.com` unchanged.
