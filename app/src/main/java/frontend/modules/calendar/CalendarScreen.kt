package frontend.modules.calendar

import android.widget.NumberPicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mymedbook.R
import frontend.modules.BackgroundScreen
import frontend.modules.bars.ExpandableHeader
import frontend.modules.common.ScrollbarLazyColumn
import frontend.modules.common.DateRangeScrollFilterDialog
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.modules.common.medicalIconTintFor
import frontend.theme.ArianFontFamily
import frontend.theme.ButtonColor
import frontend.theme.CardDark
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

private enum class CalendarEntryType {
    Event,
    Reminder
}

private enum class NotificationMode {
    BeforeHours,
    AtTime
}

private data class CalendarEntry(
    val type: CalendarEntryType,
    val title: String,
    val time: String? = null,
    val notificationText: String? = null
)

private val calendarDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale("ru")).withResolverStyle(ResolverStyle.STRICT)

private fun parseCalendarDate(value: String): LocalDate? {
    if (value.isBlank()) return null
    return runCatching { LocalDate.parse(value, calendarDateFormatter) }.getOrNull()
}

private fun parseTimeOrDefault(value: String, defaultHour: Int = 8, defaultMinute: Int = 0): Pair<Int, Int> {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: defaultHour
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: defaultMinute
    return hour to minute
}

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.US, "%02d:%02d", hour, minute)

@Composable
fun CalendarScreen(navController: NavController) {
    navController.hashCode()
    BackgroundScreen {
        // Выбранная дата (синхронизируется с CalendarGrid)
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }

        // События/напоминания по датам
        val events = remember {
            mutableStateMapOf<LocalDate, List<CalendarEntry>>().apply {
                fun addEntry(
                    date: LocalDate,
                    type: CalendarEntryType,
                    title: String,
                    time: String? = null,
                    notificationText: String? = null
                ) {
                    this[date] =
                        this[date].orEmpty() + CalendarEntry(type, title, time, notificationText)
                }

                listOf(
                    Triple(
                        LocalDate.of(2026, 4, 5),
                        CalendarEntryType.Event,
                        "Прием у кардиолога в 10:00"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 5),
                        CalendarEntryType.Reminder,
                        "Сдать анализ крови"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 12),
                        CalendarEntryType.Event,
                        "УЗИ органов брюшной полости"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 15),
                        CalendarEntryType.Reminder,
                        "Принять таблетки"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 15),
                        CalendarEntryType.Event,
                        "Визит к терапевту в 14:30"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 20),
                        CalendarEntryType.Reminder,
                        "Измерить давление"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 25),
                        CalendarEntryType.Event,
                        "Прием у невролога в 11:00"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 25),
                        CalendarEntryType.Event,
                        "Прием у невролога в 12:00"
                    ),
                    Triple(
                        LocalDate.of(2026, 4, 25),
                        CalendarEntryType.Event,
                        "Прием у невролога в 13:00"
                    )
                ).forEach { (date, type, title) -> addEntry(date, type, title) }
            }
        }

        val eventsForSelectedDate = events[selectedDate].orEmpty()
        val eventDates = events.keys

        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        val addDateFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale("ru"))

        var showAddDialog by remember { mutableStateOf(false) }
        var showReminderRangePicker by remember { mutableStateOf(false) }
        var pendingDeleteTarget by remember { mutableStateOf<Pair<LocalDate, Int>?>(null) }
        var newEntryType by remember { mutableStateOf(CalendarEntryType.Event) }
        var newEntryTitle by remember { mutableStateOf("") }
        var newEntryDateText by remember { mutableStateOf(selectedDate.format(addDateFormatter)) }
        var newEntryTimeText by remember { mutableStateOf("") }
        var reminderRangeFromText by remember { mutableStateOf(selectedDate.format(addDateFormatter)) }
        var reminderRangeToText by remember { mutableStateOf(selectedDate.format(addDateFormatter)) }
        var notifyEnabled by remember { mutableStateOf(false) }
        var notificationMode by remember { mutableStateOf(NotificationMode.BeforeHours) }
        var notifyBeforeHoursText by remember { mutableStateOf("2") }
        var notifyAtTimeText by remember { mutableStateOf("08:00") }
        var showEntryTimePicker by remember { mutableStateOf(false) }
        var showNotifyAtTimePicker by remember { mutableStateOf(false) }
        var showBeforeDurationPicker by remember { mutableStateOf(false) }
        var addDialogError by remember { mutableStateOf<String?>(null) }

        val hasEntryTime = newEntryTimeText.isNotBlank()

        LaunchedEffect(hasEntryTime, notifyEnabled, notificationMode) {
            if (notifyEnabled && !hasEntryTime && notificationMode == NotificationMode.BeforeHours) {
                notificationMode = NotificationMode.AtTime
            }
        }

        if (showEntryTimePicker) {
            var pickedHour by remember(showEntryTimePicker) {
                mutableStateOf(
                    parseTimeOrDefault(
                        newEntryTimeText
                    ).first
                )
            }
            var pickedMinute by remember(showEntryTimePicker) {
                mutableStateOf(
                    parseTimeOrDefault(
                        newEntryTimeText
                    ).second
                )
            }

            AlertDialog(
                onDismissRequest = { showEntryTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        newEntryTimeText = formatTime(pickedHour, pickedMinute)
                        showEntryTimePicker = false
                    }) {
                        Text("Ок", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEntryTimePicker = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("Время записи", color = Color.White) },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 23
                                    wrapSelectorWheel = true
                                }
                            },
                            update = { picker ->
                                picker.minValue = 0
                                picker.maxValue = 23
                                picker.value = pickedHour
                                picker.setOnValueChangedListener { _, _, newValue ->
                                    pickedHour = newValue
                                }
                            }
                        )
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 59
                                    wrapSelectorWheel = true
                                }
                            },
                            update = { picker ->
                                picker.minValue = 0
                                picker.maxValue = 59
                                picker.value = pickedMinute
                                picker.setOnValueChangedListener { _, _, newValue ->
                                    pickedMinute = newValue
                                }
                            }
                        )
                    }
                },
                containerColor = CardDark
            )
        }

        if (showNotifyAtTimePicker) {
            var pickedHour by remember(showNotifyAtTimePicker) {
                mutableStateOf(
                    parseTimeOrDefault(
                        notifyAtTimeText
                    ).first
                )
            }
            var pickedMinute by remember(showNotifyAtTimePicker) {
                mutableStateOf(
                    parseTimeOrDefault(
                        notifyAtTimeText
                    ).second
                )
            }

            AlertDialog(
                onDismissRequest = { showNotifyAtTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        notifyAtTimeText = formatTime(pickedHour, pickedMinute)
                        showNotifyAtTimePicker = false
                    }) {
                        Text("Ок", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotifyAtTimePicker = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("Время уведомления", color = Color.White) },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 23
                                    wrapSelectorWheel = true
                                }
                            },
                            update = { picker ->
                                picker.minValue = 0
                                picker.maxValue = 23
                                picker.value = pickedHour
                                picker.setOnValueChangedListener { _, _, newValue ->
                                    pickedHour = newValue
                                }
                            }
                        )
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 59
                                    wrapSelectorWheel = true
                                }
                            },
                            update = { picker ->
                                picker.minValue = 0
                                picker.maxValue = 59
                                picker.value = pickedMinute
                                picker.setOnValueChangedListener { _, _, newValue ->
                                    pickedMinute = newValue
                                }
                            }
                        )
                    }
                },
                containerColor = CardDark
            )
        }

        if (showBeforeDurationPicker) {
            var pickedHours by remember(showBeforeDurationPicker) {
                mutableStateOf(notifyBeforeHoursText.toIntOrNull()?.coerceIn(0, 23) ?: 2)
            }
            var pickedMinutes by remember(showBeforeDurationPicker) {
                mutableStateOf(0)
            }

            AlertDialog(
                onDismissRequest = { showBeforeDurationPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        notifyBeforeHoursText = pickedHours.toString()
                        notifyAtTimeText = formatTime(pickedHours, pickedMinutes)
                        showBeforeDurationPicker = false
                    }) {
                        Text("Ок", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBeforeDurationPicker = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("За сколько уведомить", color = Color.White) },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 23
                                    wrapSelectorWheel = true
                                }
                            },
                            update = { picker ->
                                picker.minValue = 0
                                picker.maxValue = 23
                                picker.value = pickedHours
                                picker.setOnValueChangedListener { _, _, newValue ->
                                    pickedHours = newValue
                                }
                            }
                        )
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { ctx ->
                                NumberPicker(ctx).apply {
                                    minValue = 0
                                    maxValue = 59
                                    wrapSelectorWheel = true
                                }
                            },
                            update = { picker ->
                                picker.minValue = 0
                                picker.maxValue = 59
                                picker.value = pickedMinutes
                                picker.setOnValueChangedListener { _, _, newValue ->
                                    pickedMinutes = newValue
                                }
                            }
                        )
                    }
                },
                containerColor = CardDark
            )
        }

        fun addEntry(
            date: LocalDate,
            type: CalendarEntryType,
            title: String,
            time: String? = null,
            notificationText: String? = null
        ) {
            events[date] =
                events[date].orEmpty() + CalendarEntry(type, title, time, notificationText)
        }

        fun addReminderRange(
            fromText: String,
            toText: String,
            title: String,
            time: String? = null,
            notificationText: String? = null
        ) {
            val fromDate = parseCalendarDate(fromText) ?: return
            val toDate = parseCalendarDate(toText) ?: return
            val start = minOf(fromDate, toDate)
            val end = maxOf(fromDate, toDate)
            var current = start
            while (!current.isAfter(end)) {
                addEntry(current, CalendarEntryType.Reminder, title, time, notificationText)
                current = current.plusDays(1)
            }
        }

        fun removeEntryAt(date: LocalDate, index: Int) {
            val current = events[date].orEmpty()
            if (index !in current.indices) return
            val updated = current.toMutableList().apply { removeAt(index) }
            if (updated.isEmpty()) {
                events.remove(date)
            } else {
                events[date] = updated
            }
        }

        if (showReminderRangePicker) {
            DateRangeScrollFilterDialog(
                dateFrom = reminderRangeFromText,
                dateTo = reminderRangeToText,
                onDismiss = { showReminderRangePicker = false },
                onConfirm = { from, to ->
                    reminderRangeFromText = from
                    reminderRangeToText = to
                    showReminderRangePicker = false
                },
                onClear = {
                    reminderRangeFromText = selectedDate.format(addDateFormatter)
                    reminderRangeToText = selectedDate.format(addDateFormatter)
                    showReminderRangePicker = false
                }
            )
        }

        if (pendingDeleteTarget != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteTarget = null },
                confirmButton = {
                    TextButton(onClick = {
                        val target = pendingDeleteTarget
                        if (target != null) {
                            removeEntryAt(target.first, target.second)
                        }
                        pendingDeleteTarget = null
                    }) {
                        Text("Удалить", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteTarget = null }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("Удалить запись", color = Color.White) },
                text = { Text("Точно ли вы хотите удалить запись?", color = TextPrimary) },
                containerColor = CardDark
            )
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        addDialogError = null
                        val title = newEntryTitle.trim()
                        if (title.isBlank()) {
                            addDialogError = "Введите название"
                            return@TextButton
                        }

                        val time = newEntryTimeText.trim().ifBlank { null }
                        if (notifyEnabled && notificationMode == NotificationMode.BeforeHours && time == null) {
                            addDialogError = "Для уведомления за время укажите время записи"
                            return@TextButton
                        }

                        val notificationText = if (!notifyEnabled) {
                            null
                        } else {
                            when (notificationMode) {
                                NotificationMode.BeforeHours -> {
                                    val hours =
                                        notifyBeforeHoursText.toIntOrNull()?.coerceAtLeast(0) ?: 2
                                    val mm = parseTimeOrDefault(notifyAtTimeText).second
                                    if (mm > 0) "уведомление за $hours ч $mm мин" else "уведомление за $hours ч"
                                }

                                NotificationMode.AtTime -> {
                                    val notifyTime = notifyAtTimeText.trim().ifBlank { "08:00" }
                                    "уведомление в $notifyTime"
                                }
                            }
                        }

                        when (newEntryType) {
                            CalendarEntryType.Event -> {
                                val date = parseCalendarDate(newEntryDateText) ?: selectedDate
                                addEntry(
                                    date,
                                    CalendarEntryType.Event,
                                    title,
                                    time,
                                    notificationText
                                )
                            }

                            CalendarEntryType.Reminder -> {
                                addReminderRange(
                                    reminderRangeFromText,
                                    reminderRangeToText,
                                    title,
                                    time,
                                    notificationText
                                )
                            }
                        }

                        showAddDialog = false
                    }) {
                        Text("Добавить", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("Добавить запись", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newEntryType == CalendarEntryType.Event,
                                onClick = { newEntryType = CalendarEntryType.Event }
                            )
                            Text("Событие", color = TextPrimary)

                            Spacer(modifier = Modifier.width(12.dp))

                            RadioButton(
                                selected = newEntryType == CalendarEntryType.Reminder,
                                onClick = { newEntryType = CalendarEntryType.Reminder }
                            )
                            Text("Напоминание", color = TextPrimary)
                        }

                        TextField(
                            value = newEntryTitle,
                            onValueChange = { newEntryTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Название") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedPlaceholderColor = TextSecondary,
                                unfocusedPlaceholderColor = TextSecondary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEntryTimePicker = true },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, SurfaceDark)
                            ) {
                                Text(
                                    text = if (newEntryTimeText.isBlank()) "Выбрать время" else newEntryTimeText,
                                    color = TextPrimary
                                )
                            }

                            if (hasEntryTime) {
                                OutlinedButton(
                                    onClick = { newEntryTimeText = "" },
                                    border = BorderStroke(1.dp, SurfaceDark)
                                ) {
                                    Text("Очистить время", color = TextPrimary)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = notifyEnabled,
                                onCheckedChange = { notifyEnabled = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SelectedColor,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text("Добавить уведомление", color = TextPrimary)
                        }

                        if (notifyEnabled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = notificationMode == NotificationMode.BeforeHours,
                                    onClick = {
                                        if (hasEntryTime) notificationMode =
                                            NotificationMode.BeforeHours
                                    },
                                    enabled = hasEntryTime
                                )
                                Text(
                                    "За N часов",
                                    color = if (hasEntryTime) TextPrimary else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { showBeforeDurationPicker = true },
                                    enabled = hasEntryTime,
                                    border = BorderStroke(1.dp, SurfaceDark)
                                ) {
                                    val pickedMinutes = parseTimeOrDefault(notifyAtTimeText).second
                                    Text(
                                        text = "${notifyBeforeHoursText} ч ${pickedMinutes} мин",
                                        color = if (hasEntryTime) TextPrimary else TextSecondary
                                    )
                                }
                            }

                            if (!hasEntryTime) {
                                Text(
                                    text = "Чтобы уведомлять за время, сначала укажите время записи",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = notificationMode == NotificationMode.AtTime,
                                    onClick = { notificationMode = NotificationMode.AtTime }
                                )
                                Text("уведомление в", color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { showNotifyAtTimePicker = true },
                                    border = BorderStroke(1.dp, SurfaceDark)
                                ) {
                                    Text(notifyAtTimeText, color = TextPrimary)
                                }
                            }
                        }

                        addDialogError?.let {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = Color(0xFFE57373)
                            )
                        }

                        if (newEntryType == CalendarEntryType.Event) {
                            TextField(
                                value = newEntryDateText,
                                onValueChange = { newEntryDateText = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Дата (дд.мм.гггг)") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedPlaceholderColor = TextSecondary,
                                    unfocusedPlaceholderColor = TextSecondary
                                )
                            )
                        } else {
                            Text(
                                text = "Диапазон: $reminderRangeFromText — $reminderRangeToText",
                                fontSize = 14.sp,
                                color = TextPrimary
                            )

                            OutlinedButton(
                                onClick = { showReminderRangePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, SurfaceDark)
                            ) {
                                Text("Выбрать диапазон", color = TextPrimary)
                            }
                        }
                    }
                },
                containerColor = CardDark
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Выдвижной хедер поверх контента
            ExpandableHeader(title = "Календарь")

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)

            ) {
                Spacer(modifier = Modifier.height(36.dp))

                // Календарь
                CalendarGridWithCallback(
                    eventDates = eventDates,
                    onDateSelected = { date -> selectedDate = date }

                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ButtonColor)
                        .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedDate.format(dateFormatter),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CardDark,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = {
                            newEntryType = CalendarEntryType.Event
                            newEntryTitle = ""
                            newEntryDateText = selectedDate.format(addDateFormatter)
                            newEntryTimeText = ""
                            reminderRangeFromText = selectedDate.format(addDateFormatter)
                            reminderRangeToText = selectedDate.format(addDateFormatter)
                            notifyEnabled = false
                            notificationMode = NotificationMode.BeforeHours
                            notifyBeforeHoursText = "2"
                            notifyAtTimeText = "08:00"
                            addDialogError = null
                            showAddDialog = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.plus),
                                contentDescription = "Добавить запись",
                                tint = CardDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (eventsForSelectedDate.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "На этот день планов нет",
                                fontSize = 16.sp,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        ScrollbarLazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(eventsForSelectedDate) { index, event ->
                                val itemTitle = when (event.type) {
                                    CalendarEntryType.Event -> event.title
                                    CalendarEntryType.Reminder -> "Напоминание: ${event.title}"
                                }
                                val iconRes = mapMedicalIconByKeywords(itemTitle)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(25.dp))
                                        .background(ButtonColor)
                                        .padding(horizontal = 10.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = "Иконка записи",
                                        tint = medicalIconTintFor(iconRes),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = buildString {
                                            append(itemTitle)
                                            if (!event.time.isNullOrBlank()) {
                                                append(" • ")
                                                append(event.time)
                                            }
                                            if (!event.notificationText.isNullOrBlank()) {
                                                append(" • ")
                                                append(event.notificationText)
                                            }
                                        },
                                        fontSize = 20.sp,
                                        fontFamily = ArianFontFamily,
                                        color = SurfaceDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { pendingDeleteTarget = selectedDate to index }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Удалить запись",
                                            tint = CardDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Preview
@Composable
fun CalendarScreenPreview() {
    CalendarScreen(navController = rememberNavController())
}
