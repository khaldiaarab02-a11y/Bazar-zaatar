package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminRole
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.DefaultCategories
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Reservation
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.repository.BazarZaatarRepository
import com.example.ui.i18n.AppLanguage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class BazarZaatarViewModel(
    private val repository: BazarZaatarRepository,
    val currentUserId: String,
    val userEmail: String?,
    val userDisplayName: String?,
    val emailVerified: Boolean
) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.AR)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private fun <T> Flow<T>.toUiState(tagLabel: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $tagLabel", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load $tagLabel"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val storeConfig: StateFlow<StoreConfig> = repository.observeStoreConfig()
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing storeConfig", error)
            emit(StoreConfig())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StoreConfig()
        )

    val categoriesState: StateFlow<UiState<List<Category>>> = repository.observeActiveCategories()
        .map { list -> list.ifEmpty { DefaultCategories.officialCategories } }
        .toUiState("categories")

    val productsState: StateFlow<UiState<List<Product>>> = repository.observeActiveProducts()
        .toUiState("products")

    val cartState: StateFlow<UiState<List<CartItem>>> = repository.observeCart(currentUserId)
        .toUiState("cart")

    val userOrdersState: StateFlow<UiState<List<Order>>> = repository.observeUserOrders(currentUserId)
        .toUiState("orders")

    val userReservationsState: StateFlow<UiState<List<Reservation>>> =
        repository.observeUserReservations(currentUserId).toUiState("reservations")

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing userProfile", error)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminProductsState: StateFlow<UiState<List<Product>>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) repository.observeAllProducts().map<List<Product>, UiState<List<Product>>> { UiState.Success(it) }
            else flowOf(UiState.Success(emptyList()))
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminCategoriesState: StateFlow<UiState<List<Category>>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) repository.observeAllCategories().map<List<Category>, UiState<List<Category>>> { UiState.Success(it) }
            else flowOf(UiState.Success(emptyList()))
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminOrdersState: StateFlow<UiState<List<Order>>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) repository.observeAllOrders().map<List<Order>, UiState<List<Order>>> { UiState.Success(it) }
            else flowOf(UiState.Success(emptyList()))
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminReservationsState: StateFlow<UiState<List<Reservation>>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) repository.observeAllReservations().map<List<Reservation>, UiState<List<Reservation>>> { UiState.Success(it) }
            else flowOf(UiState.Success(emptyList()))
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminRolesState: StateFlow<UiState<List<AdminRole>>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) repository.observeAdmins().map<List<AdminRole>, UiState<List<AdminRole>>> { UiState.Success(it) }
            else flowOf(UiState.Success(emptyList()))
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allUsersState: StateFlow<UiState<List<UserProfile>>> = _isAdmin
        .flatMapLatest { admin ->
            if (admin) repository.observeAllUsers().map<List<UserProfile>, UiState<List<UserProfile>>> { UiState.Success(it) }
            else flowOf(UiState.Success(emptyList()))
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val cartItemCount: StateFlow<Int> = cartState
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.sumOf { it.quantity.toInt() }
                else -> 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0)

    init {
        refreshAdminStatus()
    }

    fun refreshAdminStatus() {
        viewModelScope.launch {
            _isAdmin.value = repository.checkIsAdmin()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        val profile = userProfile.value
        viewModelScope.launch {
            repository.upsertUserProfile(
                displayName = profile?.displayName ?: userDisplayName ?: "Client",
                email = profile?.email ?: userEmail.orEmpty(),
                phoneNumber = profile?.phoneNumber.orEmpty(),
                defaultAddress = profile?.defaultAddress.orEmpty(),
                preferredLang = lang.code,
                existingCreatedAt = profile?.createdAt
            )
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun addToCart(
        product: Product,
        selectedSize: String,
        selectedColor: String,
        quantity: Long = 1L,
        successMessage: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.addToCart(product, selectedSize, selectedColor, quantity)
            _isBusy.value = false
            if (res.isSuccess) {
                _feedbackMessage.value = successMessage
            } else {
                _feedbackMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Error adding to cart"
            }
        }
    }

    fun updateCartQuantity(item: CartItem, newQty: Long) {
        viewModelScope.launch {
            repository.updateCartQuantity(item, newQty)
        }
    }

    fun removeCartItem(cartItemId: String) {
        viewModelScope.launch {
            repository.removeCartItem(cartItemId)
        }
    }

    fun placeOrder(
        customerName: String,
        phoneNumber: String,
        deliveryAddress: String,
        cartItems: List<CartItem>,
        notes: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.placeOrder(customerName, phoneNumber, deliveryAddress, cartItems, notes)
            if (result.isSuccess) {
                val profile = userProfile.value
                repository.upsertUserProfile(
                    displayName = customerName,
                    email = userEmail.orEmpty(),
                    phoneNumber = phoneNumber,
                    defaultAddress = deliveryAddress,
                    preferredLang = _language.value.code,
                    existingCreatedAt = profile?.createdAt
                )
                _isBusy.value = false
                onSuccess(result.getOrThrow())
            } else {
                _isBusy.value = false
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to submit order"
            }
        }
    }

    fun createReservation(
        customerName: String,
        phoneNumber: String,
        preferredDate: String,
        preferredTime: String,
        visitorCount: Long,
        notes: String,
        channel: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createReservation(
                customerName, phoneNumber, preferredDate, preferredTime, visitorCount, notes, channel
            )
            _isBusy.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save reservation"
            }
        }
    }

    // --- Admin Operations ---
    fun saveStoreConfig(config: StoreConfig, successMsg: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.saveStoreConfig(config)
            _isBusy.value = false
            _feedbackMessage.value = if (res.isSuccess) successMsg else (res.exceptionOrNull()?.localizedMessage ?: "Error")
        }
    }

    fun upsertProduct(product: Product, successMsg: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.upsertProduct(product)
            _isBusy.value = false
            if (res.isSuccess) {
                _feedbackMessage.value = successMsg
                onDone()
            } else {
                _feedbackMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Error saving product"
            }
        }
    }

    fun deleteProduct(productId: String, successMsg: String) {
        viewModelScope.launch {
            val res = repository.deleteProduct(productId)
            _feedbackMessage.value = if (res.isSuccess) successMsg else (res.exceptionOrNull()?.localizedMessage ?: "Error")
        }
    }

    fun upsertCategory(category: Category, successMsg: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.upsertCategory(category)
            _isBusy.value = false
            if (res.isSuccess) {
                _feedbackMessage.value = successMsg
                onDone()
            } else {
                _feedbackMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Error"
            }
        }
    }

    fun initializeDefaultCategories(successMsg: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.initializeDefaultCategories()
            _isBusy.value = false
            _feedbackMessage.value = if (res.isSuccess) successMsg else (res.exceptionOrNull()?.localizedMessage ?: "Error")
        }
    }

    fun deleteCategory(categoryId: String, successMsg: String) {
        viewModelScope.launch {
            val res = repository.deleteCategory(categoryId)
            _feedbackMessage.value = if (res.isSuccess) successMsg else (res.exceptionOrNull()?.localizedMessage ?: "Error")
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun updateReservationStatus(reservationId: String, status: String) {
        viewModelScope.launch {
            repository.updateReservationStatus(reservationId, status)
        }
    }

    fun quickToggleProductFlags(productId: String, active: Boolean, inStock: Boolean) {
        viewModelScope.launch {
            val res = repository.updateProductFlags(productId, active, inStock)
            if (res.isFailure) {
                _feedbackMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Error updating product"
            }
        }
    }

    fun grantAdminAccess(targetUid: String, targetEmail: String, successMsg: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.grantAdminAccess(targetUid, targetEmail)
            _isBusy.value = false
            _feedbackMessage.value = if (res.isSuccess) successMsg else (res.exceptionOrNull()?.localizedMessage ?: "Error")
        }
    }

    fun revokeAdminAccess(targetUid: String, successMsg: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.revokeAdminAccess(targetUid)
            _isBusy.value = false
            _feedbackMessage.value = if (res.isSuccess) successMsg else (res.exceptionOrNull()?.localizedMessage ?: "Error")
        }
    }

    private companion object {
        const val TAG = "BazarZaatarVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
