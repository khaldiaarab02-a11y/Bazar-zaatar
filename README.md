# Bazar Zaatar — Spark-compatible development release

This archive continues the existing Android application and Arabic/French Web/PWA storefront. It keeps the shared Firebase project and the current product, cart, order, reservation and administration features.

## Current configuration

- Firebase project: `bazar-zaatar` (default Firestore database).
- Android package: `com.aistudio.bazarzaatar.xkqmtz`.
- Web client: `web/firebase-config.js`; Web sign-in uses Firebase Anonymous Authentication.
- Android client: `app/google-services.json`; Android authentication uses Google Sign-In.
- Owner bootstrap email `mildous15@gmail.com` is intentionally preserved in Firestore rules and related source. Do not replace or remove it.

## Firebase Spark compatibility

Web and Android create orders directly in Firestore. Firestore Rules validate the signed-in owner, allowed order fields, current product price/discount, product availability, variants, requested stock snapshot, order total and pending status. Customer writes to product data and prices remain denied.

**Inventory limitation:** Spark rules cannot atomically decrement product stock or reserve it across concurrent orders. The order is therefore pending until the administrator verifies physical stock and confirms it. The admin must update inventory manually when processing an order. See `SPARK-PLAN.md`.

The `functions/` directory is retained only as optional future Blaze source. Neither client calls it, and it is not configured for deployment in `firebase.json`.

## Deployment and checks

- Vercel Web/PWA: `README-WEB-DEPLOY.md` (Root Directory must be `web`).
- Firestore audit: `FIREBASE-AUDIT.md`.
- Phone-only Android cloud build: `CLOUD-BUILD-ANDROID.md`.
- Spark order and inventory limitations: `SPARK-PLAN.md`.
- Work status and verification: `WORK-STATUS-V7.4-SPARK.md`.

Do not treat the project as production-verified until Firestore emulator tests pass, the Android APK is built and installed, Google Sign-In is tested on a device, and live Web/Android order synchronization is smoke-tested.
