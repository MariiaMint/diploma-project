package frontend.modules.analyses

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import frontend.theme.ButtonColor
import frontend.theme.CardDark
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import frontend.modules.common.ScrollbarLazyRow
import frontend.data.AnalysesRepository
import frontend.data.AnalysisModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import frontend.data.FoldersRepository
import frontend.data.room.AttachmentEntity
import frontend.data.room.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth

private data class PickerDateParts(
    val year: Int,
    val month: Int,
    val day: Int
)

private fun parseDateOrNow(value: String): PickerDateParts {
    val parts = value.split(".")
    if (parts.size != 3) {
        val now = LocalDate.now()
        return PickerDateParts(now.year, now.monthValue, now.dayOfMonth)
    }

    val day = parts[0].toIntOrNull()
    val month = parts[1].toIntOrNull()
    val year = parts[2].toIntOrNull()
    if (day == null || month == null || year == null) {
        val now = LocalDate.now()
        return PickerDateParts(now.year, now.monthValue, now.dayOfMonth)
    }

    val safeMonth = month.coerceIn(1, 12)
    val safeYear = year.coerceIn(1900, 2100)
    val maxDay = YearMonth.of(safeYear, safeMonth).lengthOfMonth()
    return PickerDateParts(safeYear, safeMonth, day.coerceIn(1, maxDay))
}

private fun formatDate(parts: PickerDateParts): String {
    return "%02d.%02d.%04d".format(parts.day, parts.month, parts.year)
}

@Composable
private fun DateScrollPicker(
    initialValue: PickerDateParts,
    onValueChange: (PickerDateParts) -> Unit
) {
    var year by remember(initialValue) { mutableIntStateOf(initialValue.year) }
    var month by remember(initialValue) { mutableIntStateOf(initialValue.month) }
    var day by remember(initialValue) { mutableIntStateOf(initialValue.day) }

    val daysInMonth = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    if (day > daysInMonth) day = daysInMonth

    onValueChange(PickerDateParts(year = year, month = month, day = day))

    val monthNames = arrayOf(
        "Янв", "Фев", "Мар", "Апр", "Май", "Июн",
        "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnalysisDialog(
    analysesStore: AnalysesStore,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    initialSelectedFolders: Set<String> = emptySet()
) {
    val context = LocalContext.current
    val defaultDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.uuuu")) }

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var selectedFolders by remember(initialSelectedFolders) { mutableStateOf(initialSelectedFolders) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(parseDateOrNow(defaultDate)) }
    var photos by remember { mutableStateOf(listOf<Bitmap>()) }
    var files by remember { mutableStateOf(listOf<AnalysisFileAttachment>()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photos = photos + bitmap
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        val newFiles = uris.mapNotNull { uri ->
            takePersistPermissionSafely(context, uri)
            val name = resolveDisplayName(context, uri) ?: return@mapNotNull null
            AnalysisFileAttachment(uri = uri.toString(), displayName = name)
        }

        if (newFiles.isNotEmpty()) {
            files = (files + newFiles).distinctBy { it.uri }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить анализ", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorText = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Название анализа") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedPlaceholderColor = TextSecondary,
                        unfocusedPlaceholderColor = TextSecondary
                    )
                )

                Button(
                    onClick = {
                        pickedDate = parseDateOrNow(date)
                        showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Дата: $date", color = TextPrimary)
                }

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    TextField(
                        value = if (selectedFolders.isEmpty()) {
                            "Папки: не выбрано"
                        } else {
                            "Папки: ${selectedFolders.joinToString()}"
                        },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedPlaceholderColor = TextSecondary,
                            unfocusedPlaceholderColor = TextSecondary
                        )
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        analysesStore.folderNames.forEach { folder ->
                            val checked = selectedFolders.contains(folder)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(checked = checked, onCheckedChange = null)
                                        Text(folder, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                },
                                onClick = {
                                    selectedFolders = if (checked) {
                                        selectedFolders - folder
                                    } else {
                                        selectedFolders + folder
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("Фото", color = SurfaceDark)
                    }

                    Button(
                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("Файлы", color = SurfaceDark)
                    }
                }

                Text("Фото: ${photos.size}", color = TextPrimary, fontSize = 13.sp)
                if (photos.isNotEmpty()) {
                    ScrollbarLazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(98.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(photos) { photo ->
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .border(1.dp, SurfaceDark, RoundedCornerShape(10.dp))
                                    .padding(2.dp)
                            ) {
                                Image(
                                    bitmap = photo.asImageBitmap(),
                                    contentDescription = "Фото анализа",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Text("Файлы: ${files.size}", color = TextPrimary, fontSize = 13.sp)
                if (files.isNotEmpty()) {
                    ScrollbarLazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(files) { file ->
                            Box(
                                modifier = Modifier
                                    .height(44.dp)
                                    .border(1.dp, SurfaceDark, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = file.displayName,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                errorText?.let {
                    Text(text = it, color = Color(0xFFFF8A80), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedTitle = title.trim()
                val finalDate = date.trim().ifEmpty { defaultDate }

                if (trimmedTitle.isEmpty()) {
                    errorText = "Введите название анализа"
                    return@TextButton
                }

                val newId = analysesStore.addAnalysis(
                    title = trimmedTitle,
                    date = finalDate,
                    folders = selectedFolders.toList(),
                    photos = photos,
                    files = files
                )

                if (newId == null) {
                    errorText = "Не удалось добавить анализ"
                    return@TextButton
                }

                // Persist to DB asynchronously but wait for completion before closing.
                if (newId == null) {
                    errorText = "Не удалось добавить анализ"
                    return@TextButton
                }

                var saveError: Exception? = null
                var createdAttachmentIds: List<String> = emptyList()
                val repo = AnalysesRepository(context)

                isSaving@ run {
                    coroutineScope.launch {
                        try {
                            // insert analysis row
                            repo.insert(AnalysisModel(newId, trimmedTitle, finalDate))

                            // save attachments and collect created ids
                            try {
                                val attachmentsRepo = frontend.data.AttachmentsRepository(context)
                                createdAttachmentIds = attachmentsRepo.saveForAnalysis(newId, photos, files)
                            } catch (e: Exception) {
                                saveError = e
                            }

                            // link analysis to folders
                            try {
                                val foldersRepo = frontend.data.FoldersRepository(context)
                                selectedFolders.forEach { folderName ->
                                    try { foldersRepo.linkAnalysisToFolder(newId, folderName) } catch (_: Exception) {}
                                }
                            } catch (_: Exception) {
                            }
                        } catch (e: Exception) {
                            saveError = e
                        }

                        withContext(Dispatchers.Main) {
                            if (saveError != null) {
                                errorText = "Ошибка сохранения: ${saveError!!.message}"
                            } else {
                                // show toast with analysis id and attachment ids
                                val msg = "Анализ добавлен: id=$newId\nattachments=${createdAttachmentIds.joinToString(prefix = "[", postfix = "]")}" 
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                onSaved()
                            }
                        }
                    }
                }
            }) {
                Text("Добавить", color = SelectedColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.Gray)
            }
        },
        containerColor = CardDark
    )

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = formatDate(pickedDate)
                    errorText = null
                    showDatePicker = false
                }) {
                    Text("Выбрать", color = SelectedColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            },
            title = { Text("Выбор даты", color = Color.White) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DateScrollPicker(
                        initialValue = parseDateOrNow(date),
                        onValueChange = { pickedDate = it }
                    )
                }
            },
            containerColor = CardDark,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private fun takePersistPermissionSafely(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: Exception) {
        // Некоторые провайдеры не поддерживают persistable permission.
    }
}

private fun resolveDisplayName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else {
            uri.lastPathSegment
        }
    } ?: uri.lastPathSegment
}

