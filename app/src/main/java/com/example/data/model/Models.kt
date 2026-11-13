package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class StoreConfig(
    @DocumentId val id: String = "main",
    val businessName: String = "Bazar Zaatar",
    val businessNameAr: String = "بازار زعتر",
    val address: String = "Volani Lavinir, Tiaret, Algeria, 14000",
    val latitude: Double = 35.3609386,
    val longitude: Double = 1.3225709,
    val mapsUrl: String = "https://www.google.com/maps?q=35.3609386,1.3225709",
    val phoneNumber: String = "",
    val whatsappNumber: String = "",
    val facebookUrl: String = "",
    val instagramUrl: String = "",
    val bannerTitleAr: String = "مرحباً بكم في بازار زعتر",
    val bannerTitleFr: String = "Bienvenue chez Bazar Zaatar",
    val bannerSubtitleAr: String = "أناقة الرجال وأزياء الأطفال في قلب تيارت",
    val bannerSubtitleFr: String = "Mode Homme & Enfants au cœur de Tiaret",
    val bannerActive: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Category(
    @DocumentId val id: String = "",
    val nameAr: String = "",
    val nameFr: String = "",
    val slug: String = "",
    val sortOrder: Long = 0L,
    val active: Boolean = true,
    val iconKey: String = "clothing",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val nameAr: String = "",
    val nameFr: String = "",
    val descriptionAr: String = "",
    val descriptionFr: String = "",
    val categoryId: String = "",
    val priceDzd: Double = 0.0,
    val discountPriceDzd: Double? = null,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val stockQuantity: Long = 0L,
    val inStock: Boolean = true,
    val featured: Boolean = false,
    val newArrival: Boolean = false,
    val specialOffer: Boolean = false,
    val active: Boolean = true,
    val imageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectivePriceDzd: Double
        get() = if (discountPriceDzd != null && discountPriceDzd > 0.0 && discountPriceDzd < priceDzd) {
            discountPriceDzd
        } else {
            priceDzd
        }

    val hasDiscount: Boolean
        get() = discountPriceDzd != null && discountPriceDzd > 0.0 && discountPriceDzd < priceDzd
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val defaultAddress: String = "",
    val preferredLang: String = "ar",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CartItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productNameAr: String = "",
    val productNameFr: String = "",
    val productImageUrl: String = "",
    val unitPriceDzd: Double = 0.0,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val quantity: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val deliveryAddress: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val subtotalDzd: Double = 0.0,
    val totalDzd: Double = 0.0,
    val status: String = "pending",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Reservation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val preferredDate: String = "",
    val preferredTime: String = "",
    val visitorCount: Long = 1L,
    val notes: String = "",
    val channel: String = "whatsapp",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminRole(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

object DefaultCategories {
    val officialCategories: List<Category> = listOf(
        Category(id = "mens_clothing", nameAr = "ملابس رجالية", nameFr = "Vêtements Homme", slug = "mens-clothing", sortOrder = 1L, active = true, iconKey = "mens_clothing"),
        Category(id = "mens_tshirts", nameAr = "تيشيرتات رجالية", nameFr = "T-shirts Homme", slug = "mens-tshirts", sortOrder = 2L, active = true, iconKey = "tshirt"),
        Category(id = "mens_shirts", nameAr = "قمصان رجالية", nameFr = "Chemises Homme", slug = "mens-shirts", sortOrder = 3L, active = true, iconKey = "shirt"),
        Category(id = "mens_trousers", nameAr = "سراويل رجالية", nameFr = "Pantalons Homme", slug = "mens-trousers", sortOrder = 4L, active = true, iconKey = "trousers"),
        Category(id = "mens_shoes", nameAr = "أحذية رجالية", nameFr = "Chaussures Homme", slug = "mens-shoes", sortOrder = 5L, active = true, iconKey = "shoes"),
        Category(id = "kids_clothing", nameAr = "ملابس أطفال", nameFr = "Vêtements Enfants", slug = "kids-clothing", sortOrder = 6L, active = true, iconKey = "kids"),
        Category(id = "boys_clothing", nameAr = "ملابس أولاد", nameFr = "Vêtements Garçons", slug = "boys-clothing", sortOrder = 7L, active = true, iconKey = "boys"),
        Category(id = "kids_new_arrivals", nameAr = "وصولات جديدة للأطفال", nameFr = "Nouveautés Enfants", slug = "kids-new-arrivals", sortOrder = 8L, active = true, iconKey = "new_arrivals"),
        Category(id = "special_offers", nameAr = "عروض خاصة", nameFr = "Offres Spéciales", slug = "special-offers", sortOrder = 9L, active = true, iconKey = "offers")
    )
}
