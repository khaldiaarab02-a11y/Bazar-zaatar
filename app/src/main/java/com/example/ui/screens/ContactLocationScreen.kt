package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.StoreConfig
import com.example.ui.components.ReserveNowBannerButton
import com.example.ui.i18n.BazarStrings
import com.example.ui.theme.ChampagneGold

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ContactLocationScreen(
    strings: BazarStrings,
    storeConfig: StoreConfig,
    onBack: () -> Unit,
    onReserveClick: () -> Unit,
    onNotify: (String) -> Unit
) {
    val context = LocalContext.current
    val lat = storeConfig.latitude
    val lng = storeConfig.longitude
    val mapsUrl = storeConfig.mapsUrl.ifBlank { "https://www.google.com/maps?q=$lat,$lng" }
    val phone = storeConfig.phoneNumber.trim()
    val whatsapp = storeConfig.whatsappNumber.trim()
    val whatsappDigits = whatsapp.filter { it.isDigit() }
    val facebook = storeConfig.facebookUrl.trim()
    val instagram = storeConfig.instagramUrl.trim()

    val mapHtml = remember(lat, lng, storeConfig.businessName) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes" />
          <style>
            html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #F2ECE1; }
          </style>
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        </head>
        <body>
          <div id="map"></div>
          <script>
            try {
              var map = L.map('map').setView([$lat, $lng], 16);
              L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '© OpenStreetMap'
              }).addTo(map);
              L.marker([$lat, $lng]).addTo(map)
                .bindPopup('<b>Bazar Zaatar – بازار زعتر</b><br/>Volani Lavinir, Tiaret 14000')
                .openPopup();
            } catch (e) {}
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.cancelLabel,
                        tint = Color.White
                    )
                }
                Text(
                    text = strings.contactLocationTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Map Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = WebViewClient()
                                        loadDataWithBaseURL(
                                            "https://unpkg.com",
                                            mapHtml,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${storeConfig.businessName} – ${storeConfig.businessNameAr}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = storeConfig.address,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "GPS: $lat, $lng",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("get_directions_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Directions, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(strings.getDirections, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Direct Contact Buttons (Using real admin-configured phone & WhatsApp only)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (phone.isNotBlank()) {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                    }
                                } else {
                                    onNotify(strings.contactNotConfigured)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("call_now_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Phone, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (phone.isNotBlank()) "${strings.callNow} ($phone)" else strings.callNow,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (whatsappDigits.isNotBlank()) {
                                    openWhatsApp(context, whatsappDigits, "Bazar Zaatar")
                                } else {
                                    onNotify(strings.contactNotConfigured)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("whatsapp_contact_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F7A4D),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (whatsapp.isNotBlank()) "${strings.whatsappContact} ($whatsapp)" else strings.whatsappContact,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (phone.isBlank() && whatsapp.isBlank()) {
                            Text(
                                text = strings.contactNotConfigured,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (facebook.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebook)))
                                        }
                                    } else {
                                        onNotify(strings.contactNotConfigured)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("facebook_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.facebookPage, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (instagram.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instagram)))
                                        }
                                    } else {
                                        onNotify(strings.contactNotConfigured)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("instagram_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.instagramPage, maxLines = 1)
                            }
                        }
                    }
                }
            }

            item {
                ReserveNowBannerButton(
                    label = strings.reserveNowButton,
                    onClick = onReserveClick
                )
            }
        }
    }
}
