# Bazar Zaatar — Firebase Spark plan

This release is designed to run without upgrading the Firebase project from Spark.
It does not deploy or call Cloud Functions.

## Order flow on Spark

- Web and Android authenticate with Firebase Authentication as they already do.
- A customer submits an order document directly to `orders`.
- Firestore Rules verify that the signed-in user is the owner of the new order, the order is `pending`, the document shape is allowed, the line-item prices match the current product document (including a valid discount), selected variants exist, and the requested aggregate quantity for each product does not exceed the currently recorded stock.
- Rules recompute the line-item subtotal and require `totalDzd == subtotalDzd`.
- Customers can read only their own orders and can only cancel their own pending orders. Admins can list orders and change only status/timestamp.

## Important inventory limitation on Spark

Firestore Security Rules can validate the stock value observed during an order write, but they cannot safely decrement inventory or serialize competing customer orders. Therefore this Spark implementation deliberately does **not** decrement `products.stockQuantity` automatically. Two customers can submit orders against the same remaining stock before an administrator updates it.

Operational procedure:

1. Treat each new order as `pending`, not as guaranteed stock allocation.
2. The administrator must verify physical stock before confirming the order.
3. When confirming, update the product stock manually in the Android admin dashboard.
4. If stock is unavailable, cancel the order and contact the customer.

Do not advertise real-time reserved stock while using this Spark-only flow. For automatic atomic reservation/decrement, move order processing to a trusted backend (Cloud Functions on Blaze or another secured server) before enabling that behavior.

## Order limits

- Maximum 10 cart lines per order, due to Firestore Rules document-access limits when validating product records.
- Quantity per line: 1–99.
- A product's price and availability are checked against Firestore at submission time.

## Optional Blaze backend

The `functions/` folder is retained as optional source for a future Blaze deployment. It is not referenced in `firebase.json`, is not called by either client, and is not required for the Spark flow. Do not deploy it while remaining on Spark.
