package com.horos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horos.app.data.Expense
import com.horos.app.ui.theme.ChipBackground
import com.horos.app.ui.theme.ChipBorder
import com.horos.app.ui.theme.HorosBlue
import com.horos.app.ui.theme.PeriodCardBorder
import com.horos.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val ExpenseCategories = listOf(
    "Comida", "Transporte", "Ocio", "Fotocopias", "Supermercado", "Suscripciones"
)

private const val AMOUNT_STEP = 5.0
private const val NOTE_MAX_LENGTH = 50

// Register expense screen: keeps its own state and hands the finished Expense to onSave.
@Composable
fun RegisterExpenseScreen(
    onSave: (Expense) -> Unit,
    onBack: () -> Unit
) {
    var amount by rememberSaveable { mutableDoubleStateOf(0.0) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var note by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        RegisterHeader(onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AmountCard(amount = amount, onAmountChange = { amount = it.coerceAtLeast(0.0) })
            SectionLabel("CATEGORÍA")
            CategoryGrid(selected = category, onSelect = { category = it })
            SectionLabel("MÉTODO DE PAGO")
            PaymentMethodRow()
            DateRow()
            NoteField(note = note, onNoteChange = { note = it.take(NOTE_MAX_LENGTH) })
        }
        SaveButton(
            enabled = amount > 0 && category != null,
            onClick = {
                val time = SimpleDateFormat("HH:mm", Locale.US).format(Date())
                onSave(Expense(amount, category!!, note.trim(), time))
            }
        )
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "←",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.clickable(onClick = onBack).padding(8.dp)
        )
        Text("Registrar gasto", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
}

@Composable
private fun AmountCard(amount: Double, onAmountChange: (Double) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PeriodCardBorder, RoundedCornerShape(4.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionLabel("MONTO")
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton("−") { onAmountChange(amount - AMOUNT_STEP) }
            Text(
                formatMoney(amount),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            StepButton("+") { onAmountChange(amount + AMOUNT_STEP) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(25.0, 50.0, 100.0).forEach { shortcut ->
                ShortcutChip("+$${shortcut.toInt()}", Modifier.weight(1f)) {
                    onAmountChange(amount + shortcut)
                }
            }
            ShortcutChip("Reset", Modifier.weight(1f)) { onAmountChange(0.0) }
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(HorosBlue, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge, color = Color.White)
    }
}

@Composable
private fun ShortcutChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(ChipBackground, RoundedCornerShape(2.dp))
            .border(1.dp, ChipBorder, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun CategoryGrid(selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpenseCategories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { category ->
                    SelectableBox(
                        label = category,
                        selected = category == selected,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableBox(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(if (selected) HorosBlue else ChipBackground, RoundedCornerShape(4.dp))
            .border(1.dp, if (selected) HorosBlue else ChipBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun PaymentMethodRow() {
    var method by remember { mutableStateOf("Efectivo") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Efectivo", "Tarjeta", "Transf.").forEach { option ->
            SelectableBox(
                label = option,
                selected = option == method,
                modifier = Modifier.weight(1f),
                onClick = { method = option }
            )
        }
    }
}

@Composable
private fun DateRow() {
    val today = SimpleDateFormat("d 'de' MMMM yyyy", Locale.forLanguageTag("es-MX")).format(Date())
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel("FECHA")
        Spacer(Modifier.width(8.dp))
        Text("Hoy, $today", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NoteField(note: String, onNoteChange: (String) -> Unit) {
    Column {
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("Concepto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "${note.length}/$NOTE_MAX_LENGTH",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HorosBlue),
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
    ) {
        Text("Guardar gasto", style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}
