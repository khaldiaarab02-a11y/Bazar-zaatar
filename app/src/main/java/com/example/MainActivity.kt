package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.repository.BazarZaatarRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.auth.switchGoogleAccount
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BazarStrings
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ContactLocationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductDetailModal
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.ReservationModal
import com.example.ui.theme.BazarZaatarTheme
import com.example.ui.viewmodel.BazarZaatarViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

const val ROUTE_HOME = "home"
const val ROUTE_CATEGORIES = "categories"
const val ROUTE_PRODUCTS = "products"
const val ROUTE_CART = "cart"
const val ROUTE_ACCOUNT = "account"
const val ROUTE_CONTACT_LOCATION = "contact_location"
const val ROUTE_ADMIN_DASHBOARD = "admin_dashboard"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BazarZaatarTheme {
                BazarZaatarAppRoot()
            }
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun BazarZaatarAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var preAuthLanguage by remember { mutableStateOf(AppLanguage.AR) }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            language = preAuthLanguage,
            onLanguageChange = { preAuthLanguage = it },
            onAuthSuccess = { /* AuthStateListener updates currentUser */ }
        )
    } else {
        key(user.uid) {
            AuthenticatedBazarZaatarApp(
                currentUserId = user.uid,
                userEmail = user.email,
                userDisplayName = user.displayName,
                emailVerified = user.isEmailVerified,
                initialLanguage = preAuthLanguage
            )
        }
    }
}

@Composable
private fun AuthenticatedBazarZaatarApp(
    currentUserId: String,
    userEmail: String?,
    userDisplayName: String?,
    emailVerified: Boolean,
    initialLanguage: AppLanguage
) {
    val viewModel: BazarZaatarViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repo = BazarZaatarRepository(db, Firebase.auth)
                BazarZaatarViewModel(
                    repository = repo,
                    currentUserId = currentUserId,
                    userEmail = userEmail,
                    userDisplayName = userDisplayName,
                    emailVerified = emailVerified
                )
            }
        }
    )

    LaunchedEffect(initialLanguage) {
        if (initialLanguage != AppLanguage.AR) {
            viewModel.setLanguage(initialLanguage)
        }
    }

    val language by viewModel.language.collectAsStateWithLifecycle()
    val strings = remember(language) { BazarStrings.forLanguage(language) }
    val layoutDirection = if (language == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    val storeConfig by viewModel.storeConfig.collectAsStateWithLifecycle()
    val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
    val productsState by viewModel.productsState.collectAsStateWithLifecycle()
    val cartState by viewModel.cartState.collectAsStateWithLifecycle()
    val ordersState by viewModel.userOrdersState.collectAsStateWithLifecycle()
    val reservationsState by viewModel.userReservationsState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartItemCount.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val adminProductsState by viewModel.adminProductsState.collectAsStateWithLifecycle()
    val adminCategoriesState by viewModel.adminCategoriesState.collectAsStateWithLifecycle()
    val adminOrdersState by viewModel.adminOrdersState.collectAsStateWithLifecycle()
    val adminReservationsState by viewModel.adminReservationsState.collectAsStateWithLifecycle()
    val adminRolesState by viewModel.adminRolesState.collectAsStateWithLifecycle()
    val allUsersState by viewModel.allUsersState.collectAsStateWithLifecycle()

    val categories = (categoriesState as? UiState.Success)?.data.orEmpty()
    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val cartItems = (cartState as? UiState.Success)?.data.orEmpty()
    val userOrders = (ordersState as? UiState.Success)?.data.orEmpty()
    val userReservations = (reservationsState as? UiState.Success)?.data.orEmpty()
    val adminProducts = (adminProductsState as? UiState.Success)?.data ?: products
    val adminOrders = (adminOrdersState as? UiState.Success)?.data.orEmpty()
    val adminReservations = (adminReservationsState as? UiState.Success)?.data.orEmpty()
    val adminRoles = (adminRolesState as? UiState.Success)?.data.orEmpty()
    val registeredUsers = (allUsersState as? UiState.Success)?.data.orEmpty()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var activeDetailProduct by remember { mutableStateOf<Product?>(null) }
    var showReservationModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(feedback) {
        val msg = feedback ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearFeedback()
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showBottomBar = currentRoute in setOf(
        ROUTE_HOME, ROUTE_CATEGORIES, ROUTE_PRODUCTS, ROUTE_CART, ROUTE_ACCOUNT
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        val navItems = listOf(
                            Triple(ROUTE_HOME, strings.navHome, Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                            Triple(ROUTE_CATEGORIES, strings.navCategories, Pair(Icons.Filled.Category, Icons.Outlined.Category)),
                            Triple(ROUTE_PRODUCTS, strings.navProducts, Pair(Icons.Filled.Checkroom, Icons.Outlined.Checkroom)),
                            Triple(ROUTE_CART, strings.navCart, Pair(Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)),
                            Triple(ROUTE_ACCOUNT, strings.navAccount, Pair(Icons.Filled.Person, Icons.Outlined.Person))
                        )
                        navItems.forEach { (route, label, icons) ->
                            val selected = currentRoute == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(route) },
                                icon = {
                                    val iconVector = if (selected) icons.first else icons.second
                                    if (route == ROUTE_CART && cartCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge { Text("$cartCount") }
                                            }
                                        ) {
                                            Icon(iconVector, contentDescription = label)
                                        }
                                    } else {
                                        Icon(iconVector, contentDescription = label)
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_$route")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            strings = strings,
                            storeConfig = storeConfig,
                            categories = categories,
                            products = products,
                            userDisplayName = userProfile?.displayName ?: userDisplayName.orEmpty(),
                            userEmail = userEmail.orEmpty(),
                            isAdmin = isAdmin,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onShopNowClick = {
                                selectedCategoryId = null
                                navigateToTab(ROUTE_PRODUCTS)
                            },
                            onCategoryClick = { cat ->
                                selectedCategoryId = cat.id
                                navigateToTab(ROUTE_PRODUCTS)
                            },
                            onProductClick = { activeDetailProduct = it },
                            onQuickAddProduct = { prod ->
                                viewModel.addToCart(
                                    product = prod,
                                    selectedSize = prod.sizes.firstOrNull().orEmpty(),
                                    selectedColor = prod.colors.firstOrNull().orEmpty(),
                                    quantity = 1L,
                                    successMessage = strings.addedToCartMsg
                                )
                            },
                            onReserveClick = { showReservationModal = true },
                            onContactClick = { navController.navigate(ROUTE_CONTACT_LOCATION) },
                            onOpenAccountClick = { navigateToTab(ROUTE_ACCOUNT) },
                            onOpenAdminClick = { navController.navigate(ROUTE_ADMIN_DASHBOARD) },
                            onSwitchAccountClick = {
                                switchGoogleAccount(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSwitchError = { msg -> viewModel.showFeedback(msg) },
                                    scope = scope
                                )
                            },
                            onSignOutClick = {
                                signOut(credentialManager, scope)
                            }
                        )
                    }

                    composable(ROUTE_CATEGORIES) {
                        CategoriesScreen(
                            strings = strings,
                            categories = categories,
                            products = products,
                            onCategorySelected = { cat ->
                                selectedCategoryId = cat.id
                                navigateToTab(ROUTE_PRODUCTS)
                            },
                            onReserveClick = { showReservationModal = true }
                        )
                    }

                    composable(ROUTE_PRODUCTS) {
                        ProductsScreen(
                            strings = strings,
                            categories = categories,
                            products = products,
                            selectedCategoryId = selectedCategoryId,
                            onSelectCategoryId = { selectedCategoryId = it },
                            onProductClick = { activeDetailProduct = it },
                            onQuickAddProduct = { prod ->
                                viewModel.addToCart(
                                    product = prod,
                                    selectedSize = prod.sizes.firstOrNull().orEmpty(),
                                    selectedColor = prod.colors.firstOrNull().orEmpty(),
                                    quantity = 1L,
                                    successMessage = strings.addedToCartMsg
                                )
                            },
                            isAdmin = isAdmin,
                            onOpenAdminClick = { navController.navigate(ROUTE_ADMIN_DASHBOARD) }
                        )
                    }

                    composable(ROUTE_CART) {
                        CartScreen(
                            strings = strings,
                            cartItems = cartItems,
                            defaultName = userProfile?.displayName ?: userDisplayName.orEmpty(),
                            defaultPhone = userProfile?.phoneNumber.orEmpty(),
                            defaultAddress = userProfile?.defaultAddress.orEmpty(),
                            isBusy = isBusy,
                            onUpdateQuantity = { item, qty -> viewModel.updateCartQuantity(item, qty) },
                            onRemoveItem = { id -> viewModel.removeCartItem(id) },
                            onPlaceOrder = { name, phone, address, notes, onSuccess ->
                                viewModel.placeOrder(name, phone, address, cartItems, notes, onSuccess)
                            },
                            onBrowseProducts = { navigateToTab(ROUTE_PRODUCTS) }
                        )
                    }

                    composable(ROUTE_ACCOUNT) {
                        AccountScreen(
                            strings = strings,
                            userDisplayName = userProfile?.displayName ?: userDisplayName.orEmpty(),
                            userEmail = userEmail.orEmpty(),
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            adminProductCount = adminProducts.size,
                            pendingOrdersCount = adminOrders.count { it.status == "pending" },
                            pendingReservationsCount = adminReservations.count { it.status == "pending" },
                            orders = userOrders,
                            reservations = userReservations,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onOpenContactLocation = { navController.navigate(ROUTE_CONTACT_LOCATION) },
                            onOpenAdminDashboard = { navController.navigate(ROUTE_ADMIN_DASHBOARD) },
                            onRefreshAdminStatus = { viewModel.refreshAdminStatus() },
                            onReserveClick = { showReservationModal = true },
                            onSignOut = {
                                signOut(credentialManager, scope)
                            }
                        )
                    }

                    composable(ROUTE_CONTACT_LOCATION) {
                        BackHandler { navController.popBackStack() }
                        ContactLocationScreen(
                            strings = strings,
                            storeConfig = storeConfig,
                            onBack = { navController.popBackStack() },
                            onReserveClick = { showReservationModal = true },
                            onNotify = { viewModel.showFeedback(it) }
                        )
                    }

                    composable(ROUTE_ADMIN_DASHBOARD) {
                        BackHandler { navController.popBackStack() }
                        AdminDashboardScreen(
                            strings = strings,
                            currentUserId = currentUserId,
                            currentUserEmail = userEmail.orEmpty(),
                            storeConfig = storeConfig,
                            products = adminProducts,
                            categories = (adminCategoriesState as? UiState.Success)?.data ?: categories,
                            orders = adminOrders,
                            reservations = adminReservations,
                            adminRoles = adminRoles,
                            registeredUsers = registeredUsers,
                            onBack = { navController.popBackStack() },
                            onSaveProduct = { prod ->
                                viewModel.upsertProduct(prod, strings.saveLabel)
                            },
                            onQuickToggleProduct = { productId, active, inStock ->
                                viewModel.quickToggleProductFlags(productId, active, inStock)
                            },
                            onDeleteProduct = { id ->
                                viewModel.deleteProduct(id, strings.deleteLabel)
                            },
                            onSaveCategory = { cat ->
                                viewModel.upsertCategory(cat, strings.saveLabel)
                            },
                            onInitDefaultCategories = {
                                viewModel.initializeDefaultCategories(strings.saveLabel)
                            },
                            onDeleteCategory = { id ->
                                viewModel.deleteCategory(id, strings.deleteLabel)
                            },
                            onUpdateOrderStatus = { orderId, status ->
                                viewModel.updateOrderStatus(orderId, status)
                            },
                            onUpdateReservationStatus = { resId, status ->
                                viewModel.updateReservationStatus(resId, status)
                            },
                            onSaveStoreConfig = { cfg ->
                                viewModel.saveStoreConfig(cfg, strings.saveLabel)
                            },
                            onGrantAdminAccess = { uid, email ->
                                viewModel.grantAdminAccess(uid, email, strings.saveLabel)
                            },
                            onRevokeAdminAccess = { uid ->
                                viewModel.revokeAdminAccess(uid, strings.deleteLabel)
                            }
                        )
                    }
                }
            }
        }

        if (activeDetailProduct != null) {
            ProductDetailModal(
                product = activeDetailProduct!!,
                strings = strings,
                onDismiss = { activeDetailProduct = null },
                onAddToCart = { size, color, qty ->
                    val prod = activeDetailProduct!!
                    viewModel.addToCart(
                        product = prod,
                        selectedSize = size,
                        selectedColor = color,
                        quantity = qty,
                        successMessage = strings.addedToCartMsg
                    )
                    activeDetailProduct = null
                }
            )
        }

        if (showReservationModal) {
            ReservationModal(
                strings = strings,
                storeConfig = storeConfig,
                initialName = userProfile?.displayName ?: userDisplayName.orEmpty(),
                initialPhone = userProfile?.phoneNumber.orEmpty(),
                isBusy = isBusy,
                onDismiss = { showReservationModal = false },
                onSubmitReservation = { name, phone, date, time, visitors, notes, channel, onSaved ->
                    viewModel.createReservation(
                        name, phone, date, time, visitors, notes, channel, onSaved
                    )
                },
                onNotify = { viewModel.showFeedback(it) }
            )
        }
    }
}
