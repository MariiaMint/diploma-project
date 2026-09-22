package frontend.modules.analyses

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mymedbook.R
import frontend.modules.BackgroundScreen
import frontend.modules.common.MedCard
import frontend.modules.common.ScrollbarLazyColumn
import frontend.modules.common.medicalIconTintFor
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import frontend.modules.common.mapMedicalIconByKeywords

@Composable
fun AnalysisProfileScreen(
    navController: NavController,
    analysesStore: AnalysesStore,
    analysisId: String
) {
    val analysis = analysesStore.getAnalysisById(analysisId) ?: run {
        BackgroundScreen {
            Box(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Анализ не найден", color = TextPrimary, fontSize = 20.sp)
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

            val analysisIcon =
                analysesStore.analysisIcons[analysis.id] ?: defaultAnalysisIcon(analysis.title)
            val photos = analysesStore.photosForAnalysis(analysis.id).toMutableList()
            val files = analysesStore.filesForAnalysis(analysis.id).toMutableList()

            var showEditPhotos by remember { mutableStateOf(false) }
            var showEditFiles by remember { mutableStateOf(false) }
            var showDeleteConfirm by remember { mutableStateOf(false) }

            var showEditTitleDialog by remember { mutableStateOf(false) }
            var showExportConfirm by remember { mutableStateOf(false) }
            var newTitle by remember { mutableStateOf(analysis.title) }

            ScrollbarLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProfileSectionCard {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Top row: action icons at right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.Top
                            ) {
                                IconButton(onClick = {
                                    newTitle = analysis.title
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
                                        .clip(RoundedCornerShape(18.dp)),
//                                        .border(1.dp, SurfaceDark, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = analysisIcon),
                                        contentDescription = "Иконка анализа",
                                        tint = medicalIconTintFor(analysisIcon),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.size(14.dp))

                                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    Text(
                                        text = analysis.title,
                                        color = TextPrimary,
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = analysis.date,
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            if (showExportConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showExportConfirm = false },
                                    title = { Text("Выгрузить анализ") },
                                    text = { Text("Скачать этот анализ в PDF? (заглушка)") },
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

//                item { SectionTitle(text = "Папки") }
//                item {
//                    ProfileSectionCard {
//                        if (folders.isEmpty()) {
//                            Text(text = "Не добавлен в папки", color = TextSecondary, fontSize = 14.sp)
//                        } else {
//                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                                folders.forEach { folderName ->
//                                    Card(
//                                        shape = RoundedCornerShape(12.dp),
//                                        modifier = Modifier.fillMaxWidth(),
//                                        colors = CardDefaults.cardColors(
//                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
//                                        )
//                                    ) {
//                                        Column(modifier = Modifier.padding(12.dp)) {
//                                            Text(
//                                                text = folderName,
//                                                style = MaterialTheme.typography.bodyLarge,
//                                                color = MaterialTheme.colorScheme.onSurface
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }


                item {
                    ProfileSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 0.dp),

                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Фото", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = { showEditPhotos = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Изменить",
                                        modifier = Modifier.size(20.dp).offset(y = (-8).dp)
                                    )
                                }
                            }
                        }

                        if (photos.isEmpty()) {
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
                                text = "Добавлено фото: ${photos.size}",
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            ScrollbarLazyColumn(
                                modifier = Modifier
                                    .height(440.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(photos) { bitmap ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Фото анализа",
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


                item {
                    ProfileSectionCard {

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Файлы", style = MaterialTheme.typography.titleMedium)
//                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { showEditFiles = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Изменить",
                                    modifier = Modifier.size(20.dp).offset(y = (-8).dp)
                                )
                            }
//                            }
                        }
//                        Spacer(modifier = Modifier.height(4.dp))

                        if (files.isEmpty()) {
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
                                items(files) { file ->
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
            if (showEditPhotos) {
                EditPhotosDialog(analysisId = analysis.id, analysesStore = analysesStore) {
                    showEditPhotos = false
                }
            }

            if (showEditFiles) {
                EditFilesDialog(analysisId = analysis.id, analysesStore = analysesStore) {
                    showEditFiles = false
                }
            }
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Удалить анализ") },
                    text = { Text("Точно ли хотите удалить анализ \"${analysis.title}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val removed = analysesStore.deleteAnalysis(analysis.id)
                            showDeleteConfirm = false
                            if (removed) navController.popBackStack()
                        }) { Text("Удалить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
                    }
                )
            }
            // диалог редактирования названия
            if (showEditTitleDialog) {
                AlertDialog(
                    onDismissRequest = { showEditTitleDialog = false },
                    title = { Text("Редактировать название") },
                    text = {
                        Column {
                            TextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val trimmed = newTitle.trim()
                            if (trimmed.isNotBlank()) {
                                analysesStore.updateAnalysisTitle(analysis.id, trimmed)
                                showEditTitleDialog = false
                            }
                        }) { Text("Сохранить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditTitleDialog = false }) { Text("Отмена") }
                    }
                )
            }
        }
    }
}

@Composable
private fun EditPhotosDialog(
    analysisId: String,
    analysesStore: AnalysesStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val photosState = analysesStore.analysisPhotos.getOrPut(analysisId) { mutableStateListOf() }
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
                // top-right controls: add / delete selected
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { picker.launch(arrayOf("image/*")) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Добавить фото",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = {
                        if (selected.isNotEmpty()) {
                            val toRemove = selected.sortedDescending()
                            toRemove.forEach { photosState.removeAt(it) }
                            selected.clear()
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Удалить выбранные")
                    }
                }

                ScrollbarLazyColumn(modifier = Modifier.height(300.dp)) {
                    items(photosState.indices.toList()) { idx ->
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
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun EditFilesDialog(
    analysisId: String,
    analysesStore: AnalysesStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val filesState = analysesStore.analysisFiles.getOrPut(analysisId) { mutableStateListOf() }
    val selected = remember { mutableStateListOf<Int>() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val added = uris.mapNotNull { uri ->
            takePersistPermissionSafely(context, uri)
            val name = resolveDisplayName(context, uri) ?: uri.lastPathSegment ?: "file"
            AnalysisFileAttachment(uri = uri.toString(), displayName = name)
        }
        if (added.isNotEmpty()) filesState.addAll(added)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать файлы") },
        text = {
            Column {
                // top-right controls: add / delete selected
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { picker.launch(arrayOf("*/*")) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Добавить файлы",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = {
                        if (selected.isNotEmpty()) {
                            val toRemove = selected.sortedDescending()
                            toRemove.forEach { filesState.removeAt(it) }
                            selected.clear()
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Удалить выбранные")
                    }
                }

                ScrollbarLazyColumn(modifier = Modifier.height(240.dp)) {
                    items(filesState.indices.toList()) { idx ->
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
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
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

@Composable
private fun ProfileSectionCard(
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        // уменьшенный верхний padding — чтобы заголовки секций (Фото/Файлы) были выше
        Column(modifier = Modifier.padding(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 12.dp)) {
            content()
        }
    }
}

private fun defaultAnalysisIcon(title: String): Int {
    return mapMedicalIconByKeywords(title)
}

