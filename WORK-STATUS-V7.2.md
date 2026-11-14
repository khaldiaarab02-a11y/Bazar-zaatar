# Bazar Zaatar — V7.2 working status

## Completed in this pass
- Kept the refreshed `app/google-services.json` and verified its project ID and Android package ID.
- Recomputed the root debug keystore fingerprints using `keytool`; its SHA-1 matches the Android OAuth client hash in `google-services.json`.
- Changed the Google Services Gradle plugin missing-config behavior from `WARN` to `ERROR`, so builds fail clearly if the Firebase Android config is missing.
- Strengthened Firestore order validation so `totalDzd` must equal `subtotalDzd` for the current no-separate-delivery-fee checkout flow.
- Added rules tests for inconsistent order totals and for customers attempting to rewrite an order total after creation.
- Added a GitHub Actions workflow to run Firestore emulator tests and build a debug APK in the cloud. This is intended for use after the user approves and uploads the final source; no GitHub push was made.
- Updated `.gitignore` so the existing non-production debug keystore is not silently omitted from a cloud build checkout. The key must never be used for release signing.
- Added `CLOUD-BUILD-ANDROID.md` with phone-friendly instructions for downloading the APK artifact.

## Checks actually performed
- `node --check web/app.js`: passed.
- `node --check firestore.test.js`: passed.
- JSON parsing for `app/google-services.json`, `web/manifest.webmanifest`, and `web/vercel.json`: passed.
- Android manifest XML parsing: passed.
- `keytool` check: debug SHA-1 and SHA-256 successfully read; SHA-1 matches the Android OAuth certificate hash in the refreshed config.

## Not completed / not claimed
- Firestore Emulator tests were not run: `npm install --no-audit --no-fund` timed out in this environment.
- Android APK was not built here: no Android SDK or system Gradle is installed, and the archive does not contain Gradle wrapper scripts/JAR. The new cloud workflow has not yet run.
- Google Sign-In was not tested on a physical device.
- Live Firestore reads/writes and real cross-platform order synchronization were not tested.
- Vercel deployment was not performed.
- The SHA-256 fingerprint was computed locally, but Firebase Console registration of that SHA-256 is not independently verifiable from `google-services.json`; the user should retain it in the Android app fingerprint list.

## Important remaining security limitation
Firestore rules still cannot establish that client-submitted line-item prices match the current trusted product prices or decrement inventory atomically. Before accepting real orders as financially/inventory authoritative, move order creation and stock reservation to a trusted Cloud Function or other server-side endpoint. Do not treat client-supplied order totals as authoritative.

## Protected owner identity
The owner email `mildous15@gmail.com` remains unchanged in the rules and test fixtures.
