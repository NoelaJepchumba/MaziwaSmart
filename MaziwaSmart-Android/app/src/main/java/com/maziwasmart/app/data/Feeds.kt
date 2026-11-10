package com.maziwasmart.app.data

/**
 * Feed types and their dry matter (DM) percentages.
 * Fresh feed contains a lot of water, so we convert what the farmer weighs
 * into dry matter intake (DMI), which is what the AI model uses.
 *
 * DMI = fresh weight x dry matter %
 * e.g. 30 kg Napier x 0.20 = 6 kg DMI
 *
 * NOTE: these DM values are typical values - change them to the ones from your research source.
 */
data class FeedType(val name: String, val shortName: String, val dryMatterPercent: Double)

object Feeds {
    val types = listOf(
        FeedType("Napier grass", "Napier", 20.0),
        FeedType("Hay", "Hay", 88.0),
        FeedType("Maize silage", "Silage", 30.0),
        FeedType("Dairy meal", "Dairy meal", 90.0)
    )

    val supplements = listOf("None", "Mineral lick", "Salt", "Molasses", "Other")

    fun dryMatterIntake(freshWeightKg: Double, dryMatterPercent: Double): Double =
        freshWeightKg * dryMatterPercent / 100.0
}

object Breeds {
    val all = listOf("Friesian", "Ayrshire", "Jersey", "Guernsey", "Crossbreed", "Other")
}

object ExpenseCategories {
    val all = listOf("Feed", "Supplements", "Veterinary", "Labour", "Transport", "Other")
}
