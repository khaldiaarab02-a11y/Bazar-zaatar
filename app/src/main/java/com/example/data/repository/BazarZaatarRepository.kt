package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AdminRole
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.DefaultCategories
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Reservation
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BazarZaatarRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> wrapFirestoreFlow(
        path: String,
        operationType: OperationType,
        uidSnapshot: String? = auth.currentUser?.uid,
        source: Flow<T>
    ): Flow<T> = source.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && (uidSnapshot == null || auth.currentUser?.uid == uidSnapshot)) {
            handleFirestoreError(exceptionToLog, operationType, path, auth)
        }
        throw error
    }

    // --- Admin Check ---
    suspend fun checkIsAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        if (user.isEmailVerified && user.email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)) {
            return true
        }
        val docRef = db.collection("admins").document(user.uid)
        return try {
            docRef.get().await().exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            false
        }
    }

    fun observeAdmins(): Flow<List<AdminRole>> = flow {
        val uid = requireUserId()
        val path = "admins"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val uid = requireUserId()
        val path = "users"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                    }
            )
        )
    }

    suspend fun grantAdminAccess(targetUid: String, targetEmail: String): Result<Unit> = runCatching {
        requireUserId()
        val cleanUid = targetUid.trim()
        require(cleanUid.isNotBlank()) { "User UID is required" }
        val docRef = db.collection("admins").document(cleanUid)
        val payload = buildMap<String, Any> {
            put("uid", cleanUid)
            if (targetEmail.isNotBlank()) {
                put("email", targetEmail.trim())
            }
            put("createdAt", FieldValue.serverTimestamp())
        }
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun revokeAdminAccess(targetUid: String): Result<Unit> = runCatching {
        requireUserId()
        val cleanUid = targetUid.trim()
        require(cleanUid.isNotBlank()) { "User UID is required" }
        val docRef = db.collection("admins").document(cleanUid)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- Store Config ---
    fun observeStoreConfig(): Flow<StoreConfig> = flow {
        val uid = requireUserId()
        val docRef = db.collection("store_config").document("main")
        emitAll(
            wrapFirestoreFlow(docRef.path, OperationType.GET, uid, docRef.snapshots().map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(StoreConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: StoreConfig()
                } else {
                    StoreConfig()
                }
            })
        )
    }

    suspend fun saveStoreConfig(config: StoreConfig): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("store_config").document("main")
        val payload = buildMap<String, Any> {
            put("businessName", config.businessName.ifBlank { "Bazar Zaatar" })
            put("businessNameAr", config.businessNameAr.ifBlank { "بازار زعتر" })
            put("address", config.address.ifBlank { "Volani Lavinir, Tiaret, Algeria, 14000" })
            put("latitude", config.latitude)
            put("longitude", config.longitude)
            put("mapsUrl", config.mapsUrl.ifBlank { "https://www.google.com/maps?q=35.3609386,1.3225709" })
            put("phoneNumber", config.phoneNumber.trim())
            put("whatsappNumber", config.whatsappNumber.trim())
            put("facebookUrl", config.facebookUrl.trim())
            put("instagramUrl", config.instagramUrl.trim())
            put("bannerTitleAr", config.bannerTitleAr.trim())
            put("bannerTitleFr", config.bannerTitleFr.trim())
            put("bannerSubtitleAr", config.bannerSubtitleAr.trim())
            put("bannerSubtitleFr", config.bannerSubtitleFr.trim())
            put("bannerActive", config.bannerActive)
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // --- Categories ---
    fun observeActiveCategories(): Flow<List<Category>> = flow {
        val uid = requireUserId()
        val path = "categories"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .whereEqualTo("active", true)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedBy { it.sortOrder }
                    }
            )
        )
    }

    fun observeAllCategories(): Flow<List<Category>> = flow {
        val uid = requireUserId()
        val path = "categories"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Category::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedBy { it.sortOrder }
                    }
            )
        )
    }

    suspend fun upsertCategory(category: Category): Result<String> = runCatching {
        requireUserId()
        val id = category.id.ifBlank { "cat_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val docRef = db.collection("categories").document(id)
        try {
            val isUpdate = category.id.isNotBlank() && category.createdAt != null
            if (isUpdate) {
                val payload = mapOf(
                    "nameAr" to category.nameAr.trim(),
                    "nameFr" to category.nameFr.trim(),
                    "slug" to category.slug.trim(),
                    "sortOrder" to category.sortOrder,
                    "active" to category.active,
                    "iconKey" to category.iconKey.ifBlank { "clothing" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(payload).await()
            } else {
                val payload = mapOf(
                    "nameAr" to category.nameAr.trim(),
                    "nameFr" to category.nameFr.trim(),
                    "slug" to category.slug.trim(),
                    "sortOrder" to category.sortOrder,
                    "active" to category.active,
                    "iconKey" to category.iconKey.ifBlank { "clothing" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun initializeDefaultCategories(): Result<Unit> = runCatching {
        requireUserId()
        val batch = db.batch()
        DefaultCategories.officialCategories.forEach { cat ->
            val docRef = db.collection("categories").document(cat.id)
            val payload = mapOf(
                "nameAr" to cat.nameAr,
                "nameFr" to cat.nameFr,
                "slug" to cat.slug,
                "sortOrder" to cat.sortOrder,
                "active" to cat.active,
                "iconKey" to cat.iconKey,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(docRef, payload)
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "categories", auth)
            throw e
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("categories").document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- Products ---
    fun observeActiveProducts(): Flow<List<Product>> = flow {
        val uid = requireUserId()
        val path = "products"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .whereEqualTo("active", true)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    fun observeAllProducts(): Flow<List<Product>> = flow {
        val uid = requireUserId()
        val path = "products"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    suspend fun upsertProduct(product: Product): Result<String> = runCatching {
        requireUserId()
        val id = product.id.ifBlank { "prod_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val docRef = db.collection("products").document(id)
        val basePayload = buildMap<String, Any> {
            put("nameAr", product.nameAr.trim())
            put("nameFr", product.nameFr.trim())
            put("descriptionAr", product.descriptionAr.trim())
            put("descriptionFr", product.descriptionFr.trim())
            put("categoryId", product.categoryId.trim())
            put("priceDzd", product.priceDzd)
            product.discountPriceDzd?.takeIf { it > 0.0 && it < product.priceDzd }?.let {
                put("discountPriceDzd", it)
            }
            put("sizes", product.sizes)
            put("colors", product.colors)
            put("stockQuantity", product.stockQuantity.coerceAtLeast(0L))
            put("inStock", product.inStock && product.stockQuantity > 0L)
            put("featured", product.featured)
            put("newArrival", product.newArrival)
            put("specialOffer", product.specialOffer)
            put("active", product.active)
            put("imageUrl", product.imageUrl.trim())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            val isUpdate = product.id.isNotBlank() && product.createdAt != null
            if (isUpdate) {
                val updateMap = basePayload.toMutableMap()
                if (product.discountPriceDzd == null || product.discountPriceDzd <= 0.0 || product.discountPriceDzd >= product.priceDzd) {
                    updateMap["discountPriceDzd"] = FieldValue.delete()
                }
                docRef.update(updateMap).await()
            } else {
                val createMap = basePayload + ("createdAt" to FieldValue.serverTimestamp())
                docRef.set(createMap).await()
            }
            id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun updateProductFlags(
        productId: String,
        active: Boolean,
        inStock: Boolean
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.update(
                mapOf(
                    "active" to active,
                    "inStock" to inStock,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- User Profile ---
    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            wrapFirestoreFlow(docRef.path, OperationType.GET, userId, docRef.snapshots().map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            })
        )
    }

    suspend fun upsertUserProfile(
        displayName: String,
        email: String,
        phoneNumber: String,
        defaultAddress: String,
        preferredLang: String,
        existingCreatedAt: com.google.firebase.Timestamp?
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            if (existingCreatedAt != null) {
                docRef.update(
                    mapOf(
                        "displayName" to displayName.trim().ifBlank { "Client" },
                        "email" to email.trim(),
                        "phoneNumber" to phoneNumber.trim(),
                        "defaultAddress" to defaultAddress.trim(),
                        "preferredLang" to if (preferredLang == "fr") "fr" else "ar",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to displayName.trim().ifBlank { "Client" },
                        "email" to email.trim(),
                        "phoneNumber" to phoneNumber.trim(),
                        "defaultAddress" to defaultAddress.trim(),
                        "preferredLang" to if (preferredLang == "fr") "fr" else "ar",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // --- Cart ---
    fun observeCart(userId: String): Flow<List<CartItem>> = flow {
        val path = "users/$userId/cart"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                userId,
                db.collection("users").document(userId).collection("cart")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    suspend fun addToCart(
        product: Product,
        selectedSize: String,
        selectedColor: String,
        quantity: Long = 1L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val sanitizedKey = "${product.id}_${selectedSize}_${selectedColor}"
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(120)
        val docRef = db.collection("users").document(uid).collection("cart").document(sanitizedKey)
        try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                val currentQty = existingSnap.getLong("quantity") ?: 1L
                val newQty = (currentQty + quantity).coerceIn(1L, 99L)
                docRef.update(
                    mapOf(
                        "quantity" to newQty,
                        "unitPriceDzd" to product.effectivePriceDzd,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "productId" to product.id,
                    "productNameAr" to product.nameAr,
                    "productNameFr" to product.nameFr,
                    "productImageUrl" to product.imageUrl,
                    "unitPriceDzd" to product.effectivePriceDzd,
                    "selectedSize" to selectedSize,
                    "selectedColor" to selectedColor,
                    "quantity" to quantity.coerceIn(1L, 99L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            sanitizedKey
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun updateCartQuantity(cartItem: CartItem, newQuantity: Long): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(cartItem.id)
        try {
            if (newQuantity <= 0L) {
                docRef.delete().await()
            } else {
                docRef.update(
                    mapOf(
                        "quantity" to newQuantity.coerceIn(1L, 99L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun removeCartItem(cartItemId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(cartItemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- Orders ---
    fun observeUserOrders(userId: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                userId,
                db.collection(path)
                    .whereEqualTo("userId", userId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    fun observeAllOrders(): Flow<List<Order>> = flow {
        val uid = requireUserId()
        val path = "orders"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    suspend fun placeOrder(
        customerName: String,
        phoneNumber: String,
        deliveryAddress: String,
        cartItems: List<CartItem>,
        notes: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(cartItems.isNotEmpty()) { "Cart cannot be empty" }
        require(cartItems.size <= 10) { "A maximum of 10 cart lines can be placed in one order." }
        require(customerName.trim().length in 2..100) { "Invalid customer name" }
        require(phoneNumber.trim().length in 6..40) { "Invalid phone number" }
        require(deliveryAddress.trim().length in 4..300) { "Invalid delivery address" }
        require(notes.trim().length <= 500) { "Order notes are too long" }

        val orderRef = db.collection("orders").document()
        try {
            db.runTransaction { transaction ->
                // Read every distinct product before writing the order.
                val productIds = cartItems.map { it.productId }.distinct()
                val productSnapshots = productIds.associateWith { productId ->
                    transaction.get(db.collection("products").document(productId))
                }
                val totalQuantityByProduct = cartItems.groupBy { it.productId }
                    .mapValues { (_, lines) -> lines.sumOf { it.quantity } }

                val orderItems = cartItems.map { item ->
                    val snapshot = productSnapshots[item.productId]
                        ?: throw IllegalStateException("Product is missing from the cart.")
                    require(snapshot.exists()) { "A product is no longer available." }
                    val product = snapshot.toObject(Product::class.java)
                        ?: throw IllegalStateException("Product data is invalid.")
                    val totalProductQuantity = totalQuantityByProduct[item.productId] ?: item.quantity
                    require(product.active && product.inStock && product.stockQuantity >= totalProductQuantity) {
                        "Insufficient stock for ${product.nameAr}."
                    }
                    require(item.selectedSize.isBlank() || product.sizes.isEmpty() || item.selectedSize in product.sizes) {
                        "Selected size is no longer available for ${product.nameAr}."
                    }
                    require(item.selectedColor.isBlank() || product.colors.isEmpty() || item.selectedColor in product.colors) {
                        "Selected color is no longer available for ${product.nameAr}."
                    }
                    val unitPrice = product.effectivePriceDzd
                    require(unitPrice > 0.0 && unitPrice.isFinite()) { "Product price is invalid." }
                    mapOf(
                        "productId" to item.productId,
                        "nameAr" to product.nameAr.ifBlank { product.nameFr },
                        "nameFr" to product.nameFr.ifBlank { product.nameAr },
                        "size" to item.selectedSize,
                        "color" to item.selectedColor,
                        "quantity" to item.quantity.toInt(),
                        "unitPriceDzd" to unitPrice
                    )
                }
                val subtotalDzd = orderItems.sumOf { line ->
                    (line["quantity"] as Int).toDouble() * (line["unitPriceDzd"] as Double)
                }
                require(subtotalDzd > 0.0 && subtotalDzd.isFinite()) { "Order total is invalid." }

                transaction.set(orderRef, mapOf(
                    "userId" to uid,
                    "customerName" to customerName.trim(),
                    "phoneNumber" to phoneNumber.trim(),
                    "deliveryAddress" to deliveryAddress.trim(),
                    "items" to orderItems,
                    "subtotalDzd" to subtotalDzd,
                    "totalDzd" to subtotalDzd,
                    "status" to "pending",
                    "notes" to notes.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                orderRef.id
            }.await()

            // Clear cart records only after Firestore confirms that the order was created.
            val batch = db.batch()
            cartItems.forEach { item ->
                val cartRef = db.collection("users").document(uid).collection("cart").document(item.id)
                batch.delete(cartRef)
            }
            try {
                batch.commit().await()
            } catch (cleanupError: Exception) {
                android.util.Log.w("BazarZaatarRepository", "Order ${orderRef.id} created, but cart cleanup failed", cleanupError)
            }
            orderRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "orders", auth)
            throw e
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    // --- Reservations ---
    fun observeUserReservations(userId: String): Flow<List<Reservation>> = flow {
        val path = "reservations"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                userId,
                db.collection(path)
                    .whereEqualTo("userId", userId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Reservation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    fun observeAllReservations(): Flow<List<Reservation>> = flow {
        val uid = requireUserId()
        val path = "reservations"
        emitAll(
            wrapFirestoreFlow(
                path,
                OperationType.LIST,
                uid,
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Reservation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    }
            )
        )
    }

    suspend fun createReservation(
        customerName: String,
        phoneNumber: String,
        preferredDate: String,
        preferredTime: String,
        visitorCount: Long,
        notes: String,
        channel: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val resId = "res_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val docRef = db.collection("reservations").document(resId)
        val payload = buildMap<String, Any> {
            put("userId", uid)
            put("customerName", customerName.trim())
            put("phoneNumber", phoneNumber.trim())
            put("preferredDate", preferredDate.trim())
            put("preferredTime", preferredTime.trim())
            put("visitorCount", visitorCount.coerceIn(1L, 20L))
            if (notes.isNotBlank()) put("notes", notes.trim())
            put("channel", if (channel == "whatsapp") "whatsapp" else "in_app")
            put("status", "pending")
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            docRef.set(payload).await()
            resId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun updateReservationStatus(reservationId: String, newStatus: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("reservations").document(reservationId)
        try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "mildous15@gmail.com"
    }
}
