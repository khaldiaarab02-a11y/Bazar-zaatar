# Bazar Zaatar — Web + PWA deployment

## Deploy on Vercel

1. Import the repository into Vercel after the owner approves the GitHub update.
2. Set **Root Directory** to `web`.
3. Set **Framework Preset** to **Other**.
4. Leave **Build Command** empty (the current storefront is static).
5. Set **Output Directory** to `.`.
6. Deploy and verify the generated URL on a phone and desktop browser.

## Firebase configuration

- Web: `web/firebase-config.js` points to project `bazar-zaatar` and Firestore `(default)`.
- Android: `app/google-services.json` points to project `bazar-zaatar` and package `com.aistudio.bazarzaatar.xkqmtz`.
- Web sign-in uses Anonymous Authentication. Keep the Anonymous provider enabled.
- Android uses Google Sign-In. Keep Google provider enabled and retain the registered SHA fingerprints. Download a fresh `google-services.json` from Firebase if OAuth settings change.

## Spark order flow

The Web storefront reads active products and writes pending orders directly to `orders`. Firestore Rules validate each line against the product's current price/discount, active/in-stock state, variant options and stock snapshot, and recompute the order subtotal. The Android app uses the same order schema and collection.

Spark does not provide an atomic server-side stock decrement in this design. A new order is a request pending administrator confirmation. The administrator must verify physical stock and update inventory manually when confirming. Orders are limited to 10 cart lines to remain within Firestore Rules document-access limits. See `SPARK-PLAN.md`.

## Owner and administration

The owner email `mildous15@gmail.com` is intentionally retained. The owner's Google account must sign in with a verified email for the owner bootstrap rule to match. Do not change this email or make product/order collections publicly writable.

## Current Web scope

The Web/PWA provides the storefront, cart and order submission. The existing Android app contains the administration dashboard for managing products, categories, orders and store settings. This release does not claim to add a complete Web admin dashboard.
