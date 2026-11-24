
package com.maziwasmart.app.ui.aiscreens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.maziwasmart.app.ai.RidgeMilkYieldPredictor
import com.maziwasmart.app.ai.RidgeModelCalculator
import com.maziwasmart.app.ai.FarmAnalysis
import com.maziwasmart.app.ai.AdviceTarget
import com.maziwasmart.app.data.Cow
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.oneDecimal
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.*
import com.maziwasmart.app.ui.navigation.Routes
import com.maziwasmart.app.ui.theme.*

/* ---------------- AI MENU ---------------- */

@Composable
fun AiMenuScreen(onNavigate: (String) -> Unit) {

    ScreenWithHeader(
        title = "AI Assistant",
        subtitle = "Predictions and advice from your farm records"
    ) {

        AiMenuItem(
            Icons.Filled.AutoAwesome,
            YellowSoft,
            YellowText,
            "Milk Yield Prediction",
            "Predict milk yield using your farm records"
        ) {
            onNavigate(Routes.prediction())
        }

        AiMenuItem(
            Icons.Filled.Insights,
            GreenSoft,
            GreenDeep,
            "Feeding Insights",
            "Explore feeding and milk yield patterns"
        ) {
            onNavigate(Routes.INSIGHTS)
        }

        AiMenuItem(
            Icons.Filled.Lightbulb,
            OrangeSoft,
            OrangeText,
            "Recommendations",
            "Advice based on your farm records"
        ) {
            onNavigate(Routes.RECOMMENDATIONS)
        }

        AiMenuItem(
            Icons.Filled.Psychology,
            GreenSoft,
            GreenDeep,
            "Prediction Accuracy",
            "Review model performance"
        ) {
            onNavigate(Routes.REPORTS)
        }

        AppCard(background = GreenSoft) {
            Text(
                "What the trained model uses",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Brown
            )

            listOf(
                "Daily dry matter intake (DMI)",
                "THI Range",
                "Cow Genetic Group"
            ).forEach {
                Text("•  $it", fontSize = 12.sp, color = Brown)
            }
        }
    }
}

@Composable
private fun AiMenuItem(
    icon: ImageVector,
    bg: Color,
    tint: Color,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, LineColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Box(
            Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }

        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Brown
            )
            Text(description, fontSize = 11.sp, color = Muted)
        }

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Muted
        )
    }
}

/* ---------------- MILK YIELD PREDICTION ---------------- */

@Composable
fun PredictionScreen(
    vm: FarmViewModel,
    startCowId: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {

    val realCows by vm.cows.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val milk by vm.milk.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val predictor = remember(context) {
        RidgeMilkYieldPredictor(context)
    }

    val cows = realCows.ifEmpty {
        listOf(
            Cow(
                id = "demo",
                name = "No cow records yet",
                tagId = ""
            )
        )
    }

    var cowId by rememberSaveable {
        mutableStateOf(startCowId)
    }

    var period by rememberSaveable {
        mutableStateOf("Tomorrow")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var result by remember {
        mutableStateOf<Prediction?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val cow = cows.find { it.id == cowId } ?: cows.first()

    fun generate() {

        loading = true
        result = null
        errorMessage = null

        try {
            result = predictor.predict(cow, feedings, milk)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to generate prediction."
        }

        loading = false
    }

    LaunchedEffect(cow.id, feedings, milk) {
        generate()
    }

    ScreenWithHeader(
        title = "Milk Yield Prediction",
        onBack = onBack,
        actions = {
            Pill("ML Model", Yellow, Brown)
        }
    ) {

        DropdownField(
            "Cow",
            cows.map { it.name },
            cow.name,
            { cowId = cows[it].id }
        )

        FieldLabel("Prediction for")

        Segmented(
            listOf("Tomorrow", "Next 7 days"),
            period
        ) {
            period = it
        }

        AppCard(background = Brown) {

            Text(
                "Predicted milk yield",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            if (loading) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 14.dp)
                ) {
                    CircularProgressIndicator(
                        Modifier.size(22.dp),
                        color = Yellow,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        "Calculating prediction...",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

            } else if (result != null) {

                val p = result!!

                val value = if (period == "Tomorrow") {
                    p.predictedLitres
                } else {
                    p.predictedLitres * 7
                }

                Row(verticalAlignment = Alignment.Bottom) {

                    Text(
                        value.oneDecimal(),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = Yellow
                    )

                    Text(
                        if (period == "Tomorrow") " L / day"
                        else " L / 7 days (projected)",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Text(
                    "Generated ${p.generatedAt}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                if (period == "Next 7 days") {
                    Text(
                        "Assumes the same daily yield for seven days.",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

            } else {

                Text(
                    errorMessage ?: "No prediction available.",
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }

        val p = result

        if (p != null && !loading) {

            if (!p.dmiWithinTrainingRange) {
                AppCard(background = OrangeSoft) {
                    Text(
                        "Warning: DMI is outside the model's training range. " +
                                "Treat this prediction with caution.",
                        fontSize = 12.sp,
                        color = OrangeText
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                AppCard(
                    Modifier.weight(1f),
                    background = GreenSoft
                ) {
                    Text(
                        "Last actual yield",
                        fontSize = 11.sp,
                        color = Muted
                    )

                    Text(
                        p.lastActualLitres?.let {
                            "${it.oneDecimal()} L"
                        } ?: "—",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Brown
                    )
                }

                AppCard(
                    Modifier.weight(1f),
                    background = YellowSoft
                ) {
                    Text(
                        "Difference",
                        fontSize = 11.sp,
                        color = Muted
                    )

                    val diff = p.lastActualLitres?.let {
                        p.predictedLitres - it
                    }

                    Text(
                        diff?.let {
                            (if (it >= 0) "+" else "") +
                                    it.oneDecimal() + " L"
                        } ?: "—",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Brown
                    )
                }
            }

            AppCard {
                SectionTitle("Actual milk yield (last 7 days)")

                LineChart(
                    p.actualHistory,
                    Dates.lastNDays(7).map {
                        Dates.dayName(it)
                    }
                )

                Legend("Actual", GreenDeep)
            }

            // Explainable AI: how the model reached this number
            PredictionExplanationCard(
                dmiKg = p.averageDmiKg,
                thiRange = p.thiRange,
                geneticGroup = p.geneticGroup
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            AppOutlineButton(
                "See insights",
                { onNavigate(Routes.INSIGHTS) },
                Modifier.weight(1f)
            )

            AppButton(
                "Generate",
                { generate() },
                Modifier.weight(1f),
                small = true,
                loading = loading
            )
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 10.sp, color = Muted)
    }
}

/* ---------------- FEEDING INSIGHTS ---------------- */

@Composable
fun InsightsScreen(
    vm: FarmViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {

    val feedings by vm.feedings.collectAsState()
    val milk by vm.milk.collectAsState()

    val days = remember { Dates.lastNDays(30) }

    val points = remember(feedings, milk) {
        FarmAnalysis.dmiVsMilk(feedings, milk, days)
    }

    val supplementComparison = remember(feedings, milk) {
        FarmAnalysis.supplementComparison(feedings, milk, days)
    }

    ScreenWithHeader(
        title = "Feeding Insights",
        onBack = onBack
    ) {

        AppCard {
            SectionTitle("Dry matter intake vs milk yield")

            if (points.size < 2) {
                EmptyText("Record feeding and milk on the same days to see this chart.")
            } else {
                ScatterChart(points)

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lower DMI (kg)", fontSize = 10.sp, color = Muted)
                    Text("Higher DMI (kg)", fontSize = 10.sp, color = Muted)
                }

                Text(
                    "${points.size} cow-days in the last 30 days",
                    fontSize = 10.sp,
                    color = Muted
                )
            }
        }

        DummyAi.insights.forEach { insight ->

            AppCard(background = YellowSoft) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = YellowText
                    )

                    Column {
                        Text(
                            insight.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Brown
                        )

                        Text(
                            insight.message,
                            fontSize = 12.sp,
                            color = Brown
                        )
                    }
                }
            }
        }

        Text(
            "These are patterns in the records, not proof of cause.",
            fontSize = 10.sp,
            color = Muted
        )

        AppCard {

            SectionTitle("Supplement days")

            if (supplementComparison.isEmpty()) {
                EmptyText("No feeding days with milk records yet.")
            } else {

                val max = supplementComparison.maxOf { it.second }.coerceAtLeast(0.1)

                supplementComparison.forEachIndexed { i, item ->

                    val (label, litres) = item

                    Row(Modifier.fillMaxWidth()) {

                        Text(
                            label,
                            fontSize = 12.sp,
                            color = Brown,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            "${litres.oneDecimal()} L avg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brown
                        )
                    }

                    ProgressBar(
                        (litres / max * 0.9).toFloat(),
                        if (i == 0) Green else Orange
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(OrangeSoft)
                .clickable {
                    onNavigate(Routes.RECOMMENDATIONS)
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = OrangeText
            )

            Text(
                "Review missing feeding records",
                fontSize = 12.sp,
                color = Brown,
                modifier = Modifier.weight(1f)
            )

            Text(
                "See advice",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenDeep
            )
        }
    }
}

/* ---------------- RECOMMENDATIONS ---------------- */

@Composable
fun RecommendationsScreen(
    vm: FarmViewModel,
    onNavigate: (String) -> Unit
) {

    val cows by vm.cows.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val milk by vm.milk.collectAsState()
    val reviewedIds by vm.reviewedAdvice.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val calculator = remember(context) {
        runCatching { RidgeModelCalculator(context) }.getOrNull()
    }

    val advice = remember(cows, feedings, milk, calculator) {
        FarmAnalysis.recommendations(cows, feedings, milk, calculator)
    }

    var tab by rememberSaveable {
        mutableStateOf("New")
    }

    val reviewedSet = reviewedIds.toSet()

    val newItems = advice.filter { it.id !in reviewedSet }
    val doneItems = advice.filter { it.id in reviewedSet }

    val shown = if (tab == "New") newItems else doneItems

    ScreenWithHeader(
        title = "Recommendations",
        subtitle = "Advice from your farm records"
    ) {

        Segmented(
            listOf("New", "Reviewed"),
            tab
        ) {
            tab = it
        }

        Text(
            "${newItems.size} new · ${doneItems.size} reviewed",
            fontSize = 11.sp,
            color = Muted
        )

        if (cows.isEmpty()) {
            EmptyText("Add cows and records to get advice.")
        } else if (shown.isEmpty()) {
            EmptyText(
                if (tab == "New") "All caught up! Your records look good."
                else "Nothing reviewed yet"
            )
        }

        shown.forEach { rec ->

            val (pillBg, pillText) = when (rec.category) {
                "Data quality" -> Orange to Color.White
                "Production" -> Yellow to Brown
                else -> Green to Color.White
            }

            AppCard {

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Pill(rec.category, pillBg, pillText)

                    Spacer(Modifier.weight(1f))

                    Text(
                        rec.cowName,
                        fontSize = 11.sp,
                        color = Muted
                    )
                }

                Text(
                    rec.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown
                )

                Text(
                    rec.message,
                    fontSize = 12.sp,
                    color = Brown
                )

                Text(
                    "Why: ${rec.reason}",
                    fontSize = 11.sp,
                    color = Brown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenSoft)
                        .padding(8.dp)
                )

                val openRoute = when (rec.target) {
                    AdviceTarget.FEEDING -> Routes.feeding(rec.cowId)
                    AdviceTarget.MILK -> Routes.milk(rec.cowId)
                    AdviceTarget.COW -> Routes.cowProfile(rec.cowId)
                }

                if (tab == "New") {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        AppOutlineButton(
                            if (rec.target == AdviceTarget.COW) "Open cow" else "Open records",
                            { onNavigate(openRoute) },
                            Modifier.weight(1f)
                        )

                        AppButton(
                            "Reviewed",
                            {
                                vm.markAdviceReviewed(rec.id) { error ->
                                    if (error != null) context.toast(error)
                                }
                            },
                            Modifier.weight(1f),
                            background = Green,
                            textColor = Color.White,
                            small = true
                        )
                    }

                } else {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = GreenDeep,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            " Reviewed",
                            fontSize = 12.sp,
                            color = GreenDeep,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            "Undo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangeText,
                            modifier = Modifier.clickable {
                                vm.undoAdviceReviewed(rec.id) { error ->
                                    if (error != null) context.toast(error)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
