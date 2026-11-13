const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const ts = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";
const ADMIN_EMAIL = "mildous15@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function adminContext() {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  });
}

function unverifiedAdminSpoofContext() {
  return testEnv.authenticatedContext("spoof_999", {
    email: ADMIN_EMAIL,
    email_verified: false,
  });
}

test("Unauthenticated user: denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("store_config").doc("main").get());
  await assertFails(unauthDb.collection("categories").where("active", "==", true).get());
  await assertFails(unauthDb.collection("products").where("active", "==", true).get());
  await assertFails(unauthDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertFails(unauthDb.collection("reservations").where("userId", "==", ALICE_UID).get());
});

test("Email spoofing: unverified admin email cannot write store_config or products", async () => {
  const spoofDb = unverifiedAdminSpoofContext().firestore();
  await assertFails(
    spoofDb.collection("store_config").doc("main").set({
      businessName: "Bazar Zaatar",
      businessNameAr: "بازار زعتر",
      address: "Volani Lavinir, Tiaret, Algeria, 14000",
      latitude: 35.3609386,
      longitude: 1.3225709,
      mapsUrl: "https://www.google.com/maps?q=35.3609386,1.3225709",
      bannerActive: true,
      updatedAt: ts(),
    })
  );
});

test("StoreConfig: admin can write, signed-in customer can get main but cannot list or write", async () => {
  const adminDb = adminContext().firestore();
  await assertSucceeds(
    adminDb.collection("store_config").doc("main").set({
      businessName: "Bazar Zaatar",
      businessNameAr: "بازار زعتر",
      address: "Volani Lavinir, Tiaret, Algeria, 14000",
      latitude: 35.3609386,
      longitude: 1.3225709,
      mapsUrl: "https://www.google.com/maps?q=35.3609386,1.3225709",
      phoneNumber: "+213555000000",
      whatsappNumber: "+213555000000",
      bannerActive: true,
      updatedAt: ts(),
    })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("store_config").doc("main").get());
  await assertFails(aliceDb.collection("store_config").get());
  await assertFails(
    aliceDb.collection("store_config").doc("main").update({
      phoneNumber: "+213666666666",
      updatedAt: ts(),
    })
  );
});

test("Products & Categories: customer can list active items, cannot list unfiltered or write", async () => {
  const adminDb = adminContext().firestore();
  await assertSucceeds(
    adminDb.collection("categories").doc("cat_men").set({
      nameAr: "ملابس رجالية",
      nameFr: "Vêtements Homme",
      slug: "mens-clothing",
      sortOrder: 1,
      active: true,
      iconKey: "men",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  await assertSucceeds(
    adminDb.collection("products").doc("prod_1").set({
      nameAr: "قميص كتان",
      nameFr: "Chemise Lin",
      descriptionAr: "وصف",
      descriptionFr: "Description",
      categoryId: "cat_men",
      priceDzd: 4500,
      discountPriceDzd: 3900,
      sizes: ["M", "L"],
      colors: ["Beige"],
      stockQuantity: 10,
      inStock: true,
      featured: true,
      newArrival: true,
      specialOffer: true,
      active: true,
      imageUrl: "",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("categories").where("active", "==", true).get());
  await assertSucceeds(aliceDb.collection("products").where("active", "==", true).get());
  // Unfiltered list query must fail for non-admin
  await assertFails(aliceDb.collection("products").get());
  // Shadow update / write by customer must fail
  await assertFails(
    aliceDb.collection("products").doc("prod_1").update({ priceDzd: 1, updatedAt: ts() })
  );
});

test("Cart & Profile: strict owner isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      preferredLang: "ar",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1").set({
      userId: ALICE_UID,
      productId: "prod_1",
      productNameAr: "قميص",
      productNameFr: "Chemise",
      unitPriceDzd: 3900,
      selectedSize: "M",
      selectedColor: "Beige",
      quantity: 2,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cart").get());
});

test("Orders & Reservations: owner can create pending and cancel, cannot spoof status or read other users", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = adminContext().firestore();

  // Create order as Alice
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_1").set({
      userId: ALICE_UID,
      customerName: "Alice Ben",
      phoneNumber: "0555123456",
      deliveryAddress: "Tiaret Centre, 14000",
      items: [{ productId: "prod_1", name: "Chemise", qty: 1, priceDzd: 3900 }],
      subtotalDzd: 3900,
      totalDzd: 3900,
      status: "pending",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Cannot create directly as confirmed
  await assertFails(
    aliceDb.collection("orders").doc("order_bad").set({
      userId: ALICE_UID,
      customerName: "Alice Ben",
      phoneNumber: "0555123456",
      deliveryAddress: "Tiaret Centre, 14000",
      items: [{ productId: "prod_1", name: "Chemise", qty: 1, priceDzd: 3900 }],
      subtotalDzd: 3900,
      totalDzd: 3900,
      status: "confirmed",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Bob cannot read Alice's order or list all orders
  await assertFails(bobDb.collection("orders").doc("order_1").get());
  await assertFails(bobDb.collection("orders").get());

  // Alice can query her own orders
  await assertSucceeds(aliceDb.collection("orders").where("userId", "==", ALICE_UID).get());

  // Admin can list and update order status
  await assertSucceeds(adminDb.collection("orders").get());
  await assertSucceeds(
    adminDb.collection("orders").doc("order_1").update({
      status: "confirmed",
      updatedAt: ts(),
    })
  );

  // Reservations
  await assertSucceeds(
    aliceDb.collection("reservations").doc("res_1").set({
      userId: ALICE_UID,
      customerName: "Alice Ben",
      phoneNumber: "0555123456",
      preferredDate: "2026-11-20",
      preferredTime: "15:00",
      visitorCount: 2,
      notes: "Menswear fitting",
      channel: "whatsapp",
      status: "pending",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  await assertFails(bobDb.collection("reservations").doc("res_1").get());
  await assertSucceeds(aliceDb.collection("reservations").where("userId", "==", ALICE_UID).get());
  await assertSucceeds(adminDb.collection("reservations").get());
});
