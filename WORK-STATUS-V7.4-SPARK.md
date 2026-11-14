# Bazar Zaatar — V7.4 Spark-compatible work status

## Changes made in this pass

- Removed the Cloud Functions runtime declaration from `firebase.json`; the project remains deployable on Firebase Spark without enabling billing.
- Updated Web order submission to create a pending Firestore order directly, using the authenticated anonymous UID, current product price snapshot and server timestamps.
- Updated Android `placeOrder` to read current product documents in a Firestore transaction, validate stock and variants client-side, then create the same pending order schema directly in Firestore. Cart cleanup occurs only after the order write succeeds.
- Removed the Firebase Functions SDK dependency and `FirebaseFunctions` usage from Android.
- Replaced Firestore order-create denial with strict Spark-compatible validation: authenticated owner, pending status, exact allowed fields, current product price/discount, active/in-stock status, aggregate requested quantity per product, valid variants, maximum 10 lines, and recomputed subtotal/total.
- Restricted order updates to status/timestamp changes for admins; customers may only cancel their own pending order.
- Added `SPARK-PLAN.md` to document the key limitation: Firestore Rules cannot atomically decrement stock or reserve stock across concurrent orders. New orders remain pending until an administrator verifies stock and updates inventory manually.
- Kept `functions/` only as optional future Blaze source. Neither client calls it; it is not configured in `firebase.json`.
- Preserved `mildous15@gmail.com`, Firebase project ID, Android package name, existing UI and app features.

## Checks run in this workspace

- `node --check web/app.js`: passed.
- `node --check firestore.test.js`: passed.
- `node --check functions/index.js` (retained optional source only): passed.
- JSON parsing for Firebase client config, Firebase CLI config, root/functions package metadata: passed.
- `npm install --no-audit --no-fund`: timed out in this environment; Firebase Emulator rules compilation/tests were not run.
- Android Gradle/APK build: not run in this workspace; Android SDK/Gradle build environment is not available here.
- Google Sign-In on a physical device: not tested.
- Live Firestore/Web/Android synchronization: not tested.
- Vercel deployment: not performed.

## Operational caveat

This Spark design checks the product stock value at order submission, but it does not reserve or decrement stock atomically. The admin must verify stock and update inventory while processing each pending order. Do not describe stock as automatically reserved.

## Next release gates

1. Run Firestore Emulator tests against `firestore.rules`.
2. Build the Android debug APK through the included GitHub Actions workflow after the owner chooses to upload the finished source.
3. Install the APK on the phone and test Google Sign-In.
4. Deploy Firestore rules and test an order on the live project using non-real test data.
5. Deploy the Web/PWA to Vercel and verify that the same order is visible in Android admin.
