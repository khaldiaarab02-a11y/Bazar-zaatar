package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminRole
import com.example.data.model.Category
import com.example.data.model.DefaultCategories
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Reservation
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.repository.BazarZaatarRepository
import com.example.ui.components.BazarProductImage
import com.example.ui.components.encodeImageUriToBase64DataUri
import com.example.ui.components.formatDzd
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BazarStrings
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonSale
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer
import com.example.ui.theme.ZaatarEmerald

@Composable
fun AdminDashboardScreen(
    strings: BazarStrings,
    currentUserId: String,
    currentUserEmail: String,
    storeConfig: StoreConfig,
    products: List<Product>,
    categories: List<Category>,
    orders: List<Order>,
    reservations: List<Reservation>,
    adminRoles: List<AdminRole>,
    registeredUsers: List<UserProfile>,
    onBack: () -> Unit,
    onSaveProduct: (Product) -> Unit,
    onQuickToggleProduct: (productId: String, active: Boolean, inStock: Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onSaveCategory: (Category) -> Unit,
    onInitDefaultCategories: () -> Unit,
    onDeleteCategory: (String) -> Unit,
    onUpdateOrderStatus: (String, String) -> Unit,
    onUpdateReservationStatus: (String, String) -> Unit,
    onSaveStoreConfig: (StoreConfig) -> Unit,
    onGrantAdminAccess: (uid: String, email: String) -> Unit,
    onRevokeAdminAccess: (uid: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showProductEditor by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryEditor by remember { mutableStateOf(false) }

    val availableCategories = categories.ifEmpty { DefaultCategories.officialCategories }
    val tabs = listOf(
        strings.adminOverviewTab,
        strings.adminProductsTab,
        strings.adminCategoriesTab,
        strings.adminOrdersTab,
        strings.adminReservationsTab,
        strings.adminStoreInfoTab,
        strings.adminAccessTab
    )

    val openNewProductEditor = {
        editingProduct = Product(
            categoryId = availableCategories.firstOrNull()?.id ?: "mens_clothing",
            sizes = listOf("S", "M", "L", "XL"),
            colors = listOf("Noir", "Blanc"),
            stockQuantity = 10L
        )
        showProductEditor = true
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancelLabel,
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.ownerControlPanelTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentUserEmail.isNotBlank()) {
                            Text(
                                text = currentUserEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = ChampagneGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    edgePadding = 12.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("admin_tab_$index")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(
                    onClick = openNewProductEditor,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addProduct)
                }
            } else if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        editingCategory = Category(sortOrder = (categories.size + 1).toLong())
                        showCategoryEditor = true
                    },
                    modifier = Modifier.testTag("admin_add_category_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addProduct)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> OwnerOverviewTab(
                    strings = strings,
                    storeConfig = storeConfig,
                    products = products,
                    orders = orders,
                    reservations = reservations,
                    onAddProduct = openNewProductEditor,
                    onInitDefaults = onInitDefaultCategories,
                    onNavigateTab = { selectedTab = it }
                )
                1 -> AdminProductsTab(
                    strings = strings,
                    products = products,
                    onEdit = {
                        editingProduct = it
                        showProductEditor = true
                    },
                    onQuickToggle = onQuickToggleProduct,
                    onDelete = { onDeleteProduct(it.id) }
                )
                2 -> AdminCategoriesTab(
                    strings = strings,
                    categories = categories,
                    onInitDefaults = onInitDefaultCategories,
                    onEdit = {
                        editingCategory = it
                        showCategoryEditor = true
                    },
                    onDelete = { onDeleteCategory(it.id) }
                )
                3 -> AdminOrdersTab(
                    strings = strings,
                    orders = orders,
                    onUpdateStatus = onUpdateOrderStatus
                )
                4 -> AdminReservationsTab(
                    strings = strings,
                    reservations = reservations,
                    onUpdateStatus = onUpdateReservationStatus
                )
                5 -> AdminStoreConfigTab(
                    strings = strings,
                    storeConfig = storeConfig,
                    onSave = onSaveStoreConfig
                )
                6 -> OwnerAccessTab(
                    strings = strings,
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    adminRoles = adminRoles,
                    registeredUsers = registeredUsers,
                    onGrantAdmin = onGrantAdminAccess,
                    onRevokeAdmin = onRevokeAdminAccess
                )
            }
        }
    }

    if (showProductEditor && editingProduct != null) {
        ProductEditorSheet(
            strings = strings,
            initial = editingProduct!!,
            categories = availableCategories,
            onDismiss = { showProductEditor = false },
            onSave = { saved ->
                onSaveProduct(saved)
                showProductEditor = false
            }
        )
    }

    if (showCategoryEditor && editingCategory != null) {
        CategoryEditorSheet(
            strings = strings,
            initial = editingCategory!!,
            onDismiss = { showCategoryEditor = false },
            onSave = { saved ->
                onSaveCategory(saved)
                showCategoryEditor = false
            }
        )
    }
}

@Composable
private fun OwnerOverviewTab(
    strings: BazarStrings,
    storeConfig: StoreConfig,
    products: List<Product>,
    orders: List<Order>,
    reservations: List<Reservation>,
    onAddProduct: () -> Unit,
    onInitDefaults: () -> Unit,
    onNavigateTab: (Int) -> Unit
) {
    val pendingOrders = orders.count { it.status == "pending" }
    val pendingReservations = reservations.count { it.status == "pending" }
    val totalRevenue = orders
        .filter { it.status != "cancelled" }
        .sumOf { it.totalDzd }
    val contactReady = storeConfig.phoneNumber.isNotBlank() && storeConfig.whatsappNumber.isNotBlank()

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Store Contact Readiness Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateTab(5) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (contactReady) EmeraldSuccessContainer else MaterialTheme.colorScheme.errorContainer
                ),
                border = BorderStroke(
                    1.dp,
                    if (contactReady) EmeraldSuccess.copy(alpha = 0.4f) else CrimsonSale.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (contactReady) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = if (contactReady) EmeraldSuccess else CrimsonSale
                    )
                    Text(
                        text = if (contactReady) strings.storeContactReadyLabel else strings.storeContactMissingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (contactReady) EmeraldSuccess else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // KPI Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OwnerKpiCard(
                        title = strings.kpiTotalProducts,
                        value = "${products.size}",
                        icon = Icons.Filled.Checkroom,
                        onClick = { onNavigateTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerKpiCard(
                        title = strings.kpiPendingOrders,
                        value = "$pendingOrders / ${orders.size}",
                        icon = Icons.Filled.ShoppingBag,
                        onClick = { onNavigateTab(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OwnerKpiCard(
                        title = strings.kpiRevenueDzd,
                        value = formatDzd(totalRevenue, strings.dzdCurrency),
                        icon = Icons.Filled.Storefront,
                        onClick = { onNavigateTab(3) },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerKpiCard(
                        title = strings.kpiPendingReservations,
                        value = "$pendingReservations / ${reservations.size}",
                        icon = Icons.Filled.CalendarMonth,
                        onClick = { onNavigateTab(4) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions Section
        item {
            Text(
                text = strings.quickActionsLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAddProduct,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("overview_add_product_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.addProduct, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateTab(5) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.adminStoreInfoTab, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(6) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.adminAccessTab, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                OutlinedButton(
                    onClick = onInitDefaults,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.initCategoriesLabel)
                }
            }
        }
    }
}

@Composable
private fun OwnerKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminProductsTab(
    strings: BazarStrings,
    products: List<Product>,
    onEdit: (Product) -> Unit,
    onQuickToggle: (String, Boolean, Boolean) -> Unit,
    onDelete: (Product) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (products.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noProductsSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(products, key = { it.id }) { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BazarProductImage(
                                imageUrl = prod.imageUrl,
                                contentDescription = prod.nameAr,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${prod.nameAr} / ${prod.nameFr}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatDzd(prod.effectivePriceDzd, strings.dzdCurrency) +
                                        " • Qty: ${prod.stockQuantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onEdit(prod) }) {
                                Icon(Icons.Filled.Edit, contentDescription = strings.editProduct)
                            }
                            IconButton(onClick = { onDelete(prod) }) {
                                Icon(Icons.Filled.Delete, contentDescription = strings.deleteLabel, tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (prod.active) strings.publishedLabel else strings.hiddenLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (prod.active) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Switch(
                                    checked = prod.active,
                                    onCheckedChange = { newActive ->
                                        onQuickToggle(prod.id, newActive, prod.inStock)
                                    }
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (prod.inStock) strings.inStockLabel else strings.outOfStockLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (prod.inStock) EmeraldSuccess else CrimsonSale
                                )
                                Switch(
                                    checked = prod.inStock,
                                    onCheckedChange = { newInStock ->
                                        onQuickToggle(prod.id, prod.active, newInStock)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriesTab(
    strings: BazarStrings,
    categories: List<Category>,
    onInitDefaults: () -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onInitDefaults,
                modifier = Modifier.fillMaxWidth().testTag("admin_init_categories_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.initCategoriesLabel, fontWeight = FontWeight.Bold)
            }
        }

        items(categories, key = { it.id }) { cat ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cat.nameAr} • ${cat.nameFr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Order: ${cat.sortOrder} • ${if (cat.active) "Active" else "Hidden"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onEdit(cat) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(cat) }) {
                        Icon(Icons.Filled.Delete, contentDescription = strings.deleteLabel, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOrdersTab(
    strings: BazarStrings,
    orders: List<Order>,
    onUpdateStatus: (String, String) -> Unit
) {
    val statuses = listOf("pending", "confirmed", "shipped", "delivered", "cancelled")
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (orders.isEmpty()) {
            item { Text(strings.noOrdersYet, style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(orders, key = { it.id }) { order ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(order.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(formatDzd(order.totalDzd, strings.dzdCurrency), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("${order.phoneNumber} • ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                        if (order.items.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = strings.orderItemsLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    order.items.forEach { itemMap ->
                                        val title = if (strings.lang == AppLanguage.AR) {
                                            itemMap["nameAr"]?.toString().orEmpty()
                                        } else {
                                            itemMap["nameFr"]?.toString().orEmpty()
                                        }
                                        val qty = itemMap["quantity"]?.toString() ?: "1"
                                        val size = itemMap["size"]?.toString().orEmpty()
                                        val color = itemMap["color"]?.toString().orEmpty()
                                        val details = listOfNotNull(
                                            size.takeIf { it.isNotBlank() },
                                            color.takeIf { it.isNotBlank() }
                                        ).joinToString(" / ")
                                        Text(
                                            text = "• $title × $qty" + if (details.isNotBlank()) " ($details)" else "",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        if (order.notes.isNotBlank()) {
                            Text(order.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            statuses.forEach { st ->
                                FilterChip(
                                    selected = order.status == st,
                                    onClick = { onUpdateStatus(order.id, st) },
                                    label = { Text(strings.statusLabel(st)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminReservationsTab(
    strings: BazarStrings,
    reservations: List<Reservation>,
    onUpdateStatus: (String, String) -> Unit
) {
    val statuses = listOf("pending", "confirmed", "completed", "cancelled")
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (reservations.isEmpty()) {
            item { Text(strings.noReservationsYet, style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(reservations, key = { it.id }) { res ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${res.customerName} (${res.visitorCount})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = res.channel,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text("${res.phoneNumber} • ${res.preferredDate} ${res.preferredTime}", style = MaterialTheme.typography.bodyMedium)
                        if (res.notes.isNotBlank()) {
                            Text(res.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            statuses.forEach { st ->
                                FilterChip(
                                    selected = res.status == st,
                                    onClick = { onUpdateStatus(res.id, st) },
                                    label = { Text(strings.statusLabel(st)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStoreConfigTab(
    strings: BazarStrings,
    storeConfig: StoreConfig,
    onSave: (StoreConfig) -> Unit
) {
    var phone by remember(storeConfig) { mutableStateOf(storeConfig.phoneNumber) }
    var whatsapp by remember(storeConfig) { mutableStateOf(storeConfig.whatsappNumber) }
    var facebook by remember(storeConfig) { mutableStateOf(storeConfig.facebookUrl) }
    var instagram by remember(storeConfig) { mutableStateOf(storeConfig.instagramUrl) }
    var address by remember(storeConfig) { mutableStateOf(storeConfig.address) }
    var bannerTitleAr by remember(storeConfig) { mutableStateOf(storeConfig.bannerTitleAr) }
    var bannerTitleFr by remember(storeConfig) { mutableStateOf(storeConfig.bannerTitleFr) }
    var bannerSubtitleAr by remember(storeConfig) { mutableStateOf(storeConfig.bannerSubtitleAr) }
    var bannerSubtitleFr by remember(storeConfig) { mutableStateOf(storeConfig.bannerSubtitleFr) }
    var bannerActive by remember(storeConfig) { mutableStateOf(storeConfig.bannerActive) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("${strings.phoneLabel} (Official)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().testTag("admin_phone_input")
        )
        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("WhatsApp (+213...)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_input")
        )
        OutlinedTextField(
            value = facebook,
            onValueChange = { facebook = it },
            label = { Text("Facebook URL") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = instagram,
            onValueChange = { instagram = it },
            label = { Text("Instagram URL") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bannerTitleAr,
            onValueChange = { bannerTitleAr = it },
            label = { Text("Banner Title (AR)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bannerTitleFr,
            onValueChange = { bannerTitleFr = it },
            label = { Text("Banner Title (FR)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bannerSubtitleAr,
            onValueChange = { bannerSubtitleAr = it },
            label = { Text("Banner Subtitle (AR)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bannerSubtitleFr,
            onValueChange = { bannerSubtitleFr = it },
            label = { Text("Banner Subtitle (FR)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Promotional Banner Active", style = MaterialTheme.typography.titleMedium)
            Switch(checked = bannerActive, onCheckedChange = { bannerActive = it })
        }

        Button(
            onClick = {
                onSave(
                    storeConfig.copy(
                        phoneNumber = phone,
                        whatsappNumber = whatsapp,
                        facebookUrl = facebook,
                        instagramUrl = instagram,
                        address = address,
                        bannerTitleAr = bannerTitleAr,
                        bannerTitleFr = bannerTitleFr,
                        bannerSubtitleAr = bannerSubtitleAr,
                        bannerSubtitleFr = bannerSubtitleFr,
                        bannerActive = bannerActive
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("admin_save_store_config_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(strings.saveLabel, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OwnerAccessTab(
    strings: BazarStrings,
    currentUserId: String,
    currentUserEmail: String,
    adminRoles: List<AdminRole>,
    registeredUsers: List<UserProfile>,
    onGrantAdmin: (uid: String, email: String) -> Unit,
    onRevokeAdmin: (uid: String) -> Unit
) {
    var manualUid by remember { mutableStateOf("") }
    var manualEmail by remember { mutableStateOf("") }
    val adminUidSet = remember(adminRoles) { adminRoles.map { it.uid.ifBlank { it.id } }.toSet() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary Owner Info Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ZaatarEmerald),
                border = BorderStroke(1.dp, ChampagneGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.primaryOwnerLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = BazarZaatarRepository.BOOTSTRAPPED_ADMIN_EMAIL,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ChampagneGold
                        )
                        Text(
                            text = "Session: ${currentUserEmail.ifBlank { currentUserId }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Manual Grant Admin Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.grantAdminLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = manualUid,
                        onValueChange = { manualUid = it },
                        label = { Text("User UID *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_grant_uid_input")
                    )
                    OutlinedTextField(
                        value = manualEmail,
                        onValueChange = { manualEmail = it },
                        label = { Text("Email (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onGrantAdmin(manualUid.trim(), manualEmail.trim())
                            manualUid = ""
                            manualEmail = ""
                        },
                        enabled = manualUid.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_grant_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.grantAdminLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Co-Admins List
        item {
            Text(
                text = "${strings.coAdminsLabel} (${adminRoles.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (adminRoles.isNotEmpty()) {
            items(adminRoles, key = { it.id }) { role ->
                val roleUid = role.uid.ifBlank { role.id }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = role.email.ifBlank { roleUid },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UID: $roleUid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (roleUid != currentUserId) {
                            OutlinedButton(
                                onClick = { onRevokeAdmin(roleUid) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(strings.revokeAdminLabel)
                            }
                        }
                    }
                }
            }
        }

        // Registered Clients List (1-tap promote to co-admin)
        item {
            Text(
                text = "${strings.registeredClientsLabel} (${registeredUsers.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(registeredUsers, key = { it.id }) { user ->
            val uid = user.userId.ifBlank { user.id }
            val alreadyCoAdmin = uid in adminUidSet ||
                user.email.equals(BazarZaatarRepository.BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName.ifBlank { "Client" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.email.isNotBlank()) {
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (user.phoneNumber.isNotBlank()) {
                            Text(
                                text = user.phoneNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (!alreadyCoAdmin && uid.isNotBlank()) {
                        OutlinedButton(onClick = { onGrantAdmin(uid, user.email) }) {
                            Text(strings.grantAdminLabel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditorSheet(
    strings: BazarStrings,
    initial: Product,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    var nameAr by remember { mutableStateOf(initial.nameAr) }
    var nameFr by remember { mutableStateOf(initial.nameFr) }
    var descAr by remember { mutableStateOf(initial.descriptionAr) }
    var descFr by remember { mutableStateOf(initial.descriptionFr) }
    var categoryId by remember { mutableStateOf(initial.categoryId.ifBlank { categories.firstOrNull()?.id.orEmpty() }) }
    var priceText by remember { mutableStateOf(if (initial.priceDzd > 0) initial.priceDzd.toInt().toString() else "") }
    var discountText by remember { mutableStateOf(initial.discountPriceDzd?.takeIf { it > 0 }?.toInt()?.toString().orEmpty()) }
    var sizesText by remember { mutableStateOf(initial.sizes.joinToString(", ")) }
    var colorsText by remember { mutableStateOf(initial.colors.joinToString(", ")) }
    var stockText by remember { mutableStateOf(initial.stockQuantity.toString()) }
    var imageUrl by remember { mutableStateOf(initial.imageUrl) }
    var featured by remember { mutableStateOf(initial.featured) }
    var newArrival by remember { mutableStateOf(initial.newArrival) }
    var specialOffer by remember { mutableStateOf(initial.specialOffer) }
    var active by remember { mutableStateOf(initial.active) }
    var catMenuExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            encodeImageUriToBase64DataUri(context, uri)?.let { encoded ->
                imageUrl = encoded
            }
        }
    }

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val parsedDiscount = discountText.toDoubleOrNull()?.takeIf { it > 0.0 && it < parsedPrice }
    val valid = nameAr.isNotBlank() && nameFr.isNotBlank() && parsedPrice > 0.0 && categoryId.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (initial.id.isBlank()) strings.addProduct else strings.editProduct,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BazarProductImage(
                    imageUrl = imageUrl,
                    contentDescription = "Preview",
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                )
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.pickPhotoLabel)
                }
            }

            OutlinedTextField(
                value = if (imageUrl.startsWith("data:image/")) "" else imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image HTTPS URL (optional if photo picked)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nameAr,
                onValueChange = { nameAr = it },
                label = { Text("Product Name (Arabic) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_product_name_ar")
            )
            OutlinedTextField(
                value = nameFr,
                onValueChange = { nameFr = it },
                label = { Text("Nom du produit (Français) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_product_name_fr")
            )

            ExposedDropdownMenuBox(
                expanded = catMenuExpanded,
                onExpandedChange = { catMenuExpanded = !catMenuExpanded }
            ) {
                val selectedCat = categories.firstOrNull { it.id == categoryId }
                OutlinedTextField(
                    value = selectedCat?.let { if (strings.lang == AppLanguage.AR) it.nameAr else it.nameFr } ?: categoryId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(strings.categoriesTitle) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = catMenuExpanded,
                    onDismissRequest = { catMenuExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.nameAr} / ${cat.nameFr}") },
                            onClick = {
                                categoryId = cat.id
                                catMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price DZD *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("admin_product_price")
                )
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount DZD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = stockText,
                onValueChange = { stockText = it },
                label = { Text("Stock Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sizesText,
                onValueChange = { sizesText = it },
                label = { Text("Sizes (comma separated, e.g. S, M, L, XL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = colorsText,
                onValueChange = { colorsText = it },
                label = { Text("Colors (comma separated)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descAr,
                onValueChange = { descAr = it },
                label = { Text("Description (Arabic)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descFr,
                onValueChange = { descFr = it },
                label = { Text("Description (Français)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.featuredProducts)
                Switch(checked = featured, onCheckedChange = { featured = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.latestArrivals)
                Switch(checked = newArrival, onCheckedChange = { newArrival = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.specialOffers)
                Switch(checked = specialOffer, onCheckedChange = { specialOffer = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Published (Active)")
                Switch(checked = active, onCheckedChange = { active = it })
            }

            Button(
                onClick = {
                    val stockQty = (stockText.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                    onSave(
                        initial.copy(
                            nameAr = nameAr,
                            nameFr = nameFr,
                            descriptionAr = descAr,
                            descriptionFr = descFr,
                            categoryId = categoryId,
                            priceDzd = parsedPrice,
                            discountPriceDzd = parsedDiscount,
                            sizes = sizesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(20),
                            colors = colorsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(20),
                            stockQuantity = stockQty,
                            inStock = stockQty > 0L,
                            featured = featured,
                            newArrival = newArrival,
                            specialOffer = specialOffer,
                            active = active,
                            imageUrl = imageUrl
                        )
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_save_product_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.saveLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorSheet(
    strings: BazarStrings,
    initial: Category,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var nameAr by remember { mutableStateOf(initial.nameAr) }
    var nameFr by remember { mutableStateOf(initial.nameFr) }
    var slug by remember { mutableStateOf(initial.slug) }
    var sortOrderText by remember { mutableStateOf(initial.sortOrder.toString()) }
    var active by remember { mutableStateOf(initial.active) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(strings.categoriesTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = nameAr, onValueChange = { nameAr = it }, label = { Text("Category Name (AR)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nameFr, onValueChange = { nameFr = it }, label = { Text("Nom catégorie (FR)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = slug, onValueChange = { slug = it }, label = { Text("Slug") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = sortOrderText,
                onValueChange = { sortOrderText = it },
                label = { Text("Sort Order") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Active")
                Switch(checked = active, onCheckedChange = { active = it })
            }
            Button(
                onClick = {
                    val safeSlug = slug.ifBlank { nameFr.lowercase().replace(Regex("[^a-z0-9]+"), "-") }
                    onSave(
                        initial.copy(
                            nameAr = nameAr,
                            nameFr = nameFr,
                            slug = safeSlug,
                            sortOrder = (sortOrderText.toLongOrNull() ?: 0L).coerceIn(0L, 1000L),
                            active = active
                        )
                    )
                },
                enabled = nameAr.isNotBlank() && nameFr.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.saveLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}
