package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BazarZaatarRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun customerCartAndOrder_ownerCanCreateAndReadOwnData() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = BazarZaatarRepository(firestore, auth)

        val product = Product(
            id = "prod_${UUID.randomUUID().toString().take(8)}",
            nameAr = "قميص رجالي",
            nameFr = "Chemise Homme",
            categoryId = "mens_shirts",
            priceDzd = 4200.0,
            sizes = listOf("M", "L"),
            colors = listOf("Blanc"),
            stockQuantity = 5L,
            inStock = true
        )

        val cartResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addToCart(product, selectedSize = "M", selectedColor = "Blanc", quantity = 2L)
        }
        assertTrue("addToCart failed: ${cartResult.exceptionOrNull()}", cartResult.isSuccess)

        val cartItems = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCart(uid).first { it.any { c -> c.productId == product.id } }
        }
        assertEquals(1, cartItems.size)
        assertEquals(2L, cartItems.first().quantity)

        val orderResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.placeOrder(
                customerName = "Alice Tiaret",
                phoneNumber = "0555010203",
                deliveryAddress = "Tiaret 14000",
                cartItems = cartItems
            )
        }
        assertTrue("placeOrder failed: ${orderResult.exceptionOrNull()}", orderResult.isSuccess)
        val orderId = orderResult.getOrThrow()

        val orders = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserOrders(uid).first { it.any { o -> o.id == orderId } }
        }
        assertTrue(orders.any { it.id == orderId && it.totalDzd == 8400.0 })
    }

    @Test
    fun reservation_ownerCanCreateAndObserve() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = BazarZaatarRepository(firestore, auth)

        val resResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createReservation(
                customerName = "Alice Tiaret",
                phoneNumber = "0555010203",
                preferredDate = "2026-11-25",
                preferredTime = "14:30",
                visitorCount = 2L,
                notes = "Men blazer fitting",
                channel = "whatsapp"
            )
        }
        assertTrue("createReservation failed: ${resResult.exceptionOrNull()}", resResult.isSuccess)
        val resId = resResult.getOrThrow()

        val reservations = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserReservations(uid).first { it.any { r -> r.id == resId } }
        }
        assertTrue(reservations.any { it.id == resId && it.visitorCount == 2L })
    }

    @Test
    fun crossUserOrderIsolation_otherUserCannotListAliceOrders() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BazarZaatarRepository(firestore, auth)
        val cartItem = CartItem(
            id = "item_${UUID.randomUUID().toString().take(8)}",
            userId = aliceUid,
            productId = "prod_test",
            productNameAr = "منتج",
            productNameFr = "Produit",
            unitPriceDzd = 3000.0,
            selectedSize = "L",
            selectedColor = "Noir",
            quantity = 1L
        )
        val orderResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.placeOrder("Alice", "0555112233", "Tiaret", listOf(cartItem))
        }
        assertTrue(orderResult.isSuccess)

        // Sign in as Bob and attempt to read Alice's orders
        signInTestUser(BOB_EMAIL)
        val bobRepo = BazarZaatarRepository(firestore, auth)
        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeUserOrders(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected PERMISSION_DENIED for cross-user order query, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    @Test
    fun unauthenticatedProductQuery_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = BazarZaatarRepository(firestore, auth)
        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeUserOrders("someone").first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected PERMISSION_DENIED for unauthenticated query, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice.bazar@test.com"
        const val BOB_EMAIL = "bob.bazar@test.com"
        const val DEFAULT_TIMEOUT_MS = 10000L
        const val FLOW_TIMEOUT_MS = 10000L
    }
}
