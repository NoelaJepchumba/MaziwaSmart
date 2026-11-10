package com.maziwasmart.app.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.FieldBg
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.GreenSoft
import com.maziwasmart.app.ui.theme.LineColor
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.Orange
import com.maziwasmart.app.ui.theme.Yellow
import com.maziwasmart.app.ui.theme.YellowSoft

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

// ---------------------------------------------------------------------------
// Screen layout: green header on top + white rounded "sheet" with the content
// ---------------------------------------------------------------------------
@Composable
fun ScreenWithHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    headerExtra: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(GreenDeep)) {
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 22.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) HeaderIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                Spacer(Modifier.weight(1f))
                actions()
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            headerExtra()
        }
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun HeaderIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = description, tint = Color.White) }
}

@Composable
fun Avatar(initials: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Yellow).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(initials, color = Brown, fontWeight = FontWeight.Bold) }
}

// ---------------------------------------------------------------------------
// Cards and text
// ---------------------------------------------------------------------------
@Composable
fun StatCard(value: String, label: String, background: Color, textColor: Color, modifier: Modifier = Modifier, valueColor: Color = textColor) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(background).padding(14.dp)) {
        Text(value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = textColor.copy(alpha = 0.85f), fontSize = 11.sp)
    }
}

@Composable
fun MiniStat(value: String, label: String, background: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(background).padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Brown)
        Text(label, fontSize = 11.sp, color = Muted)
    }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, background: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    val base = modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(background)
    Column(
        (if (background == Color.White) base.border(1.dp, LineColor, RoundedCornerShape(18.dp)) else base).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Brown, modifier = Modifier.weight(1f))
        if (action != null) Text(action, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GreenDeep, modifier = Modifier.clickable(onClick = onAction))
    }
}

@Composable
fun Pill(text: String, background: Color, textColor: Color) {
    Text(
        text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(background).padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun Banner(text: String, icon: ImageVector, background: Color, iconTint: Color, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Text(text, fontSize = 12.sp, color = Brown, modifier = Modifier.weight(1f))
    }
}

/** Shown on every AI screen so it is clear the numbers are not real yet */
@Composable
fun DemoBanner() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(YellowSoft).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("DEMO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brown,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Yellow).padding(horizontal = 6.dp, vertical = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text("AI model not connected yet - showing dummy data", fontSize = 11.sp, color = Brown)
    }
}

@Composable
fun ListRow(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brown)
            Text(subtitle, fontSize = 11.sp, color = Muted)
        }
        trailing()
    }
}

@Composable
fun EmptyText(text: String) {
    Text(text, fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Yellow,
    textColor: Color = Brown,
    loading: Boolean = false,
    small: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier.fillMaxWidth().height(if (small) 42.dp else 52.dp),
        shape = RoundedCornerShape(if (small) 12.dp else 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = textColor, disabledContainerColor = background.copy(alpha = 0.6f))
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = textColor, strokeWidth = 2.dp)
        else Text(text, fontSize = if (small) 13.sp else 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AppOutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, textColor: Color = Brown) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, LineColor)
    ) { Text(text, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
}

// ---------------------------------------------------------------------------
// Form fields
// ---------------------------------------------------------------------------
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    leadingIcon: ImageVector? = null,
    suffix: String? = null,
    singleLine: Boolean = true
) {
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null) }
        } else null,
        suffix = if (suffix != null) {
            { Text(suffix, color = GreenDeep, fontWeight = FontWeight.SemiBold) }
        } else null,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Show password")
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenDeep,
            unfocusedBorderColor = LineColor,
            focusedLabelColor = GreenDeep,
            unfocusedContainerColor = FieldBg,
            focusedContainerColor = Color.White
        )
    )
}

/** A field that opens a list to pick from (used for cow, breed, supplement...) */
@Composable
fun DropdownField(label: String, options: List<String>, selected: String, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = LineColor, unfocusedContainerColor = FieldBg)
        )
        // invisible box on top so tapping anywhere opens the list
        Box(Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(index)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brown)
}

/** Row of pill buttons where one is selected (feed type, session...) */
@Composable
fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val on = option == selected
            Text(
                option,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (on) Yellow else Muted,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(if (on) Brown else FieldBg)
                    .clickable { onSelect(option) }.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/** Segmented control (Morning | Afternoon | Evening) */
@Composable
fun Segmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FieldBg).padding(4.dp)) {
        options.forEach { option ->
            val on = option == selected
            Text(
                option,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                color = if (on) Brown else Muted,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (on) Color.White else Color.Transparent)
                    .clickable { onSelect(option) }.padding(vertical = 9.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Simple charts drawn with Canvas
// ---------------------------------------------------------------------------
@Composable
fun LineChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = GreenDeep,
    secondValues: List<Double>? = null,
    secondColor: Color = Orange
) {
    val all = values + (secondValues ?: emptyList())
    val max = (all.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    val min = (all.filter { it > 0 }.minOrNull() ?: 0.0) * 0.8
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(90.dp)) {
            fun point(i: Int, v: Double, count: Int): Offset {
                val x = if (count <= 1) 0f else size.width * i / (count - 1)
                val y = size.height - ((v - min) / (max - min)).toFloat().coerceIn(0f, 1f) * size.height * 0.9f
                return Offset(x, y)
            }
            fun drawSeries(series: List<Double>, color: Color, dashed: Boolean) {
                val path = Path()
                series.forEachIndexed { i, v ->
                    val p = point(i, v, series.size)
                    if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
                drawPath(
                    path, color,
                    style = Stroke(width = 6f, pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null)
                )
                if (series.isNotEmpty()) drawCircle(color, 8f, point(series.lastIndex, series.last(), series.size))
            }
            drawLine(LineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
            drawSeries(values, lineColor, dashed = false)
            if (secondValues != null) drawSeries(secondValues, secondColor, dashed = true)
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, fontSize = 10.sp, color = Muted) }
        }
    }
}

@Composable
fun BarChart(values: List<Double>, labels: List<String>, modifier: Modifier = Modifier, colors: List<Color>? = null) {
    val max = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    Row(modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, v ->
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(Modifier.weight(1f, fill = true).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier.fillMaxWidth().fillMaxHeight((v / max).toFloat().coerceIn(0.03f, 1f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(colors?.getOrNull(i) ?: if (i == values.lastIndex) GreenDeep else GreenSoft)
                    )
                }
                Text(labels.getOrElse(i) { "" }, fontSize = 9.sp, color = Muted, maxLines = 1)
            }
        }
    }
}

@Composable
fun ScatterChart(points: List<Pair<Double, Double>>, modifier: Modifier = Modifier, highlight: Set<Int> = emptySet()) {
    if (points.isEmpty()) return
    val minX = points.minOf { it.first }; val maxX = points.maxOf { it.first }
    val minY = points.minOf { it.second }; val maxY = points.maxOf { it.second }
    Canvas(modifier.fillMaxWidth().height(120.dp)) {
        fun pos(x: Double, y: Double) = Offset(
            (((x - minX) / (maxX - minX).coerceAtLeast(0.1)) * size.width * 0.92 + size.width * 0.04).toFloat(),
            (size.height - ((y - minY) / (maxY - minY).coerceAtLeast(0.1)) * size.height * 0.85 - size.height * 0.07).toFloat()
        )
        drawLine(LineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
        drawLine(LineColor, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2f)
        drawLine(Brown.copy(alpha = 0.4f), pos(minX, minY), pos(maxX, maxY), strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))
        points.forEachIndexed { i, (x, y) -> drawCircle(if (i in highlight) Orange else GreenDeep.copy(alpha = 0.8f), 10f, pos(x, y)) }
    }
}

/** Horizontal bar for comparisons (e.g. supplement vs no supplement) */
@Composable
fun ProgressBar(fraction: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(FieldBg)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(color))
    }
}
