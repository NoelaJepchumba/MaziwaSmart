package com.maziwasmart.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.maziwasmart.app.ai.PredictionExplanation
import com.maziwasmart.app.ai.RidgeModelCalculator
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.Green
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.LineColor
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.Orange
import com.maziwasmart.app.ui.theme.OrangeText
import kotlin.math.abs

/**
 * Explainable AI card: "Why this prediction?"
 *
 * Shows how the trained ridge model reached the predicted litres:
 * baseline + effect of feed intake + effect of weather (THI) + effect of genetic group.
 * Used on the Milk Yield Prediction screen and the Cow Profile.
 */
@Composable
fun PredictionExplanationCard(
    dmiKg: Double,
    thiRange: String,
    geneticGroup: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val explanation = remember(dmiKg, thiRange, geneticGroup) {
        runCatching {
            RidgeModelCalculator(context).explain(dmiKg, thiRange, geneticGroup)
        }.getOrNull()
    } ?: return

    AppCard(modifier) {
        SectionTitle("Why this prediction?")

        Text(
            explanation.summary(),
            fontSize = 12.sp,
            color = Brown
        )

        ExplanationBreakdown(explanation)

        HowItWorks(explanation)
    }
}

/** Baseline, one row per input with a +/− bar, then the total. */
@Composable
fun ExplanationBreakdown(explanation: PredictionExplanation) {

    val maxEffect = explanation.parts
        .maxOfOrNull { abs(it.litres) }
        ?.coerceAtLeast(0.1) ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        LitresRow(
            "Starting point",
            "Model baseline before looking at this cow",
            "${"%.1f".format(explanation.baselineLitres)} L",
            Brown
        )

        explanation.byImpact.forEach { part ->

            val positive = part.litres >= 0
            val sign = if (positive) "+" else "−"

            LitresRow(
                part.label,
                part.detail,
                "$sign${"%.1f".format(abs(part.litres))} L",
                if (positive) GreenDeep else OrangeText
            )

            ProgressBar(
                (abs(part.litres) / maxEffect).toFloat(),
                if (positive) Green else Orange
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LineColor)
        )

        LitresRow(
            "Predicted yield",
            "Starting point + all effects",
            "${"%.1f".format(explanation.totalLitres)} L/day",
            GreenDeep,
            bold = true
        )
    }
}

@Composable
private fun LitresRow(
    title: String,
    subtitle: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
                color = Brown
            )
            Text(subtitle, fontSize = 11.sp, color = Muted)
        }
        Text(
            value,
            fontSize = if (bold) 15.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

/** Expandable plain-language explanation of the model itself. */
@Composable
private fun HowItWorks(explanation: PredictionExplanation) {

    var open by rememberSaveable { mutableStateOf(false) }

    Text(
        if (open) "Hide how the model works ▲" else "How does the model work? ▼",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = GreenDeep,
        modifier = Modifier
            .clickable { open = !open }
            .padding(vertical = 4.dp)
    )

    if (!open) return

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GreenSoft)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            "MaziwaSmart uses a ${explanation.modelName} model trained on dairy farm records.",
            "It uses only three inputs: the cow's daily dry matter intake (DMI), the weather " +
                "heat level (THI range) on the latest feeding day, and the cow's genetic group.",
            "Each input adds or removes a fixed amount of milk, shown above. The amounts " +
                "always add up to the predicted yield, so nothing is hidden.",
            "Genetic group and weather have the biggest effect. In the training data, feed " +
                "intake had only a small effect, so changing feed amounts changes the estimate " +
                "only a little.",
            "The model is most reliable for DMI between ${"%.1f".format(explanation.dmiMin)} " +
                "and ${"%.1f".format(explanation.dmiMax)} kg/day.",
            "Lactation status, supplements, health and age are kept as farm records but are " +
                "not used by the model. Use the estimate as a guide, not a guarantee."
        ).forEach {
            Text("• $it", fontSize = 11.sp, color = Brown)
        }
    }
}
