package com.maziwasmart.app.ui.aiscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.ai.DummyAi
import com.maziwasmart.app.ai.Prediction
import com.maziwasmart.app.data.Cow
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.oneDecimal
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.AppButton
import com.maziwasmart.app.ui.components.AppCard
import com.maziwasmart.app.ui.components.AppOutlineButton
import com.maziwasmart.app.ui.components.ChipRow
import com.maziwasmart.app.ui.components.DemoBanner
import com.maziwasmart.app.ui.components.DropdownField
import com.maziwasmart.app.ui.components.EmptyText
import com.maziwasmart.app.ui.components.FieldLabel
import com.maziwasmart.app.ui.components.LineChart
import com.maziwasmart.app.ui.components.Pill
import com.maziwasmart.app.ui.components.ProgressBar
import com.maziwasmart.app.ui.components.ScatterChart
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.SectionTitle
import com.maziwasmart.app.ui.components.Segmented
import com.maziwasmart.app.ui.navigation.Routes
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.Green
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.LineColor
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.Orange
import com.maziwasmart.app.ui.theme.OrangeSoft
import com.maziwasmart.app.ui.theme.OrangeText
import com.maziwasmart.app.ui.theme.Yellow
import com.maziwasmart.app.ui.theme.YellowSoft
import com.maziwasmart.app.ui.theme.YellowText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// AI MENU - the AI tab in the bottom bar
// ---------------------------------------------------------------------------
@Composable
fun AiMenuScreen(onNavigate: (String) -> Unit) {
    ScreenWithHeader(title = "AI Assistant", subtitle = "Predictions and advice from your farm records") {
        DemoBanner()
        AiMenuItem(Icons.Filled.AutoAwesome, YellowSoft, YellowText, "Milk Yield Prediction",
            "Predict how much milk a cow will give, from her feeding and milk records") { onNavigate(Routes.prediction()) }
        AiMenuItem(Icons.Filled.Insights, GreenSoft, GreenDeep, "Feeding Insights",
            "See patterns between dry matter intake, supplements and milk yield") { onNavigate(Routes.INSIGHTS) }
        AiMenuItem(Icons.Filled.Lightbulb, OrangeSoft, OrangeText, "Recommendations",
            "Simple advice based on your records") { onNavigate(Routes.RECOMMENDATIONS) }
        AiMenuItem(Icons.Filled.Psychology, GreenSoft, GreenDeep, "Prediction Accuracy",
            "How close past predictions were to the real milk") { onNavigate(Routes.REPORTS) }

        AppCard(background = GreenSoft) {
            Text("What the AI looks at", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Brown)
            listOf(
                "Dry matter intake (DMI) per day",
                "How many times the cow is fed",
                "Supplements given",
                "The last 30 days of milk records"
            ).forEach { Text("•  $it", fontSize = 12.sp, color = Brown) }
        }
    }
}

@Composable
private fun AiMenuItem(icon: ImageVector, bg: Color, tint: Color, title: String, description: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, LineColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Brown)
            Text(description, fontSize = 11.sp, color = Muted)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}

// ---------------------------------------------------------------------------
// MILK YIELD PREDICTION (dummy)
// ---------------------------------------------------------------------------
@Composable
fun PredictionScreen(vm: FarmViewModel, startCowId: String, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val realCows by vm.cows.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val milk by vm.milk.collectAsState()
    val scope = rememberCoroutineScope()

    // if the farmer has no cows yet, show a demo cow so the screen still works
    val cows = realCows.ifEmpty { listOf(Cow(id = "demo", name = "Bella (demo)", tagId = "COW-001")) }
    var cowId by rememberSaveable { mutableStateOf(startCowId) }
    var period by rememberSaveable { mutableStateOf("Tomorrow") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Prediction?>(null) }

    val cow = cows.find { it.id == cowId } ?: cows.first()

    fun generate() {
        loading = true
        scope.launch {
            delay(1200) // pretend the model is working
            result = DummyAi.predictor.predict(cow, feedings, milk)
            loading = false
        }
    }
    // run a prediction when the screen opens and whenever a different cow is picked
    LaunchedEffect(cow.id) { generate() }

    ScreenWithHeader(title = "Milk Yield Prediction", onBack = onBack, actions = { Pill("AI", Yellow, Brown) }) {
        DemoBanner()
        DropdownField("Cow", cows.map { it.name }, cow.name, { cowId = cows[it].id })
        FieldLabel("Prediction for")
        Segmented(listOf("Tomorrow", "Next 7 days"), period) { period = it }

        val p = result
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brown).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Predicted milk yield", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                if (loading || p == null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 14.dp)) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Yellow, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Analysing records…", color = Color.White, fontSize = 13.sp)
                    }
                } else {
                    val value = if (period == "Tomorrow") p.predictedLitres else p.predictedLitres * 7
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(value.oneDecimal(), fontSize = 46.sp, fontWeight = FontWeight.Bold, color = Yellow)
                        Text(if (period == "Tomorrow") " L / day" else " L / week", fontSize = 16.sp, color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text("Generated ${p.generatedAt}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
            if (p != null && !loading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${p.confidencePercent}%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Yellow)
                    Text("confidence", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        if (p != null && !loading) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppCard(Modifier.weight(1f), background = GreenSoft) {
                    Text("Last actual yield", fontSize = 11.sp, color = Muted)
                    Text(p.lastActualLitres?.let { "${it.oneDecimal()} L" } ?: "—", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                }
                AppCard(Modifier.weight(1f), background = YellowSoft) {
                    Text("Difference", fontSize = 11.sp, color = Muted)
                    val diff = p.lastActualLitres?.let { p.predictedLitres - it }
                    Text(diff?.let { (if (it >= 0) "+" else "") + it.oneDecimal() + " L" } ?: "—", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                }
            }
            AppCard {
                SectionTitle("Actual vs predicted (last 7 days)")
                LineChart(p.actualHistory, Dates.lastNDays(7).map { Dates.dayName(it) }, secondValues = p.predictedHistory)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Legend("Actual", GreenDeep)
                    Legend("Predicted", Orange)
                }
            }
            AppCard {
                Text("Data used by the model", fontSize = 11.sp, color = Muted)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill("DMI ${p.averageDmiKg} kg/day", GreenSoft, GreenDeep)
                    Pill("${p.feedingsPerDay}× daily", GreenSoft, GreenDeep)
                    Pill(if (p.usesSupplements) "Supplements" else "No supplements", GreenSoft, GreenDeep)
                    Pill("30-day history", GreenSoft, GreenDeep)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppOutlineButton("See insights", { onNavigate(Routes.INSIGHTS) }, Modifier.weight(1f))
            AppButton("Generate", { generate() }, Modifier.weight(1f), small = true, loading = loading)
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 10.sp, color = Muted)
    }
}

// ---------------------------------------------------------------------------
// INSIGHTS (dummy)
// ---------------------------------------------------------------------------
@Composable
fun InsightsScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var range by rememberSaveable { mutableStateOf("Last 30 days") }

    ScreenWithHeader(title = "Feeding Insights", onBack = onBack) {
        DemoBanner()
        ChipRow(listOf("Last 7 days", "Last 30 days", "All time"), range) { range = it }

        AppCard {
            SectionTitle("Dry matter intake vs milk yield")
            ScatterChart(DummyAi.dmiVsMilk, highlight = setOf(11, 12))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lower DMI (kg)", fontSize = 10.sp, color = Muted)
                Text("Higher DMI (kg)", fontSize = 10.sp, color = Muted)
            }
        }

        DummyAi.insights.forEach { insight ->
            AppCard(background = YellowSoft) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = YellowText)
                    Column {
                        Text(insight.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                        Text(insight.message, fontSize = 12.sp, color = Brown)
                    }
                }
            }
        }
        Text("These are patterns in the records, not proof of cause.", fontSize = 10.sp, color = Muted)

        AppCard {
            SectionTitle("Supplement days")
            val max = DummyAi.supplementComparison.maxOf { it.second }
            DummyAi.supplementComparison.forEachIndexed { i, (label, litres) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(label, fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
                    Text("${litres.oneDecimal()} L avg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brown)
                }
                ProgressBar((litres / max * 0.9).toFloat(), if (i == 0) Green else Orange)
            }
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(OrangeSoft).clickable { onNavigate(Routes.RECOMMENDATIONS) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = OrangeText)
            Text("2 days of missing feeding records", fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
            Text("See advice", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GreenDeep)
        }
    }
}

// ---------------------------------------------------------------------------
// RECOMMENDATIONS (dummy) - the "Advice" tab
// ---------------------------------------------------------------------------
@Composable
fun RecommendationsScreen(onNavigate: (String) -> Unit) {
    val reviewed = remember { mutableStateListOf<Int>() }
    var tab by rememberSaveable { mutableStateOf("New") }

    val newItems = DummyAi.recommendations.filter { it.id !in reviewed }
    val doneItems = DummyAi.recommendations.filter { it.id in reviewed }
    val shown = if (tab == "New") newItems else doneItems

    ScreenWithHeader(title = "Recommendations", subtitle = "Advice from your farm records") {
        DemoBanner()
        Segmented(listOf("New", "Reviewed"), tab) { tab = it }
        Text("${newItems.size} new · ${doneItems.size} reviewed", fontSize = 11.sp, color = Muted)

        if (shown.isEmpty()) EmptyText(if (tab == "New") "All caught up!" else "Nothing reviewed yet")

        shown.forEach { rec ->
            val (pillBg, pillText) = when (rec.category) {
                "Data quality" -> Orange to Color.White
                "Production" -> Yellow to Brown
                else -> Green to Color.White
            }
            AppCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Pill(rec.category, pillBg, pillText)
                    Spacer(Modifier.weight(1f))
                    Text(rec.cowName, fontSize = 11.sp, color = Muted)
                }
                Text(rec.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                Text(rec.message, fontSize = 12.sp, color = Brown)
                Text("Why: ${rec.reason}", fontSize = 11.sp, color = Brown,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(GreenSoft).padding(8.dp))
                if (tab == "New") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppOutlineButton("Open records", { onNavigate(Routes.feeding()) }, Modifier.weight(1f))
                        AppButton("Reviewed", { reviewed.add(rec.id) }, Modifier.weight(1f), background = Green, textColor = Color.White, small = true)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = GreenDeep, modifier = Modifier.size(16.dp))
                        Text(" Reviewed", fontSize = 12.sp, color = GreenDeep)
                    }
                }
            }
        }
    }
}
