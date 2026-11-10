package com.maziwasmart.app.data

import com.google.firebase.firestore.Exclude

/*
 * These classes are saved in Firestore.
 * Every field has a default value so Firestore can create the object when it reads a document.
 * "id" is the Firestore document id. It is not saved as a field (@get:Exclude),
 * we fill it in ourselves after reading the document.
 *
 * Firestore structure:
 *   users/{uid}                 -> UserProfile
 *   users/{uid}/cows/{id}       -> Cow
 *   users/{uid}/feedings/{id}   -> FeedingRecord
 *   users/{uid}/milk/{id}       -> MilkRecord
 *   users/{uid}/expenses/{id}   -> Expense
 *   users/{uid}/sales/{id}      -> MilkSale
 */

data class UserProfile(
    var name: String = "",
    var phone: String = "",
    var email: String = "",
    var farmName: String = "",
    var location: String = "",
    var herdSize: String = ""
)

data class Cow(
    @get:Exclude var id: String = "",
    var name: String = "",
    var tagId: String = "",
    var breed: String = "",
    var dateOfBirth: String = "",
    var lactationStatus: String = "Lactating", // Lactating / Not lactating / Unknown
    var notes: String = "",
    var createdAt: Long = 0L
)

data class FeedingRecord(
    @get:Exclude var id: String = "",
    var cowId: String = "",
    var cowName: String = "",
    var date: String = "",              // yyyy-MM-dd
    var session: String = "",           // Morning / Afternoon / Evening
    var feedType: String = "",
    var freshWeightKg: Double = 0.0,    // what the farmer weighed
    var dryMatterPercent: Double = 0.0, // e.g. 20 for Napier grass
    var dmiKg: Double = 0.0,            // dry matter intake = fresh weight x DM%
    var supplement: String = "None",
    var supplementGrams: Double = 0.0,
    var timesPerDay: Int = 1,
    var createdAt: Long = 0L
)

data class MilkRecord(
    @get:Exclude var id: String = "",
    var cowId: String = "",
    var cowName: String = "",
    var date: String = "",      // yyyy-MM-dd
    var session: String = "",   // Morning / Evening
    var litres: Double = 0.0,
    var createdAt: Long = 0L
)

data class Expense(
    @get:Exclude var id: String = "",
    var category: String = "",  // Feed, Supplements, Veterinary, Labour, Transport, Other
    var description: String = "",
    var amount: Double = 0.0,   // KSh
    var date: String = "",
    var createdAt: Long = 0L
)

data class MilkSale(
    @get:Exclude var id: String = "",
    var litres: Double = 0.0,
    var pricePerLitre: Double = 0.0,
    var date: String = "",
    var createdAt: Long = 0L
) {
    @get:Exclude
    val total: Double get() = litres * pricePerLitre
}
