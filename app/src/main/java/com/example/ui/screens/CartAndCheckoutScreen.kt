package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.ui.components.BazarProductImage
import com.example.ui.components.LuxuryGradientHeader
import com.example.ui.components.formatDzd
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BazarStrings
import com.example.ui.theme.EmeraldSuccess

@Composable
fun CartScreen(
    strings: BazarStrings,
    cartItems: List<CartItem>,
    defaultName: String,
    defaultPhone: String,
    defaultAddress: String,
    isBusy: Boolean,
    onUpdateQuantity: (CartItem, Long) -> Unit,
    onRemoveItem: (String) -> Unit,
    onPlaceOrder: (name: String, phone: String, address: String, notes: String, onSuccess: (String) -> Unit) -> Unit,
    onBrowseProducts: () -> Unit
) {
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var confirmedOrderId by remember { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.unitPriceDzd * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LuxuryGradientHeader(
            title = strings.cartTitle,
            subtitle = "${cartItems.sumOf { it.quantity }}"
        )

        if (confirmedOrderId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = strings.orderConfirmedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "#${confirmedOrderId}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.orderConfirmedSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                confirmedOrderId = null
                                onBrowseProducts()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.shopNow)
                        }
                    }
                }
            }
        } else if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(84.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Text(
                        text = strings.emptyCartTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.emptyCartSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onBrowseProducts,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(strings.shopNow)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    val name = if (strings.lang == AppLanguage.AR) item.productNameAr else item.productNameFr
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BazarProductImage(
                                imageUrl = item.productImageUrl,
                                contentDescription = name,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val variants = listOfNotNull(
                                    item.selectedSize.takeIf { it.isNotBlank() },
                                    item.selectedColor.takeIf { it.isNotBlank() }
                                ).joinToString(" • ")
                                if (variants.isNotBlank()) {
                                    Text(
                                        text = variants,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatDzd(item.unitPriceDzd * item.quantity, strings.dzdCurrency),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(
                                    onClick = { onRemoveItem(item.id) },
                                    modifier = Modifier.testTag("remove_cart_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = strings.deleteLabel,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onUpdateQuantity(item, item.quantity - 1L) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { onUpdateQuantity(item, item.quantity + 1L) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Footer
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.subtotalLabel, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = formatDzd(subtotal, strings.dzdCurrency),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.totalLabel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatDzd(subtotal, strings.dzdCurrency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showCheckoutSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_checkout_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = strings.proceedCheckout,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showCheckoutSheet) {
        CheckoutModal(
            strings = strings,
            totalDzd = subtotal,
            initialName = defaultName,
            initialPhone = defaultPhone,
            initialAddress = defaultAddress,
            isBusy = isBusy,
            onDismiss = { showCheckoutSheet = false },
            onConfirm = { name, phone, address, notes ->
                onPlaceOrder(name, phone, address, notes) { orderId ->
                    showCheckoutSheet = false
                    confirmedOrderId = orderId
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutModal(
    strings: BazarStrings,
    totalDzd: Double,
    initialName: String,
    initialPhone: String,
    initialAddress: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    var customerName by remember { mutableStateOf(initialName) }
    var phoneNumber by remember { mutableStateOf(initialPhone) }
    var deliveryAddress by remember { mutableStateOf(initialAddress) }
    var notes by remember { mutableStateOf("") }

    val valid = customerName.trim().length >= 2 &&
        phoneNumber.trim().length >= 6 &&
        deliveryAddress.trim().length >= 4

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.checkoutTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text(strings.fullNameLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("checkout_name_input")
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(strings.phoneLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input")
            )
            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = { deliveryAddress = it },
                label = { Text(strings.deliveryAddressLabel) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("checkout_address_input")
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notesOptionalLabel) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.totalLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatDzd(totalDzd, strings.dzdCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = { onConfirm(customerName, phoneNumber, deliveryAddress, notes) },
                enabled = valid && !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_order_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.confirmOrder, fontWeight = FontWeight.Bold)
            }
        }
    }
}
