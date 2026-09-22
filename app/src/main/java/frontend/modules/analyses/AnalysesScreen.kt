package frontend.modules.analyses


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import frontend.modules.BackgroundScreen
import frontend.theme.ButtonColor
import frontend.theme.SurfaceDark
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.painterResource
import com.example.mymedbook.R
import frontend.modules.common.IconPickerDefaults
import frontend.modules.common.IconPickerDialog
import frontend.modules.common.DateRangeScrollFilterDialog
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.modules.common.medicalIconTintFor
import frontend.modules.common.ScrollbarLazyColumn
import frontend.modules.common.ScrollbarLazyVerticalGrid
import frontend.theme.Primary
import frontend.theme.SelectedColor
import frontend.theme.TextPrimary
import frontend.theme.TextSecondary
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import frontend.modules.bars.ExpandableHeader
import frontend.modules.common.MedCard
import frontend.theme.CardDark
import frontend.navigation.Route


@Composable
fun AnalysesScreen(
    navController: NavController,
    analysesStore: AnalysesStore = rememberAnalysesStore()
) {
    BackgroundScreen {
        val folders = analysesStore.folderNames
        val analyses = analysesStore.getAllAnalyses()

        val searchFolders = remember { mutableStateOf("") }
        val searchAnalyses = remember { mutableStateOf("") }
        val dateFromFilter = remember { mutableStateOf("") }
        val dateToFilter = remember { mutableStateOf("") }
        var showFilterDialog by remember { mutableStateOf(false) }
        var showCreateFolderDialog by remember { mutableStateOf(false) }
        var showAddAnalysisDialog by remember { mutableStateOf(false) }
        var newFolderName by remember { mutableStateOf("") }
        var newFolderError by remember { mutableStateOf<String?>(null) }
        var showIconPicker by remember { mutableStateOf(false) }
        var iconTargetType by remember { mutableStateOf("") }
        var iconTargetKey by remember { mutableStateOf("") }

        val availableIcons = IconPickerDefaults.availableIcons

        val folderIcons = analysesStore.folderIcons
        val analysisIcons = analysesStore.analysisIcons

        val filteredFolders = folders.filter { folder ->
            folder.contains(searchFolders.value, ignoreCase = true)
        }

        val filteredAnalyses = analyses.filter { analysis ->
            val titleMatch = analysis.title.contains(searchAnalyses.value, ignoreCase = true)

            val dateMatch = if (dateFromFilter.value.isEmpty() && dateToFilter.value.isEmpty()) {
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
                    if (dateFromFilter.value.isNotEmpty()) convertDate(dateFromFilter.value) else "1900-01-01"
                val to =
                    if (dateToFilter.value.isNotEmpty()) convertDate(dateToFilter.value) else "2099-12-31"

                analysisDate.compareTo(from) >= 0 && analysisDate.compareTo(to) <= 0
            }

            titleMatch && dateMatch
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Выдвижной хедер поверх контента
            ExpandableHeader(title = "Анализы")

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    "Папки", fontSize = 22.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    color = CardDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
//                    .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchFolders.value,
                        onValueChange = { searchFolders.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = SurfaceDark,
                                shape = RoundedCornerShape(32.dp)
                            ),
                        placeholder = { Text("Поиск папок...") },
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
                        onClick = {
                            newFolderName = ""
                            newFolderError = null
                            showCreateFolderDialog = true
                        },
                        modifier = Modifier
                            .size(48.dp),
//                        .border(width = 3.dp, color = SurfaceDark, shape = CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        GradientAddIcon(modifier = Modifier.size(28.dp))
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                ScrollbarLazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                ) {
                    items(filteredFolders.size) { index ->
                        val folderName = filteredFolders[index]
                        Box(
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(
                                    RoundedCornerShape(
                                        topEnd = 48.dp,
                                        topStart = 16.dp,
                                        bottomStart = 16.dp,
                                        bottomEnd = 16.dp
                                    )
                                )
                                .background(color = SurfaceDark)
                                .clickable {
                                    navController.navigate(
                                        Route.AnalysisFolder.createRoute(folderName)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
//                                    .clip(CircleShape)
                                        .clickable {
                                            iconTargetType = "folder"
                                            iconTargetKey = folderName
                                            showIconPicker = true
                                        }
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = folderIcons[folderName] ?: R.drawable.analysis
                                        ),
                                        contentDescription = "Иконка папки",
                                        tint = medicalIconTintFor(
                                            folderIcons[folderName] ?: R.drawable.analysis
                                        ),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = folderName,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Все анализы", fontSize = 22.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    color = CardDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchAnalyses.value,
                        onValueChange = { searchAnalyses.value = it },
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
                        onClick = { showAddAnalysisDialog = true },
                        modifier = Modifier
                            .size(48.dp),
//                        .border(width = 3.dp, color = SurfaceDark, shape = CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        GradientAddIcon(modifier = Modifier.size(28.dp))
                    }
                }

                if (showAddAnalysisDialog) {
                    AddAnalysisDialog(
                        analysesStore = analysesStore,
                        onDismiss = { showAddAnalysisDialog = false },
                        onSaved = { showAddAnalysisDialog = false }
                    )
                }

                if (showFilterDialog) {
                    DateRangeScrollFilterDialog(
                        dateFrom = dateFromFilter.value,
                        dateTo = dateToFilter.value,
                        onDismiss = { showFilterDialog = false },
                        onConfirm = { from, to ->
                            dateFromFilter.value = from
                            dateToFilter.value = to
                            showFilterDialog = false
                        },
                        onClear = {
                            dateFromFilter.value = ""
                            dateToFilter.value = ""
                            showFilterDialog = false
                        }
                    )
                }

                        val context = LocalContext.current
                        val coroutineScope = rememberCoroutineScope()

                        if (showCreateFolderDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            newFolderName = ""
                            newFolderError = null
                            showCreateFolderDialog = false
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val folderName = newFolderName.trim()
                                if (folderName.isEmpty()) {
                                    newFolderError = "Введите название папки"
                                    return@TextButton
                                }

                                if (folders.any { it.equals(folderName, ignoreCase = true) }) {
                                    newFolderError = "Папка с данным названием уже существует"
                                    return@TextButton
                                }

                                      val added = analysesStore.addFolder(folderName)
                                if (!added) {
                                    newFolderError = "Папка с данным названием уже существует"
                                    return@TextButton
                                }

                                newFolderError = null
                                newFolderName = ""
                                showCreateFolderDialog = false
                                      // persist folder to DB
                                      coroutineScope.launch {
                                          try {
                                              val repo = frontend.data.FoldersRepository(context)
                                              repo.insertFolder(folderName)
                                          } catch (_: Exception) {
                                              // ignore DB errors for now
                                          }
                                      }
                            }) {
                                Text("Добавить", color = SelectedColor)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                newFolderName = ""
                                newFolderError = null
                                showCreateFolderDialog = false
                            }) {
                                Text("Отмена", color = Color.Gray)
                            }
                        },
                        title = { Text("Новая папка", color = Color.White) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = newFolderName,
                                    onValueChange = {
                                        newFolderName = it
                                        newFolderError = null
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

                                newFolderError?.let {
                                    Text(
                                        text = it,
                                        color = Color(0xFFFF8A80),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        },
                        containerColor = CardDark
                    )
                }

                if (showIconPicker) {
                    IconPickerDialog(
                        icons = availableIcons,
                        onDismiss = { showIconPicker = false },
                        onPick = { icon ->
                            if (iconTargetType == "folder") {
                                folderIcons[iconTargetKey] = icon
                            } else if (iconTargetType == "analysis") {
                                analysisIcons[iconTargetKey] = icon
                            }
                            showIconPicker = false
                        }
                    )
                }

                ScrollbarLazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 0.dp,
                        bottom = 16.dp
                    )
                ) {
                    items(filteredAnalyses) { analysis ->
                        MedCard(
                            title = analysis.title,
                            subtitle = analysis.date,
                            iconResId = analysisIcons[analysis.id]
                                ?: defaultAnalysisIcon(analysis.title),
                            onClick = {
                                navController.navigate(
                                    Route.AnalysisProfile.createRoute(analysis.id)
                                )
                            },
                            onIconClick = {
                                iconTargetType = "analysis"
                                iconTargetKey = analysis.id
                                showIconPicker = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AnalysesScreenPreview() {
    AnalysesScreen(navController = rememberNavController())
}

private fun defaultAnalysisIcon(title: String): Int {
    return mapMedicalIconByKeywords(title)
}

private fun defaultFolderIcon(folder: String): Int {
    return mapMedicalIconByKeywords(folder)
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





