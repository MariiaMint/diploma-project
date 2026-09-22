package frontend.modules.calendar

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mymedbook.R
import frontend.theme.ButtonColor
import frontend.theme.CardDark
import frontend.theme.Primary
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Suppress("unused")
@Composable
fun CalendarGrid() {
    CalendarGridWithCallback(
        eventDates = emptySet(),
        onDateSelected = {}
    )
}

@Composable
@Suppress("UNUSED_VALUE")
fun CalendarGridWithCallback(
    eventDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val showMonthPicker = remember { mutableStateOf(false) }
    val showYearPicker = remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val monthNames = remember {
        (1..12).map { month ->
            YearMonth.of(2026, month).month
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                .replaceFirstChar { it.uppercase() }
        }.toTypedArray()
    }

    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        .replaceFirstChar { it.uppercase() }

    if (showMonthPicker.value) {
        var pickedMonth by remember { mutableStateOf(currentMonth.monthValue) }
        AlertDialog(
            onDismissRequest = { showMonthPicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    currentMonth = YearMonth.of(currentMonth.year, pickedMonth)
                    showMonthPicker.value = false
                }) {
                    Text("Ок", color = SelectedColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthPicker.value = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            },
            title = { Text("Выберите месяц", color = Color.White) },
            text = {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
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
                        picker.value = pickedMonth
                        picker.setOnValueChangedListener { _, _, newValue -> pickedMonth = newValue }
                    }
                )
            },
            containerColor = CardDark
        )
    }

    if (showYearPicker.value) {
        var pickedYear by remember { mutableStateOf(currentMonth.year) }
        AlertDialog(
            onDismissRequest = { showYearPicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    currentMonth = YearMonth.of(pickedYear, currentMonth.monthValue)
                    showYearPicker.value = false
                }) {
                    Text("Ок", color = SelectedColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showYearPicker.value = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            },
            title = { Text("Выберите год", color = Color.White) },
            text = {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
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
                        picker.value = pickedYear.coerceIn(1900, 2100)
                        picker.setOnValueChangedListener { _, _, newValue -> pickedYear = newValue }
                    }
                )
            },
            containerColor = CardDark
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
            .border(width = 2.dp,color = SurfaceDark, shape = RoundedCornerShape(16.dp)),
    ) {
        // Заголовок с навигацией по месяцам
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Предыдущий месяц",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = monthName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.clickable { showMonthPicker.value = true }
                )
                Text(
                    text = currentMonth.year.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.clickable { showYearPicker.value = true }
                )
            }

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Следующий месяц",
                    tint = TextPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Дни недели
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SelectedColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Дни месяца
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.lengthOfMonth()
        // Понедельник = 1, Воскресенье = 7
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value

        val totalCells = ((startDayOfWeek - 1) + lastDayOfMonth + 6) / 7 * 7
        val weeks = totalCells / 7

        for (week in 0 until weeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 1..7) {
                    val dayIndex = week * 7 + dayOfWeek
                    val dayNumber = dayIndex - (startDayOfWeek - 1)

                    if (dayNumber in 1..lastDayOfMonth) {
                        val date = currentMonth.atDay(dayNumber)
                        val isSelected = selectedDate == date
                        val isToday = date == LocalDate.now()
                        val hasEvent = date in eventDates

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Primary
                                        isToday -> SelectedColor.copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    selectedDate = date
                                    onDateSelected(date)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ButtonColor else TextPrimary
                                )
                                // Точка-индикатор события
                                if (hasEvent && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(SelectedColor)
                                    )
                                }
                            }
                        }
                    } else {
                        // Пустая ячейка
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}
