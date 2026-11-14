# Optional Cloud Functions source (not used on Firebase Spark)

The `functions/` directory contains the previous trusted `createOrder` backend implementation. It is retained for a future migration only.

The Spark-compatible application in this release does **not** call Cloud Functions. `firebase.json` does not configure the functions source, and the Web and Android clients create pending orders directly under the Firestore Security Rules documented in `SPARK-PLAN.md`.

Do not run a Functions deployment while keeping the Firebase project on Spark. Deploying Cloud Functions requires enabling Blaze billing and reviewing expected usage/costs first. If the owner later approves a Blaze migration, the clients, rules and backend must be migrated together; do not deploy the old function alone because its schema and inventory behavior must be revalidated against the live database.
