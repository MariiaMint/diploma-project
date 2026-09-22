package frontend.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import frontend.theme.Primary
import frontend.theme.SelectedColor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import frontend.modules.common.MedCard
import frontend.modules.bars.ExpandableHeader
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.modules.common.medicalIconTintFor
import frontend.modules.common.ScrollbarLazyColumn
import frontend.theme.ArianFontFamily
import frontend.theme.ButtonColor
import frontend.theme.CardDark
import frontend.theme.SurfaceDark

@Composable
fun MainScreen(_navController: NavController) {
    _navController.hashCode()
    BackgroundScreen {
        // Ближайшие события
        val upcomingEvents = listOf(
            Pair("Прием у кардиолога", "15.03.2026"),
            Pair("УЗИ органов брюшной полости", "18.03.2026"),
            Pair("Сдача анализов", "20.03.2026"),
            Pair("Прием у уролога", "22.03.2026"),
            Pair("Прием у дерматолога", "25.03.2026"),
            Pair("Прием у невролога", "28.03.2026")
        )


        val reminders = remember {
            mutableStateOf(
                listOf(
                    "Принять таблетки" to false,
                    "Сдать анализ крови" to false,
                    "Измерить давление" to false,
                    "Проверить уровень сахара" to false,
                    "Сделать зарядку" to false,
                    "Пойти на прогулку" to false,
                    "Проверить вес" to false,
                    "Проверить уровень холестерина" to false,
                    "Проверить уровень витамина D" to false,
                    "Проверить уровень магния" to false,

                )
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Выдвижной хедер поверх контента
            ExpandableHeader(title = "Главная")

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    "Ближайшие события",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    color = CardDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(16.dp))
//                    .background(color = ButtonColor)
                ) {
                    ScrollbarLazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        items(upcomingEvents.size) { index ->
                            val eventTitle = upcomingEvents[index].first
                            val iconRes = mapMedicalIconByKeywords(eventTitle)
                            MedCard(
                                title = eventTitle,
                                subtitle = upcomingEvents[index].second,
                                iconResId = iconRes
                            )
                            if (index < upcomingEvents.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ButtonColor)
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Напоминания",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        ScrollbarLazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            scrollbarColor = Color(0xFF7E7E7E),
                        ) {
                            items(reminders.value.size) { index ->
                                val (title, isChecked) = reminders.value[index]
                                val iconRes = mapMedicalIconByKeywords(title)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(25.dp))
                                        .background(ButtonColor)
                                        .padding(horizontal = 10.dp, vertical = 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = "Иконка напоминания",
                                        tint = medicalIconTintFor(iconRes),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        title,
                                        fontSize = 20.sp,
                                        fontFamily = ArianFontFamily,
                                        color = SurfaceDark,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { newValue ->
                                            reminders.value =
                                                reminders.value.mapIndexed { i, (t, c) ->
                                                    if (i == index) t to newValue else t to c
                                                }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SelectedColor,
                                            uncheckedColor = Primary,
                                            checkmarkColor = Color.White
                                        ),
                                        modifier = Modifier.padding(start = 8.dp)
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


//@Preview
//@Composable
//fun MainScreenPreview() {
//    MainScreen(navController = rememberNavController())
//}
