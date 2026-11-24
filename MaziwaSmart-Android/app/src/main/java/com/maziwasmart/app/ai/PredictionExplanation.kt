package com.maziwasmart.app.ai

import kotlin.math.abs

enum class ExplanationKind { DMI, THI, GENETIC_GROUP }

/** How much one input adds to (+) or removes from (−) the predicted litres. */
data class ExplanationPart(
    val kind: ExplanationKind,
    val label: String,
    val detail: String,
    val litres: Double
)

/** Result of RidgeModelCalculator.explain(): baseline + parts = predicted litres. */
data class PredictionExplanation(
    val baselineLitres: Double,
    val parts: List<ExplanationPart>,
    val modelName: String,
    val dmiMin: Double,
    val dmiMax: Double
) {
    val totalLitres: Double
        get() = baselineLitres + parts.sumOf { it.litres }

    /** Inputs ordered from biggest to smallest effect. */
    val byImpact: List<ExplanationPart>
        get() = parts.sortedByDescending { abs(it.litres) }

    /** One or two plain-language sentences for the farmer. */
    fun summary(): String {
        val raising = parts.filter { it.litres >= 0.05 }.maxByOrNull { it.litres }
        val lowering = parts.filter { it.litres <= -0.05 }.minByOrNull { it.litres }

        val sentences = mutableListOf<String>()

        if (raising != null) {
            sentences += "${raising.label} raises this estimate the most " +
                "(+${"%.1f".format(raising.litres)} L)."
        }
        if (lowering != null) {
            sentences += "${lowering.label} lowers it the most " +
                "(−${"%.1f".format(abs(lowering.litres))} L)."
        }
        val dmi = parts.firstOrNull { it.kind == ExplanationKind.DMI }
        if (dmi != null && abs(dmi.litres) < 0.3 && dmi != raising && dmi != lowering) {
            sentences += "Feed intake has only a small effect in this model."
        }
        return sentences.joinToString(" ")
            .ifBlank { "All inputs have a small effect; the estimate is close to the baseline." }
    }
}
