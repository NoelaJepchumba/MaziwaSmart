
package com.maziwasmart.app.ai

import android.content.Context
import org.json.JSONObject

class RidgeModelCalculator(context: Context) {

    private val model: JSONObject

    init {
        val json = context.assets
            .open("milk_yield_ridge.json")
            .bufferedReader()
            .use { it.readText() }

        model = JSONObject(json)
    }

    fun predict(
        dmi: Double,
        thiRange: String,
        geneticGroup: String
    ): Double {

        require(dmi >= 0.0) { "DMI cannot be negative" }

        require(thiRange in listOf("T0", "T1", "T2")) {
            "Invalid THI Range"
        }

        require(
            geneticGroup in listOf(
                "Local", "HF50", "HF62.5", "HF75", "HF87.5"
            )
        ) {
            "Invalid Genetic Group"
        }

        val features = model.getJSONArray("features")
        val coefficients = model.getJSONArray("coefficients")

        val scaledDmi =
            (dmi - model.getDouble("dmi_mean")) /
                    model.getDouble("dmi_scale")

        var result = model.getDouble("intercept")

        for (i in 0 until features.length()) {

            val feature = features.getString(i)

            val value = when {
                feature == "DMI (kg)" -> scaledDmi

                feature == "THI Range_$thiRange" -> 1.0

                feature.startsWith("THI Range_") -> 0.0

                feature == "Genetic Group_$geneticGroup" -> 1.0

                feature.startsWith("Genetic Group_") -> 0.0

                else -> error("Unknown model feature: $feature")
            }

            result += value * coefficients.getDouble(i)
        }

        return result
    }

    /**
     * Explainable AI: breaks a prediction into the exact amount each input adds or removes.
     *
     * Ridge regression is linear, so
     *   prediction = baseline + DMI effect + THI effect + genetic group effect
     * and these parts always add up to the same number predict() returns.
     */
    fun explain(
        dmi: Double,
        thiRange: String,
        geneticGroup: String
    ): PredictionExplanation {

        val features = model.getJSONArray("features")
        val coefficients = model.getJSONArray("coefficients")

        val dmiMean = model.getDouble("dmi_mean")
        val scaledDmi = (dmi - dmiMean) / model.getDouble("dmi_scale")

        val parts = mutableListOf<ExplanationPart>()

        for (i in 0 until features.length()) {

            val feature = features.getString(i)
            val coef = coefficients.getDouble(i)

            when (feature) {
                "DMI (kg)" -> parts += ExplanationPart(
                    kind = ExplanationKind.DMI,
                    label = "Feed intake",
                    detail = "${"%.1f".format(dmi)} kg DMI/day " +
                        "(training average ${"%.1f".format(dmiMean)} kg)",
                    litres = scaledDmi * coef
                )

                "THI Range_$thiRange" -> parts += ExplanationPart(
                    kind = ExplanationKind.THI,
                    label = "Weather (THI)",
                    detail = thiDescription(thiRange),
                    litres = coef
                )

                "Genetic Group_$geneticGroup" -> parts += ExplanationPart(
                    kind = ExplanationKind.GENETIC_GROUP,
                    label = "Genetic group",
                    detail = geneticGroupDescription(geneticGroup),
                    litres = coef
                )
            }
        }

        return PredictionExplanation(
            baselineLitres = model.getDouble("intercept"),
            parts = parts,
            modelName = model.optString("model_name", "Ridge Regression"),
            dmiMin = model.getDouble("dmi_min"),
            dmiMax = model.getDouble("dmi_max")
        )
    }

    private fun thiDescription(thi: String) = when (thi) {
        "T0" -> "T0 · cool, no heat stress"
        "T1" -> "T1 · mild heat stress"
        "T2" -> "T2 · high heat stress"
        else -> thi
    }

    private fun geneticGroupDescription(group: String) = when (group) {
        "Local" -> "Local breed"
        else -> "$group · ${group.removePrefix("HF")}% Holstein Friesian"
    }

    fun isDmiWithinTrainingRange(dmi: Double): Boolean {
        return dmi >= model.getDouble("dmi_min") &&
                dmi <= model.getDouble("dmi_max")
    }
}
