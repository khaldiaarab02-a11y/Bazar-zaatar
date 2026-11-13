package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.ui.components.CategoryPillCard
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.ProductCard
import com.example.ui.components.ReserveNowBannerButton
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BazarStrings
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ZaatarEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    strings: BazarStrings,
    storeConfig: StoreConfig,
    categories: List<Category>,
    products: List<Product>,
    userDisplayName: String,
    userEmail: String,
    isAdmin: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onShopNowClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAddProduct: (Product) -> Unit,
    onReserveClick: () -> Unit,
    onContactClick: () -> Unit,
    onOpenAccountClick: () -> Unit,
    onOpenAdminClick: () -> Unit,
    onSwitchAccountClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val featured = products.filter { it.featured }
    val latestArrivals = products.filter { it.newArrival }
    val specialOffers = products.filter { it.specialOffer || it.hasDiscount }
    var showAccountSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Brand Header with Logo & Welcome Message
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ZaatarEmerald,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bazar_zaatar_icon_1794513328114),
                                contentDescription = strings.appTitle,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column {
                            Text(
                                text = "BAZAR ZAATAR • بازار زعتر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.brandSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAdmin) {
                            Surface(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .clickable(onClick = onOpenAdminClick)
                                    .testTag("home_header_admin_button"),
                                color = ZaatarEmerald,
                                shape = CircleShape,
                                border = BorderStroke(1.5.dp, ChampagneGold)
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = strings.ownerControlPanelTitle,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Optimized Login / Account Access Icon (Clients & Account Holders)
                        val avatarInitial = userDisplayName.trim().firstOrNull()?.uppercaseChar()
                            ?: userEmail.trim().firstOrNull()?.uppercaseChar()
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .clickable { showAccountSheet = true }
                                .testTag("home_login_account_icon"),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (isAdmin) ChampagneGold else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarInitial != null && avatarInitial.isLetterOrDigit()) {
                                    Text(
                                        text = avatarInitial.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = strings.accountMenuTitle,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        LanguageTogglePill(
                            currentLanguage = strings.lang,
                            onToggle = onLanguageChange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = strings.welcomeMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Large Promotional Banner
        if (storeConfig.bannerActive) {
            item {
                val bannerTitle = if (strings.lang == AppLanguage.AR) {
                    storeConfig.bannerTitleAr.ifBlank { "مرحباً بكم في بازار زعتر" }
                } else {
                    storeConfig.bannerTitleFr.ifBlank { "Bienvenue chez Bazar Zaatar" }
                }
                val bannerSub = if (strings.lang == AppLanguage.AR) {
                    storeConfig.bannerSubtitleAr.ifBlank { "أناقة الرجال وأزياء الأطفال في قلب تيارت" }
                } else {
                    storeConfig.bannerSubtitleFr.ifBlank { "Mode Homme & Enfants au cœur de Tiaret" }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(230.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1794514160660),
                            contentDescription = bannerTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            ZaatarEmerald.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Surface(
                                color = ChampagneGold,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "TIARET • 14000",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191C1A),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = bannerTitle,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bannerSub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onShopNowClick,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampagneGold,
                                    contentColor = Color(0xFF191C1A)
                                ),
                                modifier = Modifier.testTag("shop_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.shopNow,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prominent Reservation Button on Homepage
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ReserveNowBannerButton(
                    label = strings.reserveNowButton,
                    onClick = onReserveClick
                )
            }
        }

        // Product Categories Horizontal Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.categoriesTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onShopNowClick) {
                        Text(strings.seeAll)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryPillCard(
                            category = category,
                            strings = strings,
                            onClick = { onCategoryClick(category) }
                        )
                    }
                }
            }
        }

        // Boutique Editorial Showcase (Men's Fashion & Kids Clothing Lookbook Cards)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditorialCollectionTile(
                    title = if (strings.lang == AppLanguage.AR) "تشكيلة الرجال" else "Collection Homme",
                    imageRes = R.drawable.img_mens_editorial_1794514160127,
                    onClick = {
                        categories.firstOrNull { it.id == "mens_clothing" }?.let(onCategoryClick) ?: onShopNowClick()
                    },
                    modifier = Modifier.weight(1f)
                )
                EditorialCollectionTile(
                    title = if (strings.lang == AppLanguage.AR) "تشكيلة الأطفال" else "Collection Enfants",
                    imageRes = R.drawable.img_kids_editorial_1794514161487,
                    onClick = {
                        categories.firstOrNull { it.id == "kids_clothing" }?.let(onCategoryClick) ?: onShopNowClick()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Dynamic Product Sections (Shown from live Firestore catalog — never fabricated!)
        if (products.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_catalog_1794514162114),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(14.dp))
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
                            Button(
                                onClick = onOpenAdminClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("home_open_admin_button")
                            ) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(strings.openAdminToAdd)
                            }
                        }
                    }
                }
            }
        } else {
            if (featured.isNotEmpty()) {
                item {
                    HorizontalProductSection(
                        title = strings.featuredProducts,
                        products = featured,
                        strings = strings,
                        onSeeAll = onShopNowClick,
                        onProductClick = onProductClick,
                        onQuickAdd = onQuickAddProduct
                    )
                }
            }

            if (latestArrivals.isNotEmpty()) {
                item {
                    HorizontalProductSection(
                        title = strings.latestArrivals,
                        products = latestArrivals,
                        strings = strings,
                        onSeeAll = onShopNowClick,
                        onProductClick = onProductClick,
                        onQuickAdd = onQuickAddProduct
                    )
                }
            }

            if (specialOffers.isNotEmpty()) {
                item {
                    HorizontalProductSection(
                        title = strings.specialOffers,
                        products = specialOffers,
                        strings = strings,
                        onSeeAll = onShopNowClick,
                        onProductClick = onProductClick,
                        onQuickAdd = onQuickAddProduct
                    )
                }
            }

            if (featured.isEmpty() && latestArrivals.isEmpty() && specialOffers.isEmpty()) {
                item {
                    HorizontalProductSection(
                        title = strings.allProducts,
                        products = products.take(10),
                        strings = strings,
                        onSeeAll = onShopNowClick,
                        onProductClick = onProductClick,
                        onQuickAdd = onQuickAddProduct
                    )
                }
            }
        }

        // Visit & Location Teaser Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable(onClick = onContactClick)
                    .testTag("home_location_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = ChampagneGold
                                )
                            }
                        }
                        Column {
                            Text(
                                text = strings.contactLocationTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = storeConfig.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    OutlinedButton(onClick = onContactClick, shape = RoundedCornerShape(12.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = strings.contactLocationTitle,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ZaatarEmerald,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                                contentDescription = null,
                                tint = ChampagneGold
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userDisplayName.ifBlank { strings.accountMenuTitle },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (userEmail.isNotBlank()) {
                            Text(
                                text = "${strings.signedInAsLabel}: $userEmail",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (isAdmin) {
                        Surface(
                            color = ChampagneGold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = strings.ownerBadgeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (isAdmin) {
                    Button(
                        onClick = {
                            showAccountSheet = false
                            onOpenAdminClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("sheet_open_owner_dashboard_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZaatarEmerald,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = ChampagneGold)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.ownerControlPanelTitle, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        showAccountSheet = false
                        onOpenAccountClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sheet_open_client_account_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.myAccountOrdersLabel, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        showAccountSheet = false
                        onSwitchAccountClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sheet_switch_account_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.switchAccountLabel, fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = {
                        showAccountSheet = false
                        onSignOutClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sheet_sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(strings.signOutLabel, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun EditorialCollectionTile(
    title: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(145.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HorizontalProductSection(
    title: String,
    products: List<Product>,
    strings: BazarStrings,
    onSeeAll: () -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text(strings.seeAll)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    strings = strings,
                    onClick = { onProductClick(product) },
                    onQuickAdd = { onQuickAdd(product) },
                    modifier = Modifier.width(185.dp)
                )
            }
        }
    }
}
