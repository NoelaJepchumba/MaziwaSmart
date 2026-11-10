package com.maziwasmart.app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.ai.DummyAi
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.clean
import com.maziwasmart.app.data.ksh
import com.maziwasmart.app.data.oneDecimal
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.AppButton
import com.maziwasmart.app.ui.components.AppCard
import com.maziwasmart.app.ui.components.AppOutlineButton
import com.maziwasmart.app.ui.components.BarChart
import com.maziwasmart.app.ui.components.EmptyText
import com.maziwasmart.app.ui.components.HeaderIconButton
import com.maziwasmart.app.ui.components.Pill
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.SectionTitle
import com.maziwasmart.app.ui.components.Segmented
import com.maziwasmart.app.ui.components.StatCard
import com.maziwasmart.app.ui.components.toast
import com.maziwasmart.app.ui.navigation.Routes
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.Green
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.Orange
import com.maziwasmart.app.ui.theme.Yellow
import com.maziwasmart.app.ui.theme.YellowSoft

@Composable
fun ReportsScreen(vm: FarmViewModel, onNavigate: (String) -> Unit) {
    val cows by vm.cows.collectAsState()
    val milk by vm.milk.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val expenses by vm.expenses.collectAsState()
    val sales by vm.sales.collectAsState()
    val context = LocalContext.current
    var period by rememberSaveable { mutableStateOf("Month") }

    val days = if (period == "Week") 7 else 30
    val range = Dates.lastNDays(days).toSet()
    val periodMilk = milk.filter { it.date in range }
    val periodFeedings = feedings.filter { it.date in range }
    val totalMilk = periodMilk.sumOf { it.litres }
    val cowDays = periodMilk.map { it.cowId to it.date }.distinct().size
    val avgPerCowDay = if (cowDays == 0) 0.0 else totalMilk / cowDays

    // average litres per day for each cow, highest first
    val byCow = cows.map { cow ->
        val records = periodMilk.filter { it.cowId == cow.id }
        val recordedDays = records.map { it.date }.distinct().size
        cow.name to if (recordedDays == 0) 0.0 else records.sumOf { it.litres } / recordedDays
    }.sortedByDescending { it.second }.take(6)

    val dmiDays = periodFeedings.map { it.cowId to it.date }.distinct().size
    val avgDmi = if (dmiDays == 0) 0.0 else periodFeedings.sumOf { it.dmiKg } / dmiDays
    val spent = expenses.filter { it.date in range }.sumOf { it.amount }
    val earned = sales.filter { it.date in range }.sumOf { it.total }

    ScreenWithHeader(
        title = "Reports",
        subtitle = if (period == "Week") "Last 7 days" else "Last 30 days",
        actions = { HeaderIconButton(Icons.Filled.Download, "Download") { context.toast("Export coming soon") } }
    ) {
        Segmented(listOf("Week", "Month"), period) { period = it }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("${totalMilk.clean()} L", "Total milk", Green, Color.White, Modifier.weight(1f))
            StatCard("${avgPerCowDay.oneDecimal()} L", "Avg / cow / day", Yellow, Brown, Modifier.weight(1f))
        }

        AppCard {
            SectionTitle("Yield by cow (avg L/day)")
            if (byCow.isEmpty()) EmptyText("No milk records in this period")
            else BarChart(byCow.map { it.second }, byCow.map { it.first }, colors = listOf(GreenDeep, Yellow, Orange, Brown, Green, GreenSoft))
        }

        AppCard(background = GreenSoft) {
            Text("Average dry matter intake", fontSize = 11.sp, color = Muted)
            Text("${avgDmi.oneDecimal()} kg / cow / day", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
            Text("${periodFeedings.size} feeding records", fontSize = 11.sp, color = Muted)
        }

        AppCard(background = YellowSoft) {
            Row {
                Text("Prediction accuracy", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Brown, modifier = Modifier.weight(1f))
                Pill("DEMO", Yellow, Brown)
            }
            Text("${DummyAi.predictionAccuracyPercent}%", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
            Text("of predictions were within ±1 L of the actual yield (dummy value until the AI model is connected)", fontSize = 11.sp, color = Brown)
        }

        AppCard {
            SectionTitle("Money", "Details") { onNavigate(Routes.FINANCE) }
            Row { Text("Milk sales", fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f)); Text(earned.ksh(), fontSize = 12.sp, color = GreenDeep) }
            Row { Text("Expenses", fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f)); Text(spent.ksh(), fontSize = 12.sp, color = Brown) }
            Row {
                Text("Estimated profit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brown, modifier = Modifier.weight(1f))
                Text((earned - spent).ksh(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brown)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppOutlineButton("Download CSV", { context.toast("CSV export coming soon") }, Modifier.weight(1f))
            AppButton("Export PDF", { context.toast("PDF export coming soon") }, Modifier.weight(1f), background = Brown, textColor = Yellow, small = true)
        }
    }
}
