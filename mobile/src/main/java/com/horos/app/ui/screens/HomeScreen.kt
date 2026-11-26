package com.horos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horos.app.data.Expense
import com.horos.app.ui.theme.ChipBackground
import com.horos.app.ui.theme.ChipBorder
import com.horos.app.ui.theme.ExpenseListBorder
import com.horos.app.ui.theme.HorosBlue
import com.horos.app.ui.theme.HorosCyan
import com.horos.app.ui.theme.HorosLightCyan
import com.horos.app.ui.theme.HorosNavy
import com.horos.app.ui.theme.HorosSyncDot
import com.horos.app.ui.theme.PeriodCardBorder
import com.horos.app.ui.theme.StatusOk
import com.horos.app.ui.theme.StatusOver
import com.horos.app.ui.theme.StatusWarning
import com.horos.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Home screen: receives the state from MainActivity and only draws it.
@Composable
fun HomeScreen(
    limit: Double,
    spent: Double,
    expenses: List<Expense>,
    onRegisterClick: () -> Unit
) {
    Scaffold(
        topBar = { HomeHeader() },
        bottomBar = { BottomBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Greeting() }
            item { BalanceCard(limit, spent) }
            item { PeriodCards(spent) }
            item { CategoryChips(expenses) }
            item { ActionButtons(onRegisterClick) }
            item { TodayExpensesList(expenses) }
        }
    }
}

@Composable
private fun HomeHeader() {
    Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(62.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("HOROS+", style = MaterialTheme.typography.titleMedium, color = HorosNavy)
            Text(" • Inicio", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(6.dp).background(HorosSyncDot, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text("SINCRONIZADO", style = MaterialTheme.typography.labelSmall, color = HorosSyncDot)
        }
    }
}

@Composable
private fun Greeting() {
    val date = SimpleDateFormat("EEEE d 'de' MMMM", Locale.forLanguageTag("es-MX"))
        .format(Date())
        .uppercase()
    Column {
        Text(date, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text("Hola, Alexis", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun BalanceCard(limit: Double, spent: Double) {
    val remaining = limit - spent
    val available = if (limit > 0) (remaining / limit).coerceIn(0.0, 1.0) else 0.0
    val status = budgetStatus(spent, limit)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HorosNavy, RoundedCornerShape(4.dp))
            .padding(24.dp)
    ) {
        Text("TE QUEDAN HOY", style = MaterialTheme.typography.labelLarge, color = HorosCyan)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(formatMoney(remaining), style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(Modifier.width(6.dp))
            Text(
                "MXN",
                style = MaterialTheme.typography.labelLarge,
                color = HorosCyan,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Text(
            "de ${formatMoney(limit)} presupuesto diario base",
            style = MaterialTheme.typography.bodySmall,
            color = HorosLightCyan.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(available.toFloat())
                    .background(status.color)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${(available * 100).toInt()}% disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                Text(
                    "Gastado hoy ${formatMoney(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HorosLightCyan.copy(alpha = 0.8f)
                )
            }
            StatusTag(status)
        }
    }
}

@Composable
private fun StatusTag(status: BudgetStatus) {
    Text(
        status.label,
        style = MaterialTheme.typography.labelSmall,
        color = status.color,
        modifier = Modifier
            .background(status.color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            .border(1.dp, status.color.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun PeriodCards(spentToday: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        PeriodCard("DÍA", formatMoney(spentToday), Modifier.weight(1f))
        PeriodCard("SEMANA", "—", Modifier.weight(1f))
        PeriodCard("MES", "—", Modifier.weight(1f))
    }
}

@Composable
private fun PeriodCard(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(72.dp)
            .border(1.dp, PeriodCardBorder, RoundedCornerShape(4.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(expenses: List<Expense>) {
    if (expenses.isEmpty()) return
    val totals = expenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        totals.forEach { (category, total) ->
            Text(
                "$category ${formatMoney(total)}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(ChipBackground, RoundedCornerShape(2.dp))
                    .border(1.dp, ChipBorder, RoundedCornerShape(2.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ActionButtons(onRegisterClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onRegisterClick,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = HorosBlue),
            modifier = Modifier.weight(1f).height(40.dp)
        ) {
            Text("Registrar gasto", style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
        OutlinedButton(
            onClick = {},
            shape = shape,
            modifier = Modifier.weight(1f).height(40.dp)
        ) {
            Text("Ver resumen", style = MaterialTheme.typography.labelMedium, color = HorosBlue)
        }
    }
}

@Composable
private fun TodayExpensesList(expenses: List<Expense>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ExpenseListBorder, RoundedCornerShape(4.dp))
    ) {
        Text(
            "GASTOS DE HOY",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(16.dp)
        )
        if (expenses.isEmpty()) {
            Text(
                "Todavía no registras gastos hoy",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
        // Most recent first.
        expenses.asReversed().forEach { expense ->
            HorizontalDivider(color = ExpenseListBorder)
            ExpenseRow(expense)
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(ChipBackground, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(expense.category.take(1), style = MaterialTheme.typography.titleSmall, color = HorosBlue)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                expense.note.ifBlank { expense.category },
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "${expense.category} • ${expense.time}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text("-${formatMoney(expense.amount)}", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun BottomBar() {
    Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Inicio", "Análisis", "Recordatorios", "Límites").forEachIndexed { index, label ->
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == 0) HorosBlue else TextSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {}
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class BudgetStatus(val label: String, val color: Color)

// Status by percentage used: <80% on track, 80–100% near the limit, >100% exceeded.
fun budgetStatus(spent: Double, limit: Double): BudgetStatus {
    val used = if (limit > 0) spent / limit else 1.0
    return when {
        used < 0.8 -> BudgetStatus("AL DÍA", StatusOk)
        used <= 1.0 -> BudgetStatus("CERCA DEL LÍMITE", StatusWarning)
        else -> BudgetStatus("EXCEDIDO", StatusOver)
    }
}

// Formats as $1,234.00; negative values put the sign before the $.
fun formatMoney(value: Double): String {
    val text = String.format(Locale.US, "$%,.2f", abs(value))
    return if (value < 0) "-$text" else text
}
