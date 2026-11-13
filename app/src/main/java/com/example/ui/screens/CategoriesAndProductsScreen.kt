package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.BazarProductImage
import com.example.ui.components.CategoryPillCard
import com.example.ui.components.LuxuryGradientHeader
import com.example.ui.components.ProductCard
import com.example.ui.components.ReserveNowBannerButton
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatDzd
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BazarStrings
import com.example.ui.theme.CrimsonSale
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer

@Composable
fun CategoriesScreen(
    strings: BazarStrings,
    categories: List<Category>,
    products: List<Product>,
    onCategorySelected: (Category) -> Unit,
    onReserveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LuxuryGradientHeader(
            title = strings.categoriesTitle,
            subtitle = strings.brandSubtitle
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories, key = { it.id }) { category ->
                val count = products.count { it.categoryId == category.id }
                val title = if (strings.lang == AppLanguage.AR) category.nameAr else category.nameFr
                val secondaryTitle = if (strings.lang == AppLanguage.AR) category.nameFr else category.nameAr

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCategorySelected(category) }
                        .testTag("category_grid_card_${category.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = categoryIcon(category.iconKey),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = secondaryTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            ReserveNowBannerButton(label = strings.reserveNowButton, onClick = onReserveClick)
        }
    }
}

@Composable
fun ProductsScreen(
    strings: BazarStrings,
    categories: List<Category>,
    products: List<Product>,
    selectedCategoryId: String?,
    onSelectCategoryId: (String?) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAddProduct: (Product) -> Unit,
    isAdmin: Boolean,
    onOpenAdminClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var quickFilter by remember { mutableStateOf("all") } // all, featured, new, offers

    val filteredProducts = remember(products, selectedCategoryId, searchQuery, quickFilter) {
        products.filter { prod ->
            val matchesCategory = selectedCategoryId == null || prod.categoryId == selectedCategoryId ||
                (selectedCategoryId == "special_offers" && (prod.specialOffer || prod.hasDiscount)) ||
                (selectedCategoryId == "kids_new_arrivals" && prod.newArrival)
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                prod.nameAr.lowercase().contains(q) ||
                prod.nameFr.lowercase().contains(q) ||
                prod.descriptionAr.lowercase().contains(q) ||
                prod.descriptionFr.lowercase().contains(q)
            val matchesQuick = when (quickFilter) {
                "featured" -> prod.featured
                "new" -> prod.newArrival
                "offers" -> prod.specialOffer || prod.hasDiscount
                else -> true
            }
            matchesCategory && matchesQuery && matchesQuick
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LuxuryGradientHeader(
            title = strings.allProducts,
            subtitle = "${filteredProducts.size}",
            trailing = if (isAdmin) {
                {
                    IconButton(
                        onClick = onOpenAdminClick,
                        modifier = Modifier.testTag("products_open_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = strings.ownerControlPanelTitle,
                            tint = Color.White
                        )
                    }
                }
            } else null
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchProductsHint) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("products_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = quickFilter == "all" && selectedCategoryId == null,
                        onClick = {
                            quickFilter = "all"
                            onSelectCategoryId(null)
                        },
                        label = { Text(strings.filterAll) }
                    )
                }
                item {
                    FilterChip(
                        selected = quickFilter == "featured",
                        onClick = { quickFilter = if (quickFilter == "featured") "all" else "featured" },
                        label = { Text(strings.featuredProducts) }
                    )
                }
                item {
                    FilterChip(
                        selected = quickFilter == "new",
                        onClick = { quickFilter = if (quickFilter == "new") "all" else "new" },
                        label = { Text(strings.latestArrivals) }
                    )
                }
                item {
                    FilterChip(
                        selected = quickFilter == "offers",
                        onClick = { quickFilter = if (quickFilter == "offers") "all" else "offers" },
                        label = { Text(strings.specialOffers) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { cat ->
                    CategoryPillCard(
                        category = cat,
                        strings = strings,
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            onSelectCategoryId(if (selectedCategoryId == cat.id) null else cat.id)
                        }
                    )
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_catalog_1794514162114),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.noProductsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.noProductsSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onOpenAdminClick, shape = RoundedCornerShape(12.dp)) {
                            Text(strings.openAdminToAdd)
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        strings = strings,
                        onClick = { onProductClick(product) },
                        onQuickAdd = { onQuickAddProduct(product) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailModal(
    product: Product,
    strings: BazarStrings,
    onDismiss: () -> Unit,
    onAddToCart: (size: String, color: String, qty: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSize by remember(product.id) { mutableStateOf(product.sizes.firstOrNull().orEmpty()) }
    var selectedColor by remember(product.id) { mutableStateOf(product.colors.firstOrNull().orEmpty()) }
    var quantity by remember(product.id) { mutableLongStateOf(1L) }

    val primaryName = if (strings.lang == AppLanguage.AR) product.nameAr else product.nameFr
    val secondaryName = if (strings.lang == AppLanguage.AR) product.nameFr else product.nameAr

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.productDetails,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.cancelLabel)
                }
            }

            BazarProductImage(
                imageUrl = product.imageUrl,
                contentDescription = primaryName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(20.dp))
            )

            Text(
                text = primaryName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (secondaryName.isNotBlank() && secondaryName != primaryName) {
                Text(
                    text = secondaryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Price & Stock Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = formatDzd(product.effectivePriceDzd, strings.dzdCurrency),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.hasDiscount) {
                        Text(
                            text = formatDzd(product.priceDzd, strings.dzdCurrency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (product.inStock && product.stockQuantity > 0) EmeraldSuccessContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (product.inStock && product.stockQuantity > 0) {
                            "${strings.inStockLabel} (${product.stockQuantity})"
                        } else {
                            strings.outOfStockLabel
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (product.inStock && product.stockQuantity > 0) EmeraldSuccess else CrimsonSale,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Bilingual Descriptions (Arabic & French always shown clearly!)
            if (product.descriptionAr.isNotBlank() || product.descriptionFr.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.descriptionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.descriptionAr.isNotBlank()) {
                            Text(
                                text = product.descriptionAr,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        if (product.descriptionFr.isNotBlank()) {
                            Text(
                                text = product.descriptionFr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Available Sizes
            if (product.sizes.isNotEmpty()) {
                Text(
                    text = strings.availableSizes,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    product.sizes.forEach { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size },
                            label = { Text(size) }
                        )
                    }
                }
            }

            // Available Colors
            if (product.colors.isNotEmpty()) {
                Text(
                    text = strings.availableColors,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    product.colors.forEach { color ->
                        FilterChip(
                            selected = selectedColor == color,
                            onClick = { selectedColor = color },
                            label = { Text(color) }
                        )
                    }
                }
            }

            // Quantity & Add to Cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1L) quantity-- }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { if (quantity < 99L) quantity++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }
                }

                Button(
                    onClick = { onAddToCart(selectedSize, selectedColor, quantity) },
                    enabled = product.inStock && product.stockQuantity > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("detail_add_to_cart_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = strings.addToCart,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
