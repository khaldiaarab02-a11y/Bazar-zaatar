# Bazar Zaatar — Firebase and deployment audit (V7 working review)

This document records source/configuration checks only. No live Firebase data access, Firestore emulator execution, Android APK build, Google Sign-In device test, or Vercel deployment has been performed in this environment.

## Verified from project files

- Firebase project ID: `bazar-zaatar`; project number: `369307805376`.
- Android application ID: `com.aistudio.bazarzaatar.xkqmtz`.
- Android Firestore database ID resource and Web database ID: `(default)`.
- Android applies the Google Services Gradle plugin and uses Firebase Auth/Firestore dependencies.
- Web app initializes Firebase JS SDK, signs in anonymously, reads active documents from `products`, and writes customer orders to `orders`.
- The user confirmed Anonymous Authentication and Google Authentication are enabled in Firebase Console.
- The refreshed `app/google-services.json` contains two Android OAuth clients and one Web OAuth client. The currently bundled debug keystore SHA-1 is `00:ce:10:65:f1:5b:e0:c7:b2:05:39:79:f6:0e:aa:0f:76:6c:06:27`; it matches one Android OAuth client in the refreshed file.
- The previous Android OAuth client with SHA-1 `fc:c2:91:b4:69:d5:a3:aa:e4:d3:17:82:fe:0e:b1:f0:fd:6c:4b:82` is retained in the file; it may correspond to an earlier signing key and was not removed.
- Web `app.js` passes `node --check` after the V7.1 correction.
- Vercel static site files are under `web/`. Intended Vercel settings: Root Directory `web`, Framework Preset `Other`, Build Command empty, Output Directory `.`.

## Changes in this working review

- Replaced the old Android `google-services.json` with the user-supplied refreshed file. The previous version is retained as `app/google-services.json.before-v7`.
- Corrected Web order item keys to match the Android admin screen and Android `Order` model: `nameAr`, `nameFr`, `size`, `color`, `quantity`, `unitPriceDzd`, and `productId`.
- Prevented the Web app from silently saving a local demo order when Firebase is configured but anonymous authentication/session setup failed. Configured Firebase orders now fail visibly instead of pretending to be accepted.
- Prevented stale cart entries from being submitted as demo products when the live Firebase catalogue is in use.

## Owner identity — deliberately unchanged

The owner email `mildous15@gmail.com` is retained exactly as requested. It remains in the existing Firestore rules, Android owner bootstrap check, and test fixture. Do not replace or remove it. The owner must sign in with that Google account and have a verified email for the bootstrap-owner rule to match.

## Firestore security review — not yet deployable

- Rules are default-deny and restrict product/category writes, profile/cart access, customer order ownership, and admin order/status management.
- Product and price writes are admin-only under the current rules.
- Orders are owner-isolated for reads; customers can create only their own pending orders and can only cancel their own pending orders. Admins can manage order statuses.
- Admin bootstrap is based on the verified owner email or an existing `/admins/{uid}` document. The client cannot self-create its first admin role without already being an admin.
- V7.4 Spark rules validate every submitted order line against the current product document price/discount, active/in-stock state, variant options and aggregate requested quantity; they recompute the order total. Spark rules cannot atomically decrement or reserve inventory, so pending orders require manual stock confirmation and adjustment by the administrator. See `SPARK-PLAN.md`.
- Existing rules tests are present, but they have not run because dependency installation did not complete in this environment. Do not deploy rules until `npm install` and `npm run test:rules` pass.

## Remaining blockers

1. **Rules tests:** `npm install --no-audit --no-fund` timed out in this environment; emulator tests remain unexecuted.
2. **Android build:** the archive has `gradle-wrapper.properties` but no `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`. No system Gradle or Android SDK is installed here. APK compilation is unverified.
3. **Google Sign-In:** OAuth configuration is present and the current debug SHA-1 matches, but actual sign-in on a device remains untested. The release signing key does not exist yet; release OAuth must be revisited after a release keystore is created.
4. **Live Firestore:** no live products/categories/orders were inspected and no live read/write test was performed.
5. **Web admin:** the Web/PWA is a storefront and order-submission flow, not a full Web admin dashboard. Android admin functionality remains in place.
6. **Vercel:** no deployment was performed. Configure Root Directory as `web` only when ready to deploy.

## Local Firestore rules test

From the repository root:

```bash
npm install
npm run test:rules
```

The test script uses Firebase CLI to start the Firestore emulator on port 8085 and runs `firestore.test.js`. Java is available in the current environment, but dependency installation did not complete.


## V7.2 follow-up

- Google Services plugin now uses `MissingGoogleServicesStrategy.ERROR` rather than warning if Android Firebase configuration is absent.
- Order rules require `totalDzd == subtotalDzd` for the current checkout flow, and emulator tests now include inconsistent-total and customer-total-tampering cases. These tests have been authored but not executed in this environment.
- Added `.github/workflows/android-and-rules.yml` to run the emulator tests and assemble a debug APK on GitHub Actions after the project is uploaded and the workflow is run. No GitHub upload or workflow run has occurred.
- The root debug keystore is no longer ignored by `.gitignore`, because the cloud build needs the exact key whose SHA-1 is registered in the Android OAuth client. It is a development key only, not a release key.
- Local checks passed: JavaScript syntax, JSON parsing, Android manifest XML parsing, and keytool fingerprint inspection. `npm install` timed out, so emulator tests remain unexecuted.


## V7.3 order-integrity changes

- Added `functions/createOrder` callable backend. It requires Firebase Authentication, reloads authoritative product data, validates active state, configured prices, selected variants and available stock, then creates the order and decrements inventory in one Firestore transaction.
- Web and Android order submission now call the same `createOrder` function in `us-central1`; client-submitted prices are no longer used as the authoritative order price.
- Firestore client rules now deny direct order creation. Existing owner-isolated reads, customer cancellation of pending orders, and admin status management remain.
- The Web cart checkout is disabled until Firebase configuration and an authenticated Firebase session are ready; it no longer stores a local demo order as if it were a real order.
- Deployment is still pending. Cloud Functions deployment requires Blaze billing; emulator tests and physical-device/live-Firebase tests have not run in this environment.


## V7.4 Spark-compatible changes

- Web and Android no longer call Cloud Functions. Both create pending order documents directly in the shared Firestore `orders` collection using the same schema.
- Order-create rules validate the signed-in owner, pending status, allowed fields, up to 10 line items, authoritative product price/discount, variant selection, stock snapshot and recomputed subtotal/total.
- Customer order updates are limited to cancellation of their own pending order; admins may update only status and timestamp.
- The Firebase CLI config no longer declares a Functions deployment target. The previous `functions/` source remains optional for a future Blaze migration only.
- Inventory is not atomically reserved or decremented on Spark. The administrator must verify physical stock and update inventory when confirming each order.
- Emulator rules tests, live Firebase checks and physical-device checks remain pending; this source audit does not claim those tests passed.
