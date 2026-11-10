package com.maziwasmart.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.data.Dates
import com.maziwasmart.app.data.Expense
import com.maziwasmart.app.data.ExpenseCategories
import com.maziwasmart.app.data.MilkSale
import com.maziwasmart.app.data.clean
import com.maziwasmart.app.data.ksh
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.components.AppButton
import com.maziwasmart.app.ui.components.AppCard
import com.maziwasmart.app.ui.components.AppOutlineButton
import com.maziwasmart.app.ui.components.AppTextField
import com.maziwasmart.app.ui.components.ChipRow
import com.maziwasmart.app.ui.components.EmptyText
import com.maziwasmart.app.ui.components.ListRow
import com.maziwasmart.app.ui.components.ProgressBar
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.Segmented
import com.maziwasmart.app.ui.components.toast
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.OrangeSoft
import com.maziwasmart.app.ui.theme.OrangeText
import com.maziwasmart.app.ui.theme.YellowSoft
import com.maziwasmart.app.ui.theme.YellowText

@Composable
fun FinanceScreen(vm: FarmViewModel, onBack: () -> Unit) {
    val expenses by vm.expenses.collectAsState()
    val sales by vm.sales.collectAsState()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf("Expenses") }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showSaleDialog by remember { mutableStateOf(false) }

    val monthExpenses = expenses.filter { Dates.isThisMonth(it.date) }
    val monthSales = sales.filter { Dates.isThisMonth(it.date) }
    val totalExpenses = monthExpenses.sumOf { it.amount }
    val revenue = monthSales.sumOf { it.total }
    val profit = revenue - totalExpenses
    val litresSold = monthSales.sumOf { it.litres }

    ScreenWithHeader(
        title = "Expenses & Revenue",
        onBack = onBack,
        headerExtra = {
            Text("Estimated profit this month", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
            Text(profit.ksh(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppCard(Modifier.weight(1f), background = GreenSoft) {
                Text("Milk revenue", fontSize = 11.sp, color = Muted)
                Text(revenue.ksh(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                Text("${litresSold.clean()} L sold", fontSize = 11.sp, color = GreenDeep)
            }
            AppCard(Modifier.weight(1f), background = OrangeSoft) {
                Text("Expenses", fontSize = 11.sp, color = Muted)
                Text(totalExpenses.ksh(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                Text("${monthExpenses.size} entries", fontSize = 11.sp, color = OrangeText)
            }
        }

        Segmented(listOf("Expenses", "Milk sales"), tab) { tab = it }

        if (tab == "Expenses") {
            // breakdown by category
            if (monthExpenses.isNotEmpty()) {
                AppCard {
                    monthExpenses.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }
                        .entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                            Row {
                                Text(category, fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
                                Text("${(amount / totalExpenses * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brown)
                            }
                            ProgressBar((amount / totalExpenses).toFloat(), GreenDeep)
                        }
                }
            }
            if (expenses.isEmpty()) EmptyText("No expenses recorded yet")
            expenses.sortedByDescending { it.date + it.createdAt }.take(30).forEach { e ->
                ListRow(
                    if (e.category == "Feed") Icons.Filled.Grass else Icons.Filled.Receipt,
                    OrangeSoft, OrangeText,
                    e.description.ifBlank { e.category },
                    "${e.category} · ${Dates.pretty(e.date)}"
                ) {
                    Text("– ${e.amount.ksh()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brown)
                    IconButton(onClick = { vm.deleteExpense(e.id) { err -> context.toast(err ?: "Deleted") } }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Muted)
                    }
                }
            }
        } else {
            if (sales.isEmpty()) EmptyText("No milk sales recorded yet")
            sales.sortedByDescending { it.date + it.createdAt }.take(30).forEach { s ->
                ListRow(Icons.Filled.Payments, YellowSoft, YellowText, "Milk sold · ${s.litres.clean()} L",
                    "${Dates.pretty(s.date)} · KSh ${s.pricePerLitre.clean()} per litre") {
                    Text("+ ${s.total.ksh()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GreenDeep)
                    IconButton(onClick = { vm.deleteSale(s.id) { err -> context.toast(err ?: "Deleted") } }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Muted)
                    }
                }
            }
        }

        Text("Estimate based only on recorded income and costs.", fontSize = 10.sp, color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppOutlineButton("Record sale", { showSaleDialog = true }, Modifier.weight(1f))
            AppButton("Add expense", { showExpenseDialog = true }, Modifier.weight(1f), small = true)
        }
    }

    if (showExpenseDialog) AddExpenseDialog(
        onDismiss = { showExpenseDialog = false },
        onSave = { expense ->
            vm.addExpense(expense) { err -> context.toast(err ?: "Expense saved") }
            showExpenseDialog = false
        }
    )
    if (showSaleDialog) AddSaleDialog(
        onDismiss = { showSaleDialog = false },
        onSave = { sale ->
            vm.addSale(sale) { err -> context.toast(err ?: "Sale saved") }
            showSaleDialog = false
        }
    )
}

@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (Expense) -> Unit) {
    var category by remember { mutableStateOf(ExpenseCategories.all.first()) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ChipRow(ExpenseCategories.all, category) { category = it }
                AppTextField(description, { description = it }, "Description (e.g. Dairy meal 70 kg)")
                AppTextField(amount, { amount = it }, "Amount", keyboardType = KeyboardType.Decimal, suffix = "KSh")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toDoubleOrNull()
                if (value == null || value <= 0) context.toast("Enter the amount")
                else onSave(Expense(category = category, description = description.trim(), amount = value, date = Dates.today()))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddSaleDialog(onDismiss: () -> Unit, onSave: (MilkSale) -> Unit) {
    var litres by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("50") }
    val context = LocalContext.current
    val total = (litres.toDoubleOrNull() ?: 0.0) * (price.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record milk sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(litres, { litres = it }, "Litres sold", keyboardType = KeyboardType.Decimal, suffix = "L")
                AppTextField(price, { price = it }, "Price per litre", keyboardType = KeyboardType.Decimal, suffix = "KSh")
                Text("Total: ${total.ksh()}", fontWeight = FontWeight.SemiBold, color = GreenDeep)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val l = litres.toDoubleOrNull()
                val p = price.toDoubleOrNull()
                if (l == null || l <= 0 || p == null || p <= 0) context.toast("Enter litres and price")
                else onSave(MilkSale(litres = l, pricePerLitre = p, date = Dates.today()))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
