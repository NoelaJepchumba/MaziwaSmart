package com.maziwasmart.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.clean
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.AppCard
import com.maziwasmart.app.ui.components.Avatar
import com.maziwasmart.app.ui.components.Banner
import com.maziwasmart.app.ui.components.HeaderIconButton
import com.maziwasmart.app.ui.components.LineChart
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.SectionTitle
import com.maziwasmart.app.ui.components.StatCard
import com.maziwasmart.app.ui.components.toast
import com.maziwasmart.app.ui.navigation.Routes
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.BrownSoft
import com.maziwasmart.app.ui.theme.Green
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.Orange
import com.maziwasmart.app.ui.theme.OrangeSoft
import com.maziwasmart.app.ui.theme.OrangeText
import com.maziwasmart.app.ui.theme.Yellow
import com.maziwasmart.app.ui.theme.YellowSoft
import com.maziwasmart.app.ui.theme.YellowText
import java.time.LocalTime

@Composable
fun DashboardScreen(vm: FarmViewModel, onNavigate: (String) -> Unit) {
    val cows by vm.cows.collectAsState()
    val milk by vm.milk.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val expenses by vm.expenses.collectAsState()
    val profile by vm.profile.collectAsState()
    val context = LocalContext.current

    val today = Dates.today()
    val milkToday = milk.filter { it.date == today }.sumOf { it.litres }
    val feedingsToday = feedings.count { it.date == today }
    val spentToday = expenses.filter { it.date == today }.sumOf { it.amount }

    val week = Dates.lastNDays(7)
    val weeklyMilk = week.map { day -> milk.filter { it.date == day }.sumOf { it.litres } }

    // lactating cows with no feeding record today
    val fedToday = feedings.filter { it.date == today }.map { it.cowId }.toSet()
    val notFed = cows.filter { it.lactationStatus == "Lactating" && it.id !in fedToday }

    val name = profile?.name?.substringBefore(" ")?.ifBlank { null } ?: "Farmer"
    val initials = profile?.name?.split(" ")?.filter { it.isNotBlank() }?.take(2)
        ?.joinToString("") { it.first().uppercase() }?.ifBlank { null } ?: "MS"
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    ScreenWithHeader(
        title = "$greeting, $name",
        subtitle = "${profile?.farmName?.ifBlank { null } ?: "My farm"} · ${Dates.todayLong()}",
        actions = {
            HeaderIconButton(Icons.Filled.Notifications, "Alerts") {
                context.toast(if (notFed.isEmpty()) "No new alerts" else "${notFed.size} cow(s) not fed today")
            }
            Spacer(Modifier.width(8.dp))
            Avatar(initials) { onNavigate(Routes.SETTINGS) }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("${cows.size}", "Registered cows", Green, Color.White, Modifier.weight(1f))
            StatCard("${milkToday.clean()} L", "Milk today", Yellow, Brown, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("$feedingsToday", "Feeding entries today", Orange, Color.White, Modifier.weight(1f))
            StatCard(String.format("%,.0f", spentToday), "KSh expenses today", Brown, Color.White, Modifier.weight(1f), valueColor = Yellow)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickAction("Add cow", Icons.Filled.Add, GreenSoft, GreenDeep) { onNavigate(Routes.ADD_COW) }
            QuickAction("Feeding", Icons.Filled.Grass, OrangeSoft, OrangeText) { onNavigate(Routes.feeding()) }
            QuickAction("Milk", Icons.Outlined.WaterDrop, YellowSoft, YellowText) { onNavigate(Routes.milk()) }
            QuickAction("Expense", Icons.Filled.Payments, BrownSoft, Brown) { onNavigate(Routes.FINANCE) }
        }

        AppCard {
            SectionTitle("Milk this week")
            LineChart(weeklyMilk, week.map { Dates.dayName(it) })
            Text("Total: ${weeklyMilk.sum().clean()} L", fontSize = 11.sp, color = GreenDeep)
        }

        when {
            cows.isEmpty() -> Banner("Start by adding your first cow", Icons.Filled.Add, GreenSoft, GreenDeep) { onNavigate(Routes.ADD_COW) }
            notFed.isNotEmpty() -> Banner(
                "${notFed.size} cow(s) have no feeding record today: ${notFed.take(3).joinToString { it.name }}",
                Icons.Filled.Warning, OrangeSoft, OrangeText
            ) { onNavigate(Routes.feeding(notFed.first().id)) }
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, background: Color, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(background), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Brown)
    }
}
