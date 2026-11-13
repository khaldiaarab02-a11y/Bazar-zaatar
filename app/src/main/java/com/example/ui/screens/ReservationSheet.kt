package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.StoreConfig
import com.example.ui.i18n.BazarStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationModal(
    strings: BazarStrings,
    storeConfig: StoreConfig,
    initialName: String,
    initialPhone: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmitReservation: (
        name: String,
        phone: String,
        date: String,
        time: String,
        visitors: Long,
        notes: String,
        channel: String,
        onSaved: () -> Unit
    ) -> Unit,
    onNotify: (String) -> Unit
) {
    val context = LocalContext.current
    val todayDefault = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    var customerName by remember { mutableStateOf(initialName) }
    var phoneNumber by remember { mutableStateOf(initialPhone) }
    var preferredDate by remember { mutableStateOf(todayDefault) }
    var preferredTime by remember { mutableStateOf("15:00") }
    var visitorCount by remember { mutableLongStateOf(1L) }
    var notes by remember { mutableStateOf("") }

    val valid = customerName.trim().length >= 2 &&
        phoneNumber.trim().length >= 6 &&
        preferredDate.trim().length >= 4 &&
        preferredTime.trim().length >= 2

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = strings.reservationTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = strings.reservationSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text(strings.fullNameLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reservation_name_input")
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(strings.phoneLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reservation_phone_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = preferredDate,
                    onValueChange = { preferredDate = it },
                    label = { Text(strings.preferredDateLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("reservation_date_input")
                )
                OutlinedTextField(
                    value = preferredTime,
                    onValueChange = { preferredTime = it },
                    label = { Text(strings.preferredTimeLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("reservation_time_input")
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.visitorCountLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (visitorCount > 1L) visitorCount-- }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease visitors")
                        }
                        Text(
                            text = "$visitorCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        IconButton(onClick = { if (visitorCount < 20L) visitorCount++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase visitors")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notesOptionalLabel) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val waNumber = storeConfig.whatsappNumber.filter { it.isDigit() }
                    onSubmitReservation(
                        customerName,
                        phoneNumber,
                        preferredDate,
                        preferredTime,
                        visitorCount,
                        notes,
                        if (waNumber.isNotBlank()) "whatsapp" else "in_app"
                    ) {
                        if (waNumber.isNotBlank()) {
                            val msg = buildString {
                                append("Bazar Zaatar – حجز زيارة / Réservation\n")
                                append("الاسم / Nom: $customerName\n")
                                append("الهاتف / Tél: $phoneNumber\n")
                                append("التاريخ / Date: $preferredDate\n")
                                append("الوقت / Heure: $preferredTime\n")
                                append("الزوار / Visiteurs: $visitorCount\n")
                                if (notes.isNotBlank()) append("ملاحظات / Notes: $notes")
                            }
                            openWhatsApp(context, waNumber, msg)
                            onNotify(strings.reservationSuccessMsg)
                        } else {
                            onNotify(strings.whatsappNotConfiguredMsg)
                        }
                        onDismiss()
                    }
                },
                enabled = valid && !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_reservation_whatsapp_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F7A4D),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.sendViaWhatsApp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    onSubmitReservation(
                        customerName,
                        phoneNumber,
                        preferredDate,
                        preferredTime,
                        visitorCount,
                        notes,
                        "in_app"
                    ) {
                        onNotify(strings.reservationSuccessMsg)
                        onDismiss()
                    }
                },
                enabled = valid && !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_reservation_inapp_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.saveReservationInApp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

fun openWhatsApp(context: Context, cleanDigits: String, message: String? = null) {
    val encoded = if (!message.isNullOrBlank()) "?text=${Uri.encode(message)}" else ""
    val uri = Uri.parse("https://wa.me/$cleanDigits$encoded")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching { context.startActivity(intent) }
}
