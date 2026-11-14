# Cloud build — Android APK and Firestore Rules

This project includes a GitHub Actions workflow at `.github/workflows/android-and-rules.yml`. It allows building a debug APK without Android Studio or a computer with the Android SDK.

## Before running the workflow

- Keep `app/google-services.json` in the project.
- Keep the existing root `debug.keystore` unchanged. Its SHA-1 is registered in Firebase for the current debug OAuth client.
- The included debug keystore is for development/testing only. It must never be used for a production release.
- Do not commit a production/release keystore, passwords, Firebase service-account JSON, or `.env` files.

## Run it from a phone

1. After the final project is approved and uploaded to GitHub, open the repository in GitHub.
2. Open **Actions**.
3. Select **Bazar Zaatar Android and Firestore checks**.
4. Tap **Run workflow**.
5. Wait for both jobs. The `android-debug-apk` job uploads an artifact named `bazar-zaatar-debug-apk`.
6. Open the successful workflow run, scroll to **Artifacts**, and download the APK artifact.

The workflow runs the Firestore emulator tests in one job and assembles a debug APK in another. A successful APK build does not by itself prove Google Sign-In works on a physical phone; that still requires installing the APK and completing a real sign-in test.

## Current signing limitation

No production release keystore was supplied. A separate release keystore must be created and backed up securely before distributing a production APK or publishing to Google Play. Creating a new release key may prevent updating an older app that was signed with a different private key.
