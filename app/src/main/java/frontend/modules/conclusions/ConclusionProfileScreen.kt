package frontend.modules.conclusions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mymedbook.R
import frontend.modules.BackgroundScreen
import frontend.modules.common.ScrollbarLazyColumn
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.modules.common.medicalIconTintFor
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import frontend.modules.common.MedCard

@Composable
fun ConclusionProfileScreen(
    navController: NavController,
    conclusionsStore: ConclusionsStore,
    conclusionId: String
) {
    val conclusion = conclusionsStore.getById(conclusionId) ?: run {
        BackgroundScreen {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Заключение не найдено", color = TextPrimary, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                    ) {
                        Text("Назад", color = SelectedColor)
                    }
                }
            }
        }
        return
    }

    BackgroundScreen {
        Box(modifier = Modifier.fillMaxSize()) {
            var showEditPhotos by remember { mutableStateOf(false) }
            var showEditFiles by remember { mutableStateOf(false) }
            var showDeleteConfirm by remember { mutableStateOf(false) }
            var showEditTitleDialog by remember { mutableStateOf(false) }
            var showExportConfirm by remember { mutableStateOf(false) }

            var titleState by remember { mutableStateOf(conclusion.title) }
            val photosState =
                remember { mutableStateListOf<Bitmap>().apply { addAll(conclusion.photos) } }
            val filesState =
                remember { mutableStateListOf<ConclusionFileAttachment>().apply { addAll(conclusion.files) } }

            ScrollbarLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                top = 6.dp,
                                end = 12.dp,
                                bottom = 12.dp
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    IconButton(onClick = {
                                        titleState = conclusion.title
                                        showEditTitleDialog = true
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_edit),
                                            contentDescription = "Редактировать"
                                        )
                                    }

                                    IconButton(onClick = { showExportConfirm = true }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.pdf3),
                                            contentDescription = "Выгрузить",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(onClick = { showDeleteConfirm = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Удалить"
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 47.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(
                                                androidx.compose.foundation.shape.RoundedCornerShape(
                                                    18.dp
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = mapMedicalIconByKeywords(conclusion.title)
                                        Icon(
                                            painter = painterResource(id = icon),
                                            contentDescription = "Иконка",
                                            tint = medicalIconTintFor(icon),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.size(14.dp))

                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    ) {
                                        Text(
                                            text = titleState,
                                            color = TextPrimary,
                                            fontSize = 22.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = conclusion.date,
                                            color = TextSecondary,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        when (conclusion.sourceType) {
                                            ConclusionSourceType.DOCTOR -> Text(
                                                text = "Врач: ${conclusion.doctorName ?: "не указан"}",
                                                color = TextPrimary
                                            )

                                            ConclusionSourceType.CLINIC -> {
                                                Text(
                                                    text = "Клиника: ${conclusion.clinicName ?: "не указана"}",
                                                    color = TextPrimary
                                                )
                                                conclusion.doctorSpecialization?.let { spec ->
                                                    if (spec.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Специализация: $spec",
                                                            color = TextSecondary,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }

                                            ConclusionSourceType.UNKNOWN -> Text(
                                                text = "Источник: ${conclusion.sourceNote.ifBlank { "Не указано" }}",
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }

                                if (showExportConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showExportConfirm = false },
                                        title = { Text("Выгрузить заключение") },
                                        text = { Text("Скачать это заключение в PDF? (функция заглушка)") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                showExportConfirm = false
                                            }) { Text("Выгрузить") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                showExportConfirm = false
                                            }) { Text("Отмена") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                top = 2.dp,
                                end = 12.dp,
                                bottom = 12.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 12.dp, start = 0.dp, end = 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Фото", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { showEditPhotos = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Изменить",
                                        modifier = Modifier.size(20.dp).offset(y = (-8).dp)
                                    )
                                }
                            }

                            if (photosState.isEmpty()) {
                                ScrollbarLazyColumn(
                                    modifier = Modifier
                                        .height(440.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(3) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.analyses),
                                                contentDescription = "Пример фото",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(420.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Добавлено фото: ${photosState.size}",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                ScrollbarLazyColumn(
                                    modifier = Modifier
                                        .height(440.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(photosState) { bitmap ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Фото заключения",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(420.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        1.dp,
                                                        SurfaceDark,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                top = 2.dp,
                                end = 12.dp,
                                bottom = 12.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 12.dp, start = 0.dp, end = 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Файлы", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { showEditFiles = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Изменить",
                                        modifier = Modifier.size(20.dp).offset(y = (-8).dp)
                                    )
                                }
                            }

                            if (filesState.isEmpty()) {
                                ScrollbarLazyColumn(
                                    modifier = Modifier
                                        .height(180.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(
                                        listOf(
                                            "Пример_результат_1.pdf",
                                            "Пример_результат_2.pdf",
                                            "Пример_результат_3.pdf"
                                        )
                                    ) { name ->
                                        MedCard(
                                            title = name,
                                            subtitle = "Файл",
                                            iconResId = R.drawable.analyses
                                        )
                                    }
                                }
                            } else {
                                ScrollbarLazyColumn(
                                    modifier = Modifier
                                        .height(180.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filesState) { file ->
                                        MedCard(
                                            title = file.displayName,
                                            subtitle = "Файл",
                                            iconResId = R.drawable.analyses
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showExportConfirm) {
                AlertDialog(
                    onDismissRequest = { showExportConfirm = false },
                    title = { Text("Выгрузить заключение") },
                    text = { Text("Скачать это заключение в PDF? (функция заглушка)") },
                    confirmButton = {
                        TextButton(onClick = {
                            showExportConfirm = false
                        }) { Text("Выгрузить") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showExportConfirm = false
                        }) { Text("Отмена") }
                    }
                )
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Удалить заключение") },
                    text = { Text("Точно ли хотите удалить заключение \"${conclusion.title}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val removed = conclusionsStore.delete(conclusionId)
                            showDeleteConfirm = false
                            if (removed) navController.popBackStack()
                        }) { Text("Удалить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
                    }
                )
            }

            if (showEditTitleDialog) {
                AlertDialog(
                    onDismissRequest = { showEditTitleDialog = false },
                    title = { Text("Редактировать название") },
                    text = {
                        Column {
                            TextField(
                                value = titleState,
                                onValueChange = { titleState = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val trimmed = titleState.trim()
                            if (trimmed.isNotBlank()) {
                                val draft = ConclusionDraft(
                                    title = trimmed,
                                    date = conclusion.date,
                                    sourceType = conclusion.sourceType,
                                    doctorId = conclusion.doctorId,
                                    doctorName = conclusion.doctorName,
                                    doctorSpecialization = conclusion.doctorSpecialization,
                                    clinicName = conclusion.clinicName,
                                    sourceNote = conclusion.sourceNote,
                                    folders = conclusion.folders,
                                    photos = conclusion.photos,
                                    files = conclusion.files
                                )
                                conclusionsStore.updateFromDraft(conclusionId, draft)
                                showEditTitleDialog = false
                            }
                        }) { Text("Сохранить") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showEditTitleDialog = false
                        }) { Text("Отмена") }
                    }
                )
            }

            if (showEditPhotos) {
                ConclusionEditPhotosDialog(
                    initial = photosState,
                    onDismiss = { showEditPhotos = false },
                    onSaved = { newList ->
                        photosState.clear()
                        photosState.addAll(newList)
                        val draft = ConclusionDraft(
                            title = conclusion.title,
                            date = conclusion.date,
                            sourceType = conclusion.sourceType,
                            doctorId = conclusion.doctorId,
                            doctorName = conclusion.doctorName,
                            doctorSpecialization = conclusion.doctorSpecialization,
                            clinicName = conclusion.clinicName,
                            sourceNote = conclusion.sourceNote,
                            folders = conclusion.folders,
                            photos = photosState.toList(),
                            files = conclusion.files
                        )
                        conclusionsStore.updateFromDraft(conclusionId, draft)
                    }
                )
            }

            if (showEditFiles) {
                ConclusionEditFilesDialog(
                    initial = filesState,
                    onDismiss = { showEditFiles = false },
                    onSaved = { newList ->
                        filesState.clear()
                        filesState.addAll(newList)
                        val draft = ConclusionDraft(
                            title = conclusion.title,
                            date = conclusion.date,
                            sourceType = conclusion.sourceType,
                            doctorId = conclusion.doctorId,
                            doctorName = conclusion.doctorName,
                            doctorSpecialization = conclusion.doctorSpecialization,
                            clinicName = conclusion.clinicName,
                            sourceNote = conclusion.sourceNote,
                            folders = conclusion.folders,
                            photos = conclusion.photos,
                            files = filesState.toList()
                        )
                        conclusionsStore.updateFromDraft(conclusionId, draft)
                    }
                )
            }
        }
    }
}


@Composable
private fun ConclusionEditPhotosDialog(
    initial: MutableList<Bitmap>,
    onDismiss: () -> Unit,
    onSaved: (List<Bitmap>) -> Unit
) {
    val context = LocalContext.current
    val photosState = remember { mutableStateListOf<Bitmap>().apply { addAll(initial) } }
    val selected = remember { mutableStateListOf<Int>() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val added = uris.mapNotNull { uri -> decodeBitmapFromUri(context, uri) }
        if (added.isNotEmpty()) photosState.addAll(added)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать фото") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { picker.launch(arrayOf("image/*")) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Добавить фото",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { if (selected.isNotEmpty()) { val toRemove = selected.sortedDescending(); toRemove.forEach { photosState.removeAt(it) }; selected.clear() } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Удалить выбранные") }
                }

                ScrollbarLazyColumn(modifier = Modifier.height(300.dp)) {
                    items(photosState.size) { idx ->
                        val bmp = photosState[idx]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(idx),
                                onCheckedChange = { checked ->
                                    if (checked) selected.add(idx) else selected.remove(idx)
                                })
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                if (idx > 0) {
                                    val tmp = photosState[idx - 1]; photosState[idx - 1] =
                                        photosState[idx]; photosState[idx] = tmp
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Up"
                                )
                            }
                            IconButton(onClick = {
                                if (idx < photosState.size - 1) {
                                    val tmp = photosState[idx + 1]; photosState[idx + 1] =
                                        photosState[idx]; photosState[idx] = tmp
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Down"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSaved(photosState.toList()); onDismiss() }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}


@Composable
private fun ConclusionEditFilesDialog(
    initial: MutableList<ConclusionFileAttachment>,
    onDismiss: () -> Unit,
    onSaved: (List<ConclusionFileAttachment>) -> Unit
) {
    val context = LocalContext.current
    val filesState = remember { mutableStateListOf<ConclusionFileAttachment>().apply { addAll(initial) } }
    val selected = remember { mutableStateListOf<Int>() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val added = uris.mapNotNull { uri -> takePersistPermissionSafely(context, uri); val name = resolveDisplayName(context, uri) ?: uri.lastPathSegment ?: "file"; ConclusionFileAttachment(uri = uri.toString(), displayName = name) }
        if (added.isNotEmpty()) filesState.addAll(added)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать файлы") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { picker.launch(arrayOf("*/*")) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Добавить файлы",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { if (selected.isNotEmpty()) { val toRemove = selected.sortedDescending(); toRemove.forEach { filesState.removeAt(it) }; selected.clear() } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Удалить выбранные") }
                }

                ScrollbarLazyColumn(modifier = Modifier.height(240.dp)) {
                    items(filesState.size) { idx ->
                        val f = filesState[idx]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(idx),
                                onCheckedChange = { checked ->
                                    if (checked) selected.add(idx) else selected.remove(idx)
                                })
                            Text(f.displayName, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                if (idx > 0) {
                                    val tmp = filesState[idx - 1]; filesState[idx - 1] =
                                        filesState[idx]; filesState[idx] = tmp
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Up"
                                )
                            }
                            IconButton(onClick = {
                                if (idx < filesState.size - 1) {
                                    val tmp = filesState[idx + 1]; filesState[idx + 1] =
                                        filesState[idx]; filesState[idx] = tmp
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Down"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSaved(filesState.toList()); onDismiss() }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
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


private fun takePersistPermissionSafely(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) {
    }
}


private fun resolveDisplayName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else uri.lastPathSegment
    } ?: uri.lastPathSegment
}



