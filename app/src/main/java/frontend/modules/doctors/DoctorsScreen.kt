package frontend.modules.doctors

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import frontend.modules.common.MedCard
import frontend.modules.BackgroundScreen
import frontend.modules.common.IconPickerDefaults
import frontend.modules.common.IconPickerDialog
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.theme.ButtonColor
import frontend.theme.SurfaceDark
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.mymedbook.R
import frontend.theme.Primary
import androidx.compose.ui.graphics.Color
import frontend.modules.bars.ExpandableHeader
import frontend.navigation.Route
import frontend.modules.common.ScrollbarLazyColumn

@Composable
fun DoctorsScreen(navController: NavController) {
    BackgroundScreen {
        val doctors = listOf(
            Pair("Иван Иванов", "Терапевт"),
            Pair("Анна Петрова", "Кардиолог"),
            Pair("Сергей Смирнов", "Невролог"),
            Pair("Виктория Сидорова", "Уролог"),
            Pair("Петр Волков", "Хирург"),
            Pair("Мария Соколова", "Дерматолог"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "терапевт"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "Хирург"),
            Pair("Петр Волков", "Хирург")
        )

        val searchQuery = remember { mutableStateOf("") }
        var showIconPicker by remember { mutableStateOf(false) }
        var iconTargetKey by remember { mutableStateOf("") }

        val availableIcons = IconPickerDefaults.availableIcons

        val doctorIcons = remember {
            mutableStateMapOf<String, Int>().apply {
                doctors.forEach { (name, spec) ->
                    put("$name|$spec", defaultDoctorIcon(spec))
                }
            }
        }

        val filteredDoctors = doctors.filter { (name, specialization) ->
            name.contains(searchQuery.value, ignoreCase = true) ||
                    specialization.contains(searchQuery.value, ignoreCase = true)
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Выдвижной хедер поверх контента
            ExpandableHeader(title = "Врачи")

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = SurfaceDark,
                                shape = RoundedCornerShape(32.dp)
                            ),
                        placeholder = { Text("Поиск по имени или специальности...") },
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
                        onClick = { navController.navigate(Route.DoctorProfile.createRoute(isNew = true)) },
                        modifier = Modifier
                            .size(48.dp),
//                        .border(width = 3.dp, color = SurfaceDark, shape = CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Добавить",
                            modifier = Modifier.size(28.dp),
                            tint = Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                    items(filteredDoctors) { (doctorName, specialization) ->
                        val doctorKey = "$doctorName|$specialization"
                        MedCard(
                            title = doctorName,
                            subtitle = specialization,
                            iconResId = doctorIcons[doctorKey] ?: defaultDoctorIcon(specialization),
                            onIconClick = {
                                iconTargetKey = doctorKey
                                showIconPicker = true
                            },
                            onClick = {
                                navController.navigate(Route.DoctorProfile.createRoute(isNew = false))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (showIconPicker) {
                    IconPickerDialog(
                        icons = availableIcons,
                        onDismiss = { showIconPicker = false },
                        onPick = { icon ->
                            doctorIcons[iconTargetKey] = icon
                            showIconPicker = false
                        }
                    )
                }
            }
        }
    }
}

private fun defaultDoctorIcon(specialization: String): Int {
    return mapMedicalIconByKeywords(specialization)
}


@Preview
@Composable
fun DoctorsScreenPreview() {
    DoctorsScreen(navController = rememberNavController())
}
