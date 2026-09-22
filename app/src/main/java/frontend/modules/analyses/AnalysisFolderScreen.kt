package frontend.modules.analyses

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mymedbook.R
import frontend.modules.BackgroundScreen
import frontend.modules.common.DateRangeScrollFilterDialog
import frontend.modules.common.MedCard
import frontend.modules.common.ScrollbarLazyColumn
import frontend.theme.ButtonColor
import frontend.theme.CardDark
import frontend.theme.Primary
import frontend.theme.SelectedColor
import frontend.theme.SurfaceDark
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import frontend.navigation.Route
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalysisFolderScreen(
    navController: NavController,
    analysesStore: AnalysesStore,
    initialFolderName: String
) {
    var currentFolderName by remember(initialFolderName) { mutableStateOf(initialFolderName) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddModeDialog by remember { mutableStateOf(false) }
    var showAddNewDialog by remember { mutableStateOf(false) }
    var showPickExistingDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showExportSelectDialog by remember { mutableStateOf(false) }
    var showAddAnalysisDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var renameText by remember { mutableStateOf(currentFolderName) }
    var renameError by remember { mutableStateOf<String?>(null) }

    var searchAnalyses by remember { mutableStateOf("") }
    var dateFromFilter by remember { mutableStateOf("") }
    var dateToFilter by remember { mutableStateOf("") }

    val defaultDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.uuuu")) }
    var newAnalysisTitle by remember { mutableStateOf("") }
    var newAnalysisDate by remember { mutableStateOf(defaultDate) }
    var addAnalysisError by remember { mutableStateOf<String?>(null) }

    val resolvedFolderName = analysesStore.folderNames.firstOrNull { it.equals(currentFolderName, ignoreCase = true) }

    BackgroundScreen {
        if (resolvedFolderName == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Папка удалена", color = TextPrimary, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                ) {
                    Text("Назад", color = SelectedColor)
                }
            }
            return@BackgroundScreen
        }

        val folderName = resolvedFolderName
        val folderAnalyses = analysesStore.analysesForFolder(folderName)
        val filteredFolderAnalyses = folderAnalyses.filter { analysis ->
            val titleMatch = analysis.title.contains(searchAnalyses, ignoreCase = true)

            val dateMatch = if (dateFromFilter.isEmpty() && dateToFilter.isEmpty()) {
                true
            } else {
                val analysisDate = analysis.date.split(".").let { parts ->
                    if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else analysis.date
                }

                val convertDate = { dateStr: String ->
                    val parts = dateStr.split(".").takeIf { it.size == 3 }
                    if (parts != null) {
                        "${parts[2]}-${parts[1]}-${parts[0]}"
                    } else {
                        dateStr
                    }
                }

                val from =
                    if (dateFromFilter.isNotEmpty()) convertDate(dateFromFilter) else "1900-01-01"
                val to = if (dateToFilter.isNotEmpty()) convertDate(dateToFilter) else "2099-12-31"
                analysisDate.compareTo(from) >= 0 && analysisDate.compareTo(to) <= 0
            }

            titleMatch && dateMatch
        }

        val availableAnalyses = analysesStore.getAllAnalyses().filter { analysis ->
            folderAnalyses.none { it.id == analysis.id }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = folderName,
                        color = CardDark,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        renameText = folderName
                        renameError = null
                        showRenameDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Переименовать папку",
                            tint = CardDark
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Удалить папку",
                            tint = CardDark
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchAnalyses,
                        onValueChange = { searchAnalyses = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .border(
                                width = 1.dp,
                                color = SurfaceDark,
                                shape = RoundedCornerShape(32.dp)
                            ),
                        placeholder = { Text("Поиск анализов...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ButtonColor,
                            unfocusedContainerColor = Color(0, 0, 0, 0),
                            focusedTextColor = Primary,
                            unfocusedTextColor = Primary,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(32.dp),
                        singleLine = true,
                        maxLines = 1
                    )

                    Button(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Фильтр",
                            modifier = Modifier.size(24.dp),
                            tint = Primary
                        )
                    }

                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.pdf3),
                            contentDescription = "Выгрузить из папки",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = { showAddModeDialog = true },
                        modifier = Modifier.size(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        GradientAddIcon(modifier = Modifier.size(28.dp))
                    }
                }

                if (showFilterDialog) {
                    DateRangeScrollFilterDialog(
                        dateFrom = dateFromFilter,
                        dateTo = dateToFilter,
                        onDismiss = { showFilterDialog = false },
                        onConfirm = { from, to ->
                            dateFromFilter = from
                            dateToFilter = to
                            showFilterDialog = false
                        },
                        onClear = {
                            dateFromFilter = ""
                            dateToFilter = ""
                            showFilterDialog = false
                        }
                    )
                }

                if (showExportDialog) {
                    AlertDialog(
                        onDismissRequest = { showExportDialog = false },
                        title = { Text("Выгрузить из папки", color = Color.White) },
                        text = {
                            Column {
                                Text("Выберите действие:", color = TextPrimary)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                // export all (placeholder)
                                showExportDialog = false
                                // show a simple confirmation
                                showExportSelectDialog = false
                            }) { Text("Выгрузить все", color = SelectedColor) }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showExportDialog = false
                                showExportSelectDialog = true
                            }) { Text("Выбрать", color = SelectedColor) }
                        },
                        containerColor = CardDark
                    )
                }

                if (showExportSelectDialog) {
                    val toExport = remember { mutableStateListOf<String>() }
                    AlertDialog(
                        onDismissRequest = { showExportSelectDialog = false },
                        title = { Text("Выбрать анализы для выгрузки", color = Color.White) },
                        text = {
                            ScrollbarLazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                                items(availableAnalyses) { analysis ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val checked = toExport.contains(analysis.id)
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { now ->
                                                if (now) toExport.add(analysis.id) else toExport.remove(
                                                    analysis.id
                                                )
                                            })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = analysis.title,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                // placeholder: perform export of selected items
                                showExportSelectDialog = false
                            }) { Text("Выгрузить в PDF", color = SelectedColor) }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showExportSelectDialog = false
                            }) { Text("Отмена", color = Color.Gray) }
                        },
                        containerColor = CardDark
                    )
                }

                ScrollbarLazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 0.dp,
                        bottom = 16.dp
                    )
                ) {
                    if (filteredFolderAnalyses.isEmpty()) {
                        item {
                            Text(
                                text = "В этой папке пока нет анализов",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        items(filteredFolderAnalyses) { analysis ->
                            MedCard(
                                title = analysis.title,
                                subtitle = analysis.date,
                                iconResId = analysesStore.analysisIcons[analysis.id],
                                onClick = {
                                    navController.navigate(
                                        Route.AnalysisProfile.createRoute(analysis.id)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val newName = renameText.trim()
                        if (newName.isEmpty()) {
                            renameError = "Введите название папки"
                            return@TextButton
                        }

                        val renamed = analysesStore.renameFolder(folderName, newName)
                        if (!renamed) {
                            renameError = "Папка с данным названием уже существует"
                            return@TextButton
                        }

                        currentFolderName = newName
                        renameError = null
                        showRenameDialog = false
                    }) {
                        Text("Сохранить", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("Переименовать папку", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = renameText,
                            onValueChange = {
                                renameText = it
                                renameError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Название папки") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedPlaceholderColor = TextSecondary,
                                unfocusedPlaceholderColor = TextSecondary
                            )
                        )

                        renameError?.let {
                            Text(text = it, color = Color(0xFFFF8A80), fontSize = 12.sp)
                        }
                    }
                },
                containerColor = CardDark
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        analysesStore.deleteFolder(folderName)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Удалить", color = Color(0xFFFF8A80))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                },
                title = { Text("Удалить папку?", color = Color.White) },
                text = { Text("Анализы останутся в общем списке", color = TextPrimary) },
                containerColor = CardDark
            )
        }

        if (showAddModeDialog) {
            AlertDialog(
                onDismissRequest = { showAddModeDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showAddModeDialog = false
                        addAnalysisError = null
                        newAnalysisTitle = ""
                        newAnalysisDate = defaultDate
                        showAddAnalysisDialog = true
                    }) {
                        Text("Новый анализ", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddModeDialog = false
                        showPickExistingDialog = true
                    }) {
                        Text("Из существующих", color = SelectedColor)
                    }
                },
                title = { Text("Добавить в папку", color = Color.White) },
                text = { Text("Выберите способ добавления", color = TextPrimary) },
                containerColor = CardDark
            )
        }

        if (showAddAnalysisDialog) {
            AddAnalysisDialog(
                analysesStore = analysesStore,
                onDismiss = { showAddAnalysisDialog = false },
                onSaved = { showAddAnalysisDialog = false },
                initialSelectedFolders = setOf(folderName)
            )
        }

        if (showPickExistingDialog) {
            val selectedExisting = remember { mutableStateListOf<String>() }

            AlertDialog(
                onDismissRequest = { showPickExistingDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        // Link all selected analyses to the folder
                        selectedExisting.forEach { id ->
                            analysesStore.linkExistingAnalysisToFolder(
                                folderName,
                                id
                            )
                        }
                        showPickExistingDialog = false
                    }) {
                        Text("Готово", color = SelectedColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPickExistingDialog = false }) {
                        Text(
                            "Отмена",
                            color = Color.Gray
                        )
                    }
                },
                title = { Text("Выбрать из существующих", color = Color.White) },
                text = {
                    if (availableAnalyses.isEmpty()) {
                        Text("Все анализы уже добавлены в папку", color = TextSecondary)
                    } else {
                        ScrollbarLazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            items(availableAnalyses) { analysis ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val checked = selectedExisting.contains(analysis.id)
                                    Checkbox(checked = checked, onCheckedChange = { checkedNow ->
                                        if (checkedNow) selectedExisting.add(analysis.id) else selectedExisting.remove(
                                            analysis.id
                                        )
                                    })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = analysis.title,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = CardDark
            )
        }
    }
}

@Composable
private fun GradientAddIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = R.drawable.plus),
        contentDescription = "Добавить",
        tint = Color.White,
        modifier = modifier
            .graphicsLayer(alpha = 0.99f)
            .drawWithCache {
                val gradient = Brush.linearGradient(
                    colors = listOf(Primary, Primary),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = gradient, blendMode = BlendMode.SrcAtop)
                }
            }
    )
}
