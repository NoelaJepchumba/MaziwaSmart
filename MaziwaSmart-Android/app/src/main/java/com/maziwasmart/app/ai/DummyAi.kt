package com.maziwasmart.app.ai

import com.maziwasmart.app.data.Cow
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.FeedingRecord
import com.maziwasmart.app.data.MilkRecord
import java.time.LocalTime
import kotlin.math.round

/*
 * ===================== AI PART (DUMMY FOR NOW) =====================
 * The real milk yield model is not connected yet.
 * Everything here returns demo numbers so the screens can be shown.
 *
 * To plug in the real model later:
 *   1. Make a new class that implements MilkYieldPredictor
 *      (for example one that calls your trained model / API)
 *   2. Change DummyAi.predictor below to use it.
 * The screens do not need to change.
 * ===================================================================
 */

data class Prediction(
    val cowName: String,
    val predictedLitres: Double,
    val confidencePercent: Int,
    val lastActualLitres: Double?,
    val generatedAt: String,
    val averageDmiKg: Double,
    val feedingsPerDay: Int,
    val usesSupplements: Boolean,
    val actualHistory: List<Double>,     // last 7 days (actual)
    val predictedHistory: List<Double>,  // last 7 days (what the model "would have" predicted)
    val isDemo: Boolean = true
)

interface MilkYieldPredictor {
    fun predict(cow: Cow, feedings: List<FeedingRecord>, milk: List<MilkRecord>): Prediction
}

/** Returns demo predictions. Uses the cow's real averages when there are records, so it looks sensible. */
class DummyPredictor : MilkYieldPredictor {

    private val demoHistory = listOf(11.8, 12.4, 12.1, 13.0, 12.6, 13.2, 12.8)
    private val demoError = listOf(0.4, -0.3, 0.2, -0.2, 0.3, -0.1, 0.2)

    override fun predict(cow: Cow, feedings: List<FeedingRecord>, milk: List<MilkRecord>): Prediction {
        val days = Dates.lastNDays(7)
        val actual = days.map { day -> milk.filter { it.cowId == cow.id && it.date == day }.sumOf { it.litres } }
        val hasMilkData = actual.any { it > 0 }
        val history = if (hasMilkData) actual else demoHistory

        val recorded = history.filter { it > 0 }
        val predicted = round1(recorded.average() + 0.5)

        val cowFeedings = feedings.filter { it.cowId == cow.id }
        val dailyDmi = cowFeedings.groupBy { it.date }.values.map { day -> day.sumOf { it.dmiKg } }
        val avgDmi = if (dailyDmi.isNotEmpty()) round1(dailyDmi.average()) else 14.2
        val perDay = cowFeedings.maxOfOrNull { it.timesPerDay } ?: 3

        val now = LocalTime.now()
        return Prediction(
            cowName = cow.name,
            predictedLitres = predicted,
            confidencePercent = 78,
            lastActualLitres = recorded.lastOrNull(),
            generatedAt = "Today, %d:%02d".format(now.hour, now.minute),
            averageDmiKg = avgDmi,
            feedingsPerDay = perDay,
            usesSupplements = cowFeedings.any { it.supplement != "None" } || cowFeedings.isEmpty(),
            actualHistory = history,
            predictedHistory = history.mapIndexed { i, v -> if (v == 0.0) 0.0 else round1(v + demoError[i]) }
        )
    }

    private fun round1(value: Double) = round(value * 10) / 10.0
}

data class Recommendation(
    val id: Int,
    val category: String,     // Data quality / Production / Feeding
    val cowName: String,
    val title: String,
    val message: String,
    val reason: String
)

data class Insight(val title: String, val message: String)

object DummyAi {
    val predictor: MilkYieldPredictor = DummyPredictor()

    /** Demo points for the "DMI vs milk yield" chart: (dry matter kg, litres) */
    val dmiVsMilk = listOf(
        10.5 to 10.2, 11.2 to 10.9, 11.8 to 11.1, 12.4 to 11.8, 12.9 to 11.6, 13.3 to 12.4,
        13.9 to 12.7, 14.4 to 13.1, 14.8 to 13.4, 15.3 to 13.8, 15.9 to 14.1, 12.0 to 9.8, 14.6 to 12.1
    )

    val insights = listOf(
        Insight(
            "Dry matter and milk yield",
            "Bella's recorded yield was higher on days with higher dry matter intake. Keep recording to see whether this pattern continues."
        ),
        Insight(
            "Feeding frequency",
            "Cows fed 3 times a day gave on average 0.8 L more milk than cows fed twice a day."
        )
    )

    val supplementComparison = listOf("With mineral lick" to 13.6, "Without supplement" to 12.4)

    val recommendations = listOf(
        Recommendation(
            1, "Data quality", "Bella", "Review feeding consistency",
            "Bella has 2 days of missing feeding records. Complete them before relying on the latest prediction.",
            "Predictions use the last 30 days of feeding data."
        ),
        Recommendation(
            2, "Production", "Neema", "Check a drop in milk yield",
            "Neema's yield fell from 11 L to 9 L over 4 days with no recorded feed change. If it continues, consult a vet.",
            "A drop of more than 15% without a feed change can mean a health problem."
        ),
        Recommendation(
            3, "Feeding", "Daisy", "Record supplement amounts",
            "Supplements were logged without amounts on 3 days.",
            "The model needs the amount (in grams) to measure the effect of supplements."
        ),
        Recommendation(
            4, "Feeding", "Malkia", "Try a third feeding session",
            "Malkia is fed twice a day. Cows on 3 feeds a day on your farm gave more milk.",
            "Based on the feeding frequency pattern in your records."
        )
    )

    const val predictionAccuracyPercent = 91
}
