package frontend.modules.doctors

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mymedbook.R
import frontend.modules.BackgroundScreen
import frontend.modules.common.ScrollbarLazyColumn
import frontend.theme.SurfaceDark
import androidx.core.net.toUri
import frontend.modules.common.IconPickerDefaults
import frontend.modules.common.IconPickerDialog
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.modules.common.medicalIconTintFor


private data class DoctorConclusion(
    val title: String,
    val date: String
)

private data class DoctorContactEntry(
    val label: String,
    val value: String
)

private fun hasRealPhone(value: String): Boolean {
    val clean = value.trim()
    return clean.isNotBlank() && clean.any { it.isDigit() }
}

private fun hasRealLink(value: String): Boolean {
    val clean = value.trim()
    return clean.isNotBlank() && clean != "https://" && clean != "http://"
}

@Composable
fun DoctorProfileScreen(navController: NavController, isNewDoctor: Boolean = false) {
    val conclusions = if (isNewDoctor) {
        emptyList()
    } else {
        listOf(
            DoctorConclusion("Первичный осмотр", "12.03.2026"),
            DoctorConclusion("Контроль после лечения", "25.03.2026"),
            DoctorConclusion("Плановый прием", "06.04.2026"),
            DoctorConclusion("Повторный прием", "15.04.2026"),
            DoctorConclusion("Коррекция лечения", "20.04.2026")
        )
    }

    var doctorName by rememberSaveable(isNewDoctor) {
        mutableStateOf(if (isNewDoctor) "" else "Смирнова Анна Викторовна")
    }
    var specialization by rememberSaveable(isNewDoctor) {
        mutableStateOf(if (isNewDoctor) "" else "Окулист / Терапевт")
    }
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var isProfileEditable by rememberSaveable(isNewDoctor) { mutableStateOf(isNewDoctor) }

    val phoneNumbers = remember(isNewDoctor) { mutableStateListOf<DoctorContactEntry>() }
    val bookingLinks = remember(isNewDoctor) { mutableStateListOf<DoctorContactEntry>() }
    var isContactsEditable by rememberSaveable(isNewDoctor) { mutableStateOf(isNewDoctor) }

    var notesText by rememberSaveable(isNewDoctor) {
        mutableStateOf(
            if (isNewDoctor) {
                ""
            } else {
                "Внимательно объясняет назначения\n" +
                        "Удобное время приема\n" +
                        "Напомнить взять результаты анализов"
            }
        )
    }

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            photoUriString = uri?.toString()
        }

    val photoBitmap = remember(photoUriString) {
        val uri = photoUriString?.let(Uri::parse) ?: return@remember null
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }.getOrNull()
    }

    val flatFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    var profileIconRes by rememberSaveable(isNewDoctor) {
        mutableStateOf(mapMedicalIconByKeywords(if (specialization.isBlank()) "врач" else specialization))
    }
    var showProfileIconPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    BackgroundScreen {
        Box(modifier = Modifier.fillMaxSize()) {
            ScrollbarLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 0.dp, start = 12.dp, end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isNewDoctor) {
                                    IconButton(
                                        onClick = { showDeleteConfirmDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Удалить профиль"
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { isProfileEditable = !isProfileEditable }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Изменить профиль"
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(112.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photoBitmap != null) {
                                        Image(
                                            bitmap = photoBitmap.asImageBitmap(),
                                            contentDescription = "Фото врача",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text("Фото")
                                    }
                                }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    TextField(
                                        value = doctorName,
                                        onValueChange = { doctorName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        readOnly = !isProfileEditable,
                                        colors = flatFieldColors,
                                        placeholder = { Text("ФИО") }
                                    )
                                    TextField(
                                        value = specialization,
                                        onValueChange = { specialization = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = (-16).dp),
                                        singleLine = true,
                                        readOnly = !isProfileEditable,
                                        colors = flatFieldColors,
                                        placeholder = { Text("Специализация") },
                                        prefix = {
                                            IconButton(
                                                onClick = {
                                                    if (isProfileEditable || isNewDoctor) {
                                                        showProfileIconPicker = true
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = profileIconRes),
                                                    contentDescription = "Иконка специализации",
                                                    modifier = Modifier.size(24.dp),
                                                    tint = medicalIconTintFor(profileIconRes)
                                                )
                                            }
                                        }
                                    )

                                    if (isProfileEditable) {
                                        OutlinedButton(onClick = { photoPicker.launch("image/*") }) {
                                            Text("Выбрать фото")
                                        }
                                    }
                                }

                                if (isNewDoctor) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            navController.navigate("doctors") {
                                                popUpTo("doctors") { inclusive = true }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Сохранить")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val visiblePhones = phoneNumbers.filter { hasRealPhone(it.value) }
                            val visibleLinks = bookingLinks.filter { hasRealLink(it.value) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Контакты", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { isContactsEditable = !isContactsEditable }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Изменить контакты"
                                    )
                                }
                            }

                            if (isContactsEditable) {
                                phoneNumbers.forEachIndexed { index, value ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = value.label,
                                            onValueChange = {
                                                phoneNumbers[index] = value.copy(label = it)
                                            },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            label = { Text("Подпись") }
                                        )
                                        OutlinedTextField(
                                            value = value.value,
                                            onValueChange = {
                                                phoneNumbers[index] = value.copy(value = it)
                                            },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            label = { Text("Телефон ${index + 1}") }
                                        )
                                        OutlinedButton(onClick = { phoneNumbers.removeAt(index) }) {
                                            Text("Удалить")
                                        }
                                    }
                                }

                                OutlinedButton(onClick = {
                                    phoneNumbers.add(
                                        DoctorContactEntry(
                                            "",
                                            ""
                                        )
                                    )
                                }) {
                                    Text("Добавить номер")
                                }

                                bookingLinks.forEachIndexed { index, value ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = value.label,
                                            onValueChange = {
                                                bookingLinks[index] = value.copy(label = it)
                                            },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            label = { Text("Подпись") }
                                        )
                                        OutlinedTextField(
                                            value = value.value,
                                            onValueChange = {
                                                bookingLinks[index] = value.copy(value = it)
                                            },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            label = { Text("Ссылка ${index + 1}") }
                                        )
                                        OutlinedButton(onClick = { bookingLinks.removeAt(index) }) {
                                            Text("Удалить")
                                        }
                                    }
                                }

                                OutlinedButton(onClick = {
                                    bookingLinks.add(
                                        DoctorContactEntry(
                                            "",
                                            ""
                                        )
                                    )
                                }) {
                                    Text("Добавить ссылку")
                                }
                            } else {
                                if (visiblePhones.isEmpty()) {
                                    Text(
                                        text = "Номер не добавлен",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    visiblePhones.forEach { phone ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (phone.label.isNotBlank()) phone.label else "Номер",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(phone.value)
                                            }
                                            OutlinedButton(onClick = {
                                                val digits =
                                                    phone.value.filter { it.isDigit() || it == '+' }
                                                if (digits.isNotBlank()) {
                                                    val dialIntent = Intent(
                                                        Intent.ACTION_DIAL,
                                                        "tel:$digits".toUri()
                                                    )
                                                    context.startActivity(dialIntent)
                                                }
                                            }) {
                                                Text("Позвонить")
                                            }
                                        }
                                    }
                                }

                                if (visibleLinks.isEmpty()) {
                                    Text(
                                        text = "Ссылка не добавлена",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    visibleLinks.forEach { link ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (link.label.isNotBlank()) link.label else "Ссылка",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
//                                                Text(link.value)
                                            }
                                            Button(onClick = { uriHandler.openUri(link.value) }) {
                                                Text("Перейти по ссылке")
                                            }
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { navController.navigate("calendar") }) {
                                    Text("К календарю")
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Заключения врача",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            ScrollbarLazyColumn(
                                modifier = Modifier
                                    .height(220.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(conclusions) { conclusion ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                conclusion.title,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                conclusion.date,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Заметки о враче", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                            )
                        }
                    }
                }
            }


            if (showProfileIconPicker) {
                IconPickerDialog(
                    icons = IconPickerDefaults.availableIcons,
                    onDismiss = { showProfileIconPicker = false },
                    onPick = { icon ->
                        profileIconRes = icon
                        showProfileIconPicker = false
                    }
                )
            }

            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Удалить профиль врача?") },
                    text = { Text("Вы уверены, что хотите удалить этот профиль? Это действие невозможно отменить.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                navController.navigate("doctors") {
                                    popUpTo("doctors") { inclusive = true }
                                }
                            }
                        ) {
                            Text("Удалить")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = false }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}


//@Composable
//fun DoctorProfileScreenPreview() {
//    DoctorProfileScreen(navController = rememberNavController())
//}
