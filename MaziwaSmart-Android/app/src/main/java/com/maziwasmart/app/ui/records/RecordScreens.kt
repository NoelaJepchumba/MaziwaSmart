package com.maziwasmart.app.ui.records

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.data.Cow
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.FeedingRecord
import com.maziwasmart.app.data.Feeds
import com.maziwasmart.app.data.MilkRecord
import com.maziwasmart.app.data.clean
import com.maziwasmart.app.data.oneDecimal
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.AppButton
import com.maziwasmart.app.ui.components.AppTextField
import com.maziwasmart.app.ui.components.ChipRow
import com.maziwasmart.app.ui.components.DropdownField
import com.maziwasmart.app.ui.components.EmptyText
import com.maziwasmart.app.ui.components.FieldLabel
import com.maziwasmart.app.ui.components.ListRow
import com.maziwasmart.app.ui.components.MiniStat
import com.maziwasmart.app.ui.components.Pill
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.SectionTitle
import com.maziwasmart.app.ui.components.Segmented
import com.maziwasmart.app.ui.components.toast
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.FieldBg
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.LineColor
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.OrangeSoft
import com.maziwasmart.app.ui.theme.Yellow
import com.maziwasmart.app.ui.theme.YellowSoft
import java.time.LocalDate

/** Opens the phone's calendar to pick a date */
private fun pickDate(context: Context, current: String, onPicked: (String) -> Unit) {
    val d = runCatching { LocalDate.parse(current) }.getOrDefault(LocalDate.now())
    DatePickerDialog(context, { _, y, m, day -> onPicked(Dates.of(y, m + 1, day)) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
}

/** Cow picker + date picker row used by both record screens */
@Composable
private fun CowAndDateRow(cows: List<Cow>, cowId: String, onCow: (String) -> Unit, date: String, onDate: (String) -> Unit) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownField(
            label = "Cow",
            options = cows.map { "${it.name} (${it.tagId})" },
            selected = cows.find { it.id == cowId }?.name ?: "Select cow",
            onSelect = { onCow(cows[it].id) },
            modifier = Modifier.weight(1.2f)
        )
        Box(Modifier.weight(1f)) {
            OutlinedTextField(
                value = Dates.pretty(date), onValueChange = {}, readOnly = true, label = { Text("Date") },
                trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = LineColor, unfocusedContainerColor = FieldBg)
            )
            Box(Modifier.matchParentSize().clickable { pickDate(context, date, onDate) })
        }
    }
}

// ---------------------------------------------------------------------------
// FEEDING RECORDS - uses dry matter: DMI = fresh weight x DM%
// ---------------------------------------------------------------------------
@Composable
fun FeedingScreen(vm: FarmViewModel, startCowId: String, onBack: () -> Unit, onSaved: (String) -> Unit) {
    val cows by vm.cows.collectAsState()
    val feedings by vm.feedings.collectAsState()
    val context = LocalContext.current

    var cowId by rememberSaveable { mutableStateOf(startCowId) }
    var date by rememberSaveable { mutableStateOf(Dates.today()) }
    var session by rememberSaveable { mutableStateOf("Morning") }
    var feedName by rememberSaveable { mutableStateOf(Feeds.types.first().shortName) }
    var freshWeight by rememberSaveable { mutableStateOf("") }
    var dmPercent by rememberSaveable { mutableStateOf(Feeds.types.first().dryMatterPercent.clean()) }
    var supplement by rememberSaveable { mutableStateOf("None") }
    var supplementGrams by rememberSaveable { mutableStateOf("") }
    var timesPerDay by rememberSaveable { mutableStateOf("3") }
    var saving by remember { mutableStateOf(false) }

    // pick the first cow automatically once the list loads
    LaunchedEffect(cows) { if (cowId.isBlank() && cows.isNotEmpty()) cowId = cows.first().id }

    val feed = Feeds.types.first { it.shortName == feedName }
    val fresh = freshWeight.toDoubleOrNull() ?: 0.0
    val dm = dmPercent.toDoubleOrNull() ?: 0.0
    val dmi = Feeds.dryMatterIntake(fresh, dm)

    val cow = cows.find { it.id == cowId }
    val todays = feedings.filter { it.cowId == cowId && it.date == date }.sortedBy { it.createdAt }

    ScreenWithHeader(title = "Record Feeding", onBack = onBack, actions = { Pill("Key research data", Yellow, Brown) }) {
        if (cows.isEmpty()) {
            EmptyText("Add a cow first, then record her feeding.")
            return@ScreenWithHeader
        }
        CowAndDateRow(cows, cowId, { cowId = it }, date, { date = it })

        FieldLabel("Feeding time")
        Segmented(listOf("Morning", "Afternoon", "Evening"), session) { session = it }

        FieldLabel("Feed type")
        ChipRow(Feeds.types.map { it.shortName }, feedName) { picked ->
            feedName = picked
            // fill in the dry matter % for the chosen feed
            dmPercent = Feeds.types.first { it.shortName == picked }.dryMatterPercent.clean()
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(freshWeight, { freshWeight = it }, "Fresh weight", Modifier.weight(1f), KeyboardType.Decimal, suffix = "kg")
            AppTextField(dmPercent, { dmPercent = it }, "Dry matter", Modifier.weight(1f), KeyboardType.Decimal, suffix = "%")
        }

        // The DMI result card
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brown).padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text("Dry matter intake (DMI)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
            Text("${dmi.oneDecimal()} kg", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Yellow)
            Text(
                "${fresh.clean()} kg × ${"%.2f".format(dm / 100)} = ${dmi.oneDecimal()} kg",
                fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownField("Supplement", Feeds.supplements, supplement, { supplement = Feeds.supplements[it] }, Modifier.weight(1f))
            AppTextField(supplementGrams, { supplementGrams = it }, "Amount", Modifier.weight(1f), KeyboardType.Decimal, suffix = "g")
        }
        AppTextField(timesPerDay, { timesPerDay = it }, "Times fed per day", keyboardType = KeyboardType.Number, suffix = "feeds")

        AppButton("Save Feeding Record", loading = saving, onClick = {
            when {
                cow == null -> context.toast("Select a cow")
                fresh <= 0 -> context.toast("Enter the fresh weight in kg")
                dm <= 0 || dm > 100 -> context.toast("Dry matter must be between 1 and 100%")
                else -> {
                    saving = true
                    val record = FeedingRecord(
                        cowId = cow.id, cowName = cow.name, date = date, session = session, feedType = feed.name,
                        freshWeightKg = fresh, dryMatterPercent = dm, dmiKg = dmi,
                        supplement = supplement, supplementGrams = supplementGrams.toDoubleOrNull() ?: 0.0,
                        timesPerDay = timesPerDay.toIntOrNull() ?: 1
                    )
                    vm.addFeeding(record) { error ->
                        saving = false
                        if (error == null) {
                            context.toast("Saved · ${dmi.oneDecimal()} kg DMI")
                            freshWeight = ""
                            onSaved(cow.id)
                        } else context.toast(error)
                    }
                }
            }
        })

        SectionTitle("${cow?.name ?: ""} on ${Dates.pretty(date)}: ${todays.sumOf { it.dmiKg }.oneDecimal()} kg DMI")
        if (todays.isEmpty()) EmptyText("No feeding recorded for this day yet")
        todays.forEach { f ->
            ListRow(
                Icons.Filled.Grass, GreenSoft, GreenDeep,
                "${f.feedType} ${f.freshWeightKg.clean()} kg → ${f.dmiKg.oneDecimal()} kg DM",
                "${f.session} · ${f.dryMatterPercent.clean()}% DM" + if (f.supplement != "None") " · ${f.supplement}" else ""
            ) {
                IconButton(onClick = { vm.deleteFeeding(f.id) { e -> context.toast(e ?: "Deleted") } }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Muted)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// MILK RECORDS
// ---------------------------------------------------------------------------
@Composable
fun MilkScreen(vm: FarmViewModel, startCowId: String, onBack: () -> Unit, onSaved: (String) -> Unit) {
    val cows by vm.cows.collectAsState()
    val milk by vm.milk.collectAsState()
    val context = LocalContext.current

    var cowId by rememberSaveable { mutableStateOf(startCowId) }
    var date by rememberSaveable { mutableStateOf(Dates.today()) }
    var session by rememberSaveable { mutableStateOf("Morning") }
    var litresText by rememberSaveable { mutableStateOf("8.0") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(cows) { if (cowId.isBlank() && cows.isNotEmpty()) cowId = cows.first().id }

    val cow = cows.find { it.id == cowId }
    val litres = litresText.toDoubleOrNull() ?: 0.0
    val dayRecords = milk.filter { it.cowId == cowId && it.date == date }
    val morning = dayRecords.filter { it.session == "Morning" }.sumOf { it.litres }
    val evening = dayRecords.filter { it.session == "Evening" }.sumOf { it.litres }
    val yesterday = milk.filter { it.cowId == cowId && it.date == LocalDate.parse(date).minusDays(1).toString() }.sumOf { it.litres }
    val history = milk.sortedByDescending { it.date + it.createdAt }.take(10)

    ScreenWithHeader(title = "Record Milk", onBack = onBack, actions = { Pill("Key research data", Yellow, Brown) }) {
        if (cows.isEmpty()) {
            EmptyText("Add a cow first, then record her milk.")
            return@ScreenWithHeader
        }
        CowAndDateRow(cows, cowId, { cowId = it }, date, { date = it })
        FieldLabel("Milking session")
        Segmented(listOf("Morning", "Evening"), session) { session = it }

        FieldLabel("Milk quantity")
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FieldBg).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepButton(Icons.Filled.Remove) { litresText = (litres - 0.5).coerceAtLeast(0.0).oneDecimal() }
            AppTextField(litresText, { litresText = it }, "Litres", Modifier.weight(1f).padding(horizontal = 10.dp), KeyboardType.Decimal, suffix = "L")
            StepButton(Icons.Filled.Add) { litresText = (litres + 0.5).oneDecimal() }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("${morning.clean()} L", "Morning", YellowSoft, Modifier.weight(1f))
            MiniStat("${evening.clean()} L", "Evening", OrangeSoft, Modifier.weight(1f))
            MiniStat("${(morning + evening).clean()} L", "Total", GreenSoft, Modifier.weight(1f))
        }
        Text("Previous day: ${yesterday.clean()} L · the total is calculated automatically", fontSize = 11.sp, color = Muted)

        AppButton("Save Milk Record", loading = saving, onClick = {
            when {
                cow == null -> context.toast("Select a cow")
                litres <= 0 -> context.toast("Enter the litres")
                // stop duplicate entries for the same cow, date and session
                dayRecords.any { it.session == session } -> context.toast("$session milk for ${cow.name} is already recorded. Delete it below to correct it.")
                else -> {
                    saving = true
                    vm.addMilk(MilkRecord(cowId = cow.id, cowName = cow.name, date = date, session = session, litres = litres)) { error ->
                        saving = false
                        if (error == null) {
                            context.toast("Milk record saved")
                            onSaved(cow.id)
                        } else context.toast(error)
                    }
                }
            }
        })

        SectionTitle("Recent records")
        if (history.isEmpty()) EmptyText("No milk recorded yet")
        Row(Modifier.fillMaxWidth()) {
            listOf("Date", "Cow", "Session", "Litres").forEach {
                Text(it, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Muted, modifier = Modifier.weight(1f))
            }
            Box(Modifier.size(32.dp))
        }
        history.forEach { m ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(Dates.pretty(m.date), fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
                Text(m.cowName, fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
                Text(m.session, fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
                Text("${m.litres.clean()} L", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brown, modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.deleteMilk(m.id) { e -> context.toast(e ?: "Deleted") } }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Muted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = Brown) }
}
