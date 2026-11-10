package com.maziwasmart.app.ui.cows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.ai.DummyAi
import com.maziwasmart.app.data.Breeds
import com.maziwasmart.app.data.Cow
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.clean
import com.maziwasmart.app.data.oneDecimal
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.AppButton
import com.maziwasmart.app.ui.components.AppCard
import com.maziwasmart.app.ui.components.AppOutlineButton
import com.maziwasmart.app.ui.components.AppTextField
import com.maziwasmart.app.ui.components.BarChart
import com.maziwasmart.app.ui.components.ChipRow
import com.maziwasmart.app.ui.components.DropdownField
import com.maziwasmart.app.ui.components.EmptyText
import com.maziwasmart.app.ui.components.FieldLabel
import com.maziwasmart.app.ui.components.HeaderIconButton
import com.maziwasmart.app.ui.components.ListRow
import com.maziwasmart.app.ui.components.MiniStat
import com.maziwasmart.app.ui.components.Pill
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.SectionTitle
import com.maziwasmart.app.ui.components.Segmented
import com.maziwasmart.app.ui.components.toast
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

private val avatarColors = listOf(Green to Color.White, Yellow to Brown, Orange to Color.White, Brown to Yellow, GreenSoft to GreenDeep)

@Composable
fun CowAvatar(cow: Cow, index: Int, size: Int = 52) {
    val (bg, fg) = avatarColors[index % avatarColors.size]
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(16.dp)).background(bg), contentAlignment = Alignment.Center) {
        Text(cow.name.take(1).uppercase(), color = fg, fontSize = (size / 2.5).sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// My Cows (list)
// ---------------------------------------------------------------------------
@Composable
fun CowsScreen(vm: FarmViewModel, onNavigate: (String) -> Unit) {
    val cows by vm.cows.collectAsState()
    val milk by vm.milk.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var breedFilter by rememberSaveable { mutableStateOf("All") }
    val today = Dates.today()

    val breeds = listOf("All") + cows.map { it.breed }.filter { it.isNotBlank() }.distinct()
    val shown = cows.sortedBy { it.tagId }.filter { cow ->
        (breedFilter == "All" || cow.breed == breedFilter) &&
            (search.isBlank() || cow.name.contains(search, ignoreCase = true) || cow.tagId.contains(search, ignoreCase = true))
    }

    ScreenWithHeader(
        title = "My Cows",
        subtitle = "${cows.size} registered",
        actions = { HeaderIconButton(Icons.Filled.Add, "Add cow") { onNavigate(Routes.ADD_COW) } }
    ) {
        AppTextField(search, { search = it }, "Search by name or ID", leadingIcon = Icons.Filled.Search)
        ChipRow(breeds, breedFilter) { breedFilter = it }

        if (shown.isEmpty()) EmptyText(if (cows.isEmpty()) "No cows yet. Tap + to add your first cow." else "No cows match your search.")

        shown.forEachIndexed { index, cow ->
            val litres = milk.filter { it.cowId == cow.id && it.date == today }.sumOf { it.litres }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, LineColor, RoundedCornerShape(18.dp))
                    .clickable { onNavigate(Routes.cowProfile(cow.id)) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CowAvatar(cow, index)
                Column(Modifier.weight(1f)) {
                    Text("${cow.name}  ·  ${cow.tagId}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                    Text("${cow.breed} · ${cow.lactationStatus}", fontSize = 11.sp,
                        color = if (cow.lactationStatus == "Lactating") GreenDeep else Muted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (litres > 0) "${litres.clean()} L" else "—", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                    Text("today", fontSize = 11.sp, color = Muted)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add Cow
// ---------------------------------------------------------------------------
@Composable
fun AddCowScreen(vm: FarmViewModel, onBack: () -> Unit) {
    val cows by vm.cows.collectAsState()
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var tagId by rememberSaveable { mutableStateOf("COW-%03d".format(cows.size + 1)) }
    var breed by rememberSaveable { mutableStateOf(Breeds.all.first()) }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("Lactating") }
    var notes by rememberSaveable { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    ScreenWithHeader(title = "Add Cow", onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(name, { name = it }, "Cow name", Modifier.weight(1f))
            AppTextField(tagId, { tagId = it }, "Cow ID", Modifier.weight(1f))
        }
        DropdownField("Breed", Breeds.all, breed, { breed = Breeds.all[it] })
        AppTextField(dateOfBirth, { dateOfBirth = it }, "Date of birth (optional, e.g. 2021-03-12)")
        FieldLabel("Lactation status")
        Segmented(listOf("Lactating", "Not lactating", "Unknown"), status) { status = it }
        AppTextField(notes, { notes = it }, "Notes (optional)", singleLine = false)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppOutlineButton("Cancel", onBack, Modifier.weight(1f))
            AppButton("Save Cow", small = true, loading = saving, modifier = Modifier.weight(1f), onClick = {
                when {
                    name.isBlank() -> context.toast("Enter the cow's name")
                    tagId.isBlank() -> context.toast("Enter the cow ID")
                    cows.any { it.tagId.equals(tagId.trim(), ignoreCase = true) } -> context.toast("Cow ID $tagId is already used")
                    else -> {
                        saving = true
                        vm.addCow(Cow(name = name.trim(), tagId = tagId.trim(), breed = breed, dateOfBirth = dateOfBirth.trim(),
                            lactationStatus = status, notes = notes.trim())) { error ->
                            saving = false
                            if (error == null) {
                                context.toast("$name saved")
                                onBack()
                            } else context.toast(error)
                        }
                    }
                }
            })
        }
    }
}

// ---------------------------------------------------------------------------
// Cow Profile
// ---------------------------------------------------------------------------
@Composable
fun CowProfileScreen(vm: FarmViewModel, cowId: String, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val cows by vm.cows.collectAsState()
    val milk by vm.milk.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    val cow = cows.find { it.id == cowId }
    if (cow == null) {
        ScreenWithHeader(title = "Cow", onBack = onBack) { EmptyText("Loading…") }
        return
    }
    val index = cows.sortedBy { it.tagId }.indexOf(cow)

    val today = Dates.today()
    val week = Dates.lastNDays(7)
    val cowMilk = milk.filter { it.cowId == cow.id }
    val cowFeedings = feedings.filter { it.cowId == cow.id }
    val todayLitres = cowMilk.filter { it.date == today }.sumOf { it.litres }
    val daily = week.map { day -> cowMilk.filter { it.date == day }.sumOf { it.litres } }
    val recordedDays = daily.filter { it > 0 }
    val weekAverage = if (recordedDays.isEmpty()) 0.0 else recordedDays.average()
    val todayDmi = cowFeedings.filter { it.date == today }.sumOf { it.dmiKg }
    val prediction = DummyAi.predictor.predict(cow, feedings, milk)

    // newest 6 milk + feeding entries together
    val recent = (cowMilk.map { Triple(it.createdAt, "Milk · ${it.session} · ${it.litres.clean()} L", Dates.pretty(it.date)) } +
        cowFeedings.map { Triple(it.createdAt, "${it.feedType} ${it.freshWeightKg.clean()} kg → ${it.dmiKg.oneDecimal()} kg DM", Dates.pretty(it.date) + " · " + it.session) })
        .sortedByDescending { it.first }.take(6)

    ScreenWithHeader(
        title = cow.name,
        subtitle = "${cow.tagId} · ${cow.breed}",
        onBack = onBack,
        actions = { HeaderIconButton(Icons.Filled.Delete, "Delete cow") { confirmDelete = true } },
        headerExtra = {
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CowAvatar(cow, index, size = 44)
                Pill(cow.lactationStatus, Yellow, Brown)
            }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("${todayLitres.clean()} L", "Today", GreenSoft, Modifier.weight(1f))
            MiniStat("${weekAverage.oneDecimal()} L", "7-day avg", YellowSoft, Modifier.weight(1f))
            MiniStat("${prediction.predictedLitres} L", "Predicted*", OrangeSoft, Modifier.weight(1f))
        }
        AppCard {
            SectionTitle("Milk, last 7 days (L)")
            BarChart(daily, week.map { Dates.pretty(it).substringBefore(" ") })
        }
        AppCard(background = GreenSoft) {
            Text("Dry matter intake today", fontSize = 11.sp, color = Muted)
            Text("${todayDmi.oneDecimal()} kg", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
        }
        SectionTitle("Recent activity")
        if (recent.isEmpty()) EmptyText("No records yet for ${cow.name}")
        recent.forEach { (_, title, subtitle) ->
            val isMilk = title.startsWith("Milk")
            ListRow(
                if (isMilk) Icons.Outlined.WaterDrop else Icons.Filled.Grass,
                if (isMilk) YellowSoft else OrangeSoft,
                if (isMilk) YellowText else OrangeText,
                title, subtitle
            )
        }
        Text("* demo prediction - AI model not connected yet", fontSize = 10.sp, color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppOutlineButton("Feeding", { onNavigate(Routes.feeding(cow.id)) }, Modifier.weight(1f))
            AppOutlineButton("Milk", { onNavigate(Routes.milk(cow.id)) }, Modifier.weight(1f))
            AppButton("Predict", { onNavigate(Routes.prediction(cow.id)) }, Modifier.weight(1f), background = Green, textColor = Color.White, small = true)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${cow.name}?") },
            text = { Text("The cow will be removed. Her past feeding and milk records stay saved.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteCow(cow.id) { error ->
                        if (error == null) onBack() else context.toast(error)
                    }
                }) { Text("Delete", color = OrangeText) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
