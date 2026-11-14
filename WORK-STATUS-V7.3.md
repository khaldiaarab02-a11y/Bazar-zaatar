# Bazar Zaatar — V7.3 local completion status

## Changes in this pass
- Added a trusted Firebase callable Cloud Function `createOrder` in `functions/index.js`. It validates authenticated callers, customer fields, product availability, server-side prices, variants and stock. It writes an order and decrements stock atomically in one Firestore transaction.
- Updated Web order submission to call `createOrder` in `us-central1`; client prices are no longer submitted as authoritative values.
- Updated Android `placeOrder` to call the same function and clear the cart only after a successful function response. Added the Firebase Functions Android SDK through the Firebase BOM.
- Changed Firestore rules to deny direct client order creation. Existing customer-owned order reads, pending-order cancellation and admin order management remain.
- Disabled Web checkout until Firebase configuration, sign-in and Firestore session are ready. Removed local demo-order success behavior.
- Updated deployment documentation and CI syntax checks for the Cloud Function.
- Kept `mildous15@gmail.com`, Android package ID, Firebase project ID and existing UI design unchanged.

## Checks actually run
- `node --check web/app.js`: passed.
- `node --check firestore.test.js`: passed.
- `node --check functions/index.js`: passed.
- JSON parsing for Firebase config, Vercel config, manifest, Firebase CLI config and functions package: passed.
- Android manifest XML parsing: passed.

## Not run / not verified
- `npm install` timed out in this environment; Firestore Emulator rules tests were not run.
- Android APK build was not run: Android SDK and system Gradle are not installed here. The existing GitHub Actions workflow can build it only after the source is uploaded and the workflow is run.
- Cloud Function was not deployed or executed against the real Firebase project. Deployment requires Blaze billing and authenticated Firebase CLI access.
- Google Sign-In was not tested on a physical device.
- Live cross-platform product/order synchronization was not tested.
- Vercel deployment and production URL were not tested.

## Important release gate
Deploy `createOrder` and the Firestore rules first, then run the smoke tests in `CLOUD-FUNCTIONS-DEPLOY.md`. Do not launch checkout before those checks pass. A release signing keystore is also still needed for a distributable production APK; the included debug keystore is for testing only.
