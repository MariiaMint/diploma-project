package frontend.modules.conclusions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import frontend.modules.common.ScrollbarLazyRow
import frontend.theme.ButtonColor
import frontend.theme.CardDark
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore

data class DoctorOption(
    val id: String,
    val name: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AddConclusionDialog(
    folders: List<String>,
    doctors: List<DoctorOption>,
    knownClinics: List<String>,
    onDismiss: () -> Unit,
    onSaved: (ConclusionDraft) -> Unit
) {
    val context = LocalContext.current
    val defaultDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.uuuu")) }

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var selectedFolders by remember { mutableStateOf(setOf<String>()) }

    var sourceType by remember { mutableStateOf(ConclusionSourceType.DOCTOR) }
    var selectedDoctorId by remember { mutableStateOf<String?>(null) }
    var selectedDoctorName by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var doctorSpecialization by remember { mutableStateOf("") }
    var sourceNote by remember { mutableStateOf("") }
    var createDoctorProfileLater by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }

    var foldersExpanded by remember { mutableStateOf(false) }
    var doctorsExpanded by remember { mutableStateOf(false) }

    var photos by remember { mutableStateOf(listOf<Bitmap>()) }
    var files by remember { mutableStateOf(listOf<ConclusionFileAttachment>()) }

    var errorText by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) photos = photos + bitmap
    }

    val fileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val newFiles = uris.mapNotNull { uri ->
            takePersistPermissionSafely(context, uri)
            val name = resolveDisplayName(context, uri) ?: return@mapNotNull null
            ConclusionFileAttachment(uri = uri.toString(), displayName = name)
        }
        if (newFiles.isNotEmpty()) files = (files + newFiles).distinctBy { it.uri }
    }

    // Временный список врачей, если в аргументе doctors передан пустой список
    val doctorsToShow: List<DoctorOption> = if (doctors.isNotEmpty()) doctors else listOf(
        DoctorOption("doc_tmp_1", "Иван Иванов"),
        DoctorOption("doc_tmp_2", "Анна Петрова"),
        DoctorOption("doc_tmp_3", "Сергей Смирнов")
    )

    // Локальная коллекция папок (immutable list в состоянии) — обновляем заменой списка, чтобы триггерить рекомпозицию
    var foldersLocal by remember { mutableStateOf(folders.toList()) }
    var newFolderInput by remember { mutableStateOf("") }
    var newFolderError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val trimmedTitle = title.trim()
                if (trimmedTitle.isBlank()) {
                    errorText = "Введите название заключения"
                    return@TextButton
                }
                if (sourceType == ConclusionSourceType.DOCTOR && selectedDoctorId == null && !createDoctorProfileLater) {
                    errorText = "Выберите врача или отметьте 'Создать профиль врача позже'"
                    return@TextButton
                }
                if (sourceType == ConclusionSourceType.CLINIC && clinicName.trim().isBlank()) {
                    errorText = "Укажите название клиники"
                    return@TextButton
                }

                onSaved(ConclusionDraft(
                    title = trimmedTitle,
                    date = date,
                    sourceType = sourceType,
                    doctorId = selectedDoctorId,
                    doctorName = selectedDoctorName.ifBlank { null },
                    doctorSpecialization = doctorSpecialization.trim().ifBlank { null },
                    clinicName = clinicName.trim().ifBlank { null },
                    sourceNote = if (createDoctorProfileLater) "Создать профиль врача позже" else sourceNote.trim(),
                    folders = selectedFolders.toList(),
                    photos = photos,
                    files = files
                ))
            }) { Text("Добавить", color = SelectedColor) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Color.Gray) }
        },
        containerColor = CardDark,
        title = { Text("Добавить заключение", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it; errorText = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Название заключения") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedPlaceholderColor = TextSecondary,
                        unfocusedPlaceholderColor = TextSecondary
                    )
                )

                Button(onClick = { pickedDate = LocalDate.now(); showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark), contentPadding = PaddingValues(vertical = 12.dp)) { Text("Дата: $date", color = TextPrimary) }

                Text("Кто выдал?", color = TextPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceTypeButton(text = "Врач", selected = sourceType == ConclusionSourceType.DOCTOR) { sourceType = ConclusionSourceType.DOCTOR }
                    SourceTypeButton(text = "Клиника", selected = sourceType == ConclusionSourceType.CLINIC) { sourceType = ConclusionSourceType.CLINIC }
                }

                if (sourceType == ConclusionSourceType.DOCTOR) {
                    ExposedDropdownMenuBox(expanded = doctorsExpanded, onExpandedChange = { doctorsExpanded = !doctorsExpanded }) {
                        TextField(value = if (selectedDoctorName.isBlank()) "Врач: не выбран" else selectedDoctorName, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorsExpanded) }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedPlaceholderColor = TextSecondary, unfocusedPlaceholderColor = TextSecondary))

                        DropdownMenu(expanded = doctorsExpanded, onDismissRequest = { doctorsExpanded = false }, modifier = Modifier.heightIn(max = 220.dp)) {
                            doctorsToShow.forEach { doctor ->
                                DropdownMenuItem(
                                    text = { Text(doctor.name) },
                                    onClick = {
                                        selectedDoctorId = doctor.id
                                        selectedDoctorName = doctor.name
                                        sourceNote = ""
                                        createDoctorProfileLater = false
                                        doctorsExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Опция "создать профиль врача позже" как чекбокс
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = createDoctorProfileLater, onCheckedChange = { checked ->
                            createDoctorProfileLater = checked
                            if (checked) {
                                selectedDoctorId = null
                                selectedDoctorName = ""
                                sourceNote = "Создать профиль врача позже"
                                doctorsExpanded = false
                            } else {
                                sourceNote = ""
                            }
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Создать профиль врача позже", color = TextSecondary)
                    }

                } else if (sourceType == ConclusionSourceType.CLINIC) {
                    TextField(value = clinicName, onValueChange = { clinicName = it; errorText = null }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Название клиники") }, colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedPlaceholderColor = TextSecondary, unfocusedPlaceholderColor = TextSecondary))

                    val suggestions = knownClinics.filter { it.contains(clinicName, ignoreCase = true) }.take(4)
                    suggestions.forEach { suggestion -> TextButton(onClick = { clinicName = suggestion }, contentPadding = PaddingValues(0.dp)) { Text(suggestion, color = TextSecondary) } }

                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(value = doctorSpecialization, onValueChange = { doctorSpecialization = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Специализация врача (если известна)") }, colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedPlaceholderColor = TextSecondary, unfocusedPlaceholderColor = TextSecondary))
                }

                    ExposedDropdownMenuBox(expanded = foldersExpanded, onExpandedChange = { foldersExpanded = !foldersExpanded }) {
                    TextField(
                        value = if (selectedFolders.isEmpty()) "Папки: не выбрано" else "Папки: ${selectedFolders.joinToString()}",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = foldersExpanded) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedPlaceholderColor = TextSecondary, unfocusedPlaceholderColor = TextSecondary)
                    )

                    DropdownMenu(expanded = foldersExpanded, onDismissRequest = { foldersExpanded = false }, modifier = Modifier.heightIn(max = 240.dp)) {
                        foldersLocal.forEach { folder ->
                            val checked = selectedFolders.contains(folder)
                            DropdownMenuItem(text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Checkbox(checked = checked, onCheckedChange = null)
                                    Text(folder, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }, onClick = {
                                selectedFolders = if (checked) selectedFolders - folder else selectedFolders + folder
                            })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ButtonColor), contentPadding = PaddingValues(vertical = 10.dp)) { Text("Фото", color = SurfaceDark) }
                    Button(onClick = { fileLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ButtonColor), contentPadding = PaddingValues(vertical = 10.dp)) { Text("Файлы", color = SurfaceDark) }
                }

                Text("Фото: ${photos.size}", color = TextPrimary, fontSize = 13.sp)
                if (photos.isNotEmpty()) {
                    ScrollbarLazyRow(
                        modifier = Modifier.fillMaxWidth().height(98.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(photos) { photo ->
                            Box(
                                modifier = Modifier.size(84.dp)
                                    .border(1.dp, SurfaceDark, RoundedCornerShape(10.dp))
                                    .padding(2.dp)
                            ) {
                                Image(
                                    bitmap = photo.asImageBitmap(),
                                    contentDescription = "Фото заключения",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Text("Файлы: ${files.size}", color = TextPrimary, fontSize = 13.sp)
                if (files.isNotEmpty()) {
                    ScrollbarLazyRow(
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(files) { file ->
                            Box(
                                modifier = Modifier.height(44.dp)
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

                errorText?.let { Text(text = it, color = Color(0xFFFF8A80), fontSize = 12.sp) }
            }
        }
    )


    if (showDatePicker) {
        var pickedDateText by remember { mutableStateOf(pickedDate.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))) }

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = pickedDateText.trim()
                    if (trimmed.isNotBlank()) date = trimmed
                    errorText = null
                    showDatePicker = false
                }) { Text("Выбрать", color = SelectedColor) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена", color = Color.Gray) }
            },
            title = { Text("Выбор даты", color = Color.White) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextField(value = pickedDateText, onValueChange = { pickedDateText = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), placeholder = { Text("дд.MM.гггг") })
                }
            },
            containerColor = CardDark,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}


@Composable
private fun SourceTypeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = if (selected) ButtonColor else SurfaceDark, contentColor = if (selected) SelectedColor else TextPrimary), contentPadding = PaddingValues(vertical = 8.dp)) { Text(text) }
}

private fun takePersistPermissionSafely(context: Context, uri: Uri) {
    try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
}

private fun resolveDisplayName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else uri.lastPathSegment
    } ?: uri.lastPathSegment
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()
}


