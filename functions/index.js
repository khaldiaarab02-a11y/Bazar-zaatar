const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { initializeApp } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

initializeApp();
const db = getFirestore();
const REGION = 'us-central1';

function cleanText(value, field, min, max, required = true) {
  const text = String(value ?? '').trim();
  if ((!text && required) || text.length < min || text.length > max) {
    throw new HttpsError('invalid-argument', `Invalid ${field}.`);
  }
  return text;
}

function validProductId(value) {
  return typeof value === 'string' && /^[A-Za-z0-9_-]{1,128}$/.test(value);
}

exports.createOrder = onCall({ region: REGION, maxInstances: 10 }, async (request) => {
  if (!request.auth) {
    throw new HttpsError('unauthenticated', 'Sign in before placing an order.');
  }

  const uid = request.auth.uid;
  const customerName = cleanText(request.data?.customerName, 'customer name', 2, 100);
  const phoneNumber = cleanText(request.data?.phoneNumber, 'phone number', 6, 40);
  const deliveryAddress = cleanText(request.data?.deliveryAddress, 'delivery address', 4, 300);
  const notes = cleanText(request.data?.notes, 'notes', 0, 500, false);
  const inputItems = request.data?.items;

  if (!Array.isArray(inputItems) || inputItems.length < 1 || inputItems.length > 50) {
    throw new HttpsError('invalid-argument', 'The cart must contain between 1 and 50 items.');
  }

  const normalizedItems = inputItems.map((item) => {
    if (!item || !validProductId(item.productId)) {
      throw new HttpsError('invalid-argument', 'A cart item has an invalid product ID.');
    }
    const quantity = Number(item.quantity);
    if (!Number.isInteger(quantity) || quantity < 1 || quantity > 99) {
      throw new HttpsError('invalid-argument', 'Item quantity must be an integer between 1 and 99.');
    }
    return {
      productId: item.productId,
      quantity,
      size: cleanText(item.selectedSize, 'size', 0, 40, false),
      color: cleanText(item.selectedColor, 'color', 0, 40, false),
    };
  });

  // Combine duplicate product IDs for stock validation and atomic decrement.
  const quantityByProduct = new Map();
  for (const item of normalizedItems) {
    quantityByProduct.set(item.productId, (quantityByProduct.get(item.productId) || 0) + item.quantity);
  }

  const orderRef = db.collection('orders').doc();
  const productRefs = [...quantityByProduct.keys()].map((id) => db.collection('products').doc(id));

  const result = await db.runTransaction(async (transaction) => {
    // Firestore transactions require all reads before any writes.
    const productSnapshots = await Promise.all(productRefs.map((ref) => transaction.get(ref)));
    const products = new Map();

    productSnapshots.forEach((snapshot) => {
      if (!snapshot.exists) {
        throw new HttpsError('failed-precondition', 'A product in your cart is no longer available.');
      }
      const product = snapshot.data();
      const quantity = quantityByProduct.get(snapshot.id);
      const stock = Number(product.stockQuantity);
      if (product.active !== true || product.inStock !== true || !Number.isInteger(stock) || stock < quantity) {
        throw new HttpsError('failed-precondition', `Insufficient stock for product ${snapshot.id}.`);
      }
      const basePrice = Number(product.priceDzd);
      const discountPrice = Number(product.discountPriceDzd);
      const unitPrice = Number.isFinite(discountPrice) && discountPrice > 0 && discountPrice < basePrice
        ? discountPrice
        : basePrice;
      if (!Number.isFinite(unitPrice) || unitPrice <= 0 || unitPrice > 10000000) {
        throw new HttpsError('failed-precondition', `Invalid configured price for product ${snapshot.id}.`);
      }
      products.set(snapshot.id, { ...product, stock, unitPrice });
    });

    const orderItems = normalizedItems.map((item) => {
      const product = products.get(item.productId);
      const sizes = Array.isArray(product.sizes) ? product.sizes : [];
      const colors = Array.isArray(product.colors) ? product.colors : [];
      if (item.size && sizes.length && !sizes.includes(item.size)) {
        throw new HttpsError('failed-precondition', `Selected size is unavailable for product ${item.productId}.`);
      }
      if (item.color && colors.length && !colors.includes(item.color)) {
        throw new HttpsError('failed-precondition', `Selected color is unavailable for product ${item.productId}.`);
      }
      return {
        productId: item.productId,
        nameAr: String(product.nameAr || product.nameFr || '').slice(0, 120),
        nameFr: String(product.nameFr || product.nameAr || '').slice(0, 120),
        size: item.size,
        color: item.color,
        quantity: item.quantity,
        unitPriceDzd: product.unitPrice,
      };
    });

    const subtotalDzd = orderItems.reduce((sum, item) => sum + item.quantity * item.unitPriceDzd, 0);
    if (!Number.isFinite(subtotalDzd) || subtotalDzd <= 0 || subtotalDzd > 100000000) {
      throw new HttpsError('failed-precondition', 'The order total is invalid.');
    }

    for (const [productId, quantity] of quantityByProduct.entries()) {
      const ref = db.collection('products').doc(productId);
      const product = products.get(productId);
      const remaining = product.stock - quantity;
      transaction.update(ref, {
        stockQuantity: remaining,
        inStock: remaining > 0,
        updatedAt: FieldValue.serverTimestamp(),
      });
    }

    transaction.create(orderRef, {
      userId: uid,
      customerName,
      phoneNumber,
      deliveryAddress,
      items: orderItems,
      subtotalDzd,
      totalDzd: subtotalDzd,
      status: 'pending',
      ...(notes ? { notes } : {}),
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });

    return { orderId: orderRef.id, totalDzd: subtotalDzd, itemCount: orderItems.length };
  });

  return result;
});
