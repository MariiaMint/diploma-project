package frontend.modules.common

import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import frontend.theme.CardDark
import frontend.theme.SelectedColor
import frontend.theme.TextPrimary
import java.time.LocalDate
import java.time.YearMonth

private data class DateParts(
    val year: Int,
    val month: Int,
    val day: Int
)

private fun parseDateOrNow(value: String): DateParts {
    val parts = value.split(".")
    if (parts.size != 3) {
        val now = LocalDate.now()
        return DateParts(now.year, now.monthValue, now.dayOfMonth)
    }

    val day = parts[0].toIntOrNull()
    val month = parts[1].toIntOrNull()
    val year = parts[2].toIntOrNull()

    if (day == null || month == null || year == null) {
        val now = LocalDate.now()
        return DateParts(now.year, now.monthValue, now.dayOfMonth)
    }

    val safeMonth = month.coerceIn(1, 12)
    val safeYear = year.coerceIn(1900, 2100)
    val maxDay = YearMonth.of(safeYear, safeMonth).lengthOfMonth()
    val safeDay = day.coerceIn(1, maxDay)

    return DateParts(safeYear, safeMonth, safeDay)
}

private fun formatDate(parts: DateParts): String {
    return "%02d.%02d.%04d".format(parts.day, parts.month, parts.year)
}

@Composable
private fun DateScrollRow(
    label: String,
    initialValue: DateParts,
    onValueChange: (DateParts) -> Unit
) {
    var year by remember(label) { mutableIntStateOf(initialValue.year) }
    var month by remember(label) { mutableIntStateOf(initialValue.month) }
    var day by remember(label) { mutableIntStateOf(initialValue.day) }

    val daysInMonth = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    if (day > daysInMonth) {
        day = daysInMonth
    }

    onValueChange(DateParts(year = year, month = month, day = day))

    val monthNames = arrayOf(
        "Янв", "Фев", "Мар", "Апр", "Май", "Июн",
        "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = TextPrimary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AndroidView(
                modifier = Modifier.weight(1f),
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 1
                        maxValue = 31
                        wrapSelectorWheel = true
                    }
                },
                update = { picker ->
                    picker.minValue = 1
                    picker.maxValue = daysInMonth
                    picker.value = day.coerceIn(1, daysInMonth)
                    picker.setOnValueChangedListener { _, _, newValue -> day = newValue }
                }
            )

            AndroidView(
                modifier = Modifier.weight(1f),
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 1
                        maxValue = 12
                        displayedValues = monthNames
                        wrapSelectorWheel = true
                    }
                },
                update = { picker ->
                    picker.minValue = 1
                    picker.maxValue = 12
                    picker.displayedValues = null
                    picker.displayedValues = monthNames
                    picker.value = month.coerceIn(1, 12)
                    picker.setOnValueChangedListener { _, _, newValue -> month = newValue }
                }
            )

            AndroidView(
                modifier = Modifier.weight(1f),
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 1900
                        maxValue = 2100
                        wrapSelectorWheel = true
                    }
                },
                update = { picker ->
                    picker.minValue = 1900
                    picker.maxValue = 2100
                    picker.value = year.coerceIn(1900, 2100)
                    picker.setOnValueChangedListener { _, _, newValue -> year = newValue }
                }
            )
        }
    }
}

@Composable
fun DateRangeScrollFilterDialog(
    dateFrom: String,
    dateTo: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onClear: () -> Unit
) {
    val initialFrom = remember(dateFrom) { parseDateOrNow(dateFrom) }
    val initialTo = remember(dateTo) { parseDateOrNow(dateTo) }

    var currentFrom by remember { mutableStateOf(initialFrom) }
    var currentTo by remember { mutableStateOf(initialTo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val fromDate = LocalDate.of(currentFrom.year, currentFrom.month, currentFrom.day)
                val toDate = LocalDate.of(currentTo.year, currentTo.month, currentTo.day)

                val finalFrom: LocalDate
                val finalTo: LocalDate
                if (fromDate.isAfter(toDate)) {
                    finalFrom = toDate
                    finalTo = fromDate
                } else {
                    finalFrom = fromDate
                    finalTo = toDate
                }

                onConfirm(
                    formatDate(DateParts(finalFrom.year, finalFrom.monthValue, finalFrom.dayOfMonth)),
                    formatDate(DateParts(finalTo.year, finalTo.monthValue, finalTo.dayOfMonth))
                )
            }) {
                Text("Применить", color = SelectedColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onClear) {
                Text("Очистить", color = Color.Gray)
            }
        },
        title = { Text("Фильтр по дате", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DateScrollRow(
                    label = "От",
                    initialValue = initialFrom,
                    onValueChange = { currentFrom = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                DateScrollRow(
                    label = "До",
                    initialValue = initialTo,
                    onValueChange = { currentTo = it }
                )
            }
        },
        containerColor = CardDark,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}


