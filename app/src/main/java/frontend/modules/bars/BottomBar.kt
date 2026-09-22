package frontend.modules.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mymedbook.R
import frontend.theme.Accent2
import frontend.theme.Primary
import frontend.theme.SelectedColor

@Composable
fun BottomBar(navController: NavController, currentRoute: String?) {
    val selectedColor = SelectedColor
    val unselectedColor = MaterialTheme.colorScheme.onPrimary

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(

                        Primary,
                        Primary,
                        Primary,
                        Primary,
                        Primary,
                        Primary,
                        Color(101, 147, 150, 238),
                        Color(105, 159, 164, 211),
                        Color(121, 106, 203, 220),
                        Accent2.copy(alpha = 0.88f),
                        Color(176, 160, 78, 255),

                    )
                )
            ),
        containerColor = Color.Transparent
    ) {
        NavigationBarItem(
            selected = currentRoute == "main",
            onClick = { navController.navigate("main") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Главная",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 4.dp),
                    tint = if (currentRoute == "main") selectedColor else unselectedColor
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == "doctors",
            onClick = { navController.navigate("doctors") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.stethoscope),
                    contentDescription = "Врачи",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 4.dp),
                    tint = if (currentRoute == "doctors") selectedColor else unselectedColor
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == "analyses",
            onClick = { navController.navigate("analyses") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.analyses),
                    contentDescription = "Анализы",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 4.dp),
                    tint = if (currentRoute == "analyses") selectedColor else unselectedColor
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == "conclusions",
            onClick = { navController.navigate("conclusions") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.conclusions),
                    contentDescription = "Заключения",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 4.dp),
                    tint = if (currentRoute == "conclusions") selectedColor else unselectedColor
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == "calendar",
            onClick = { navController.navigate("calendar") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = "Календарь",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 4.dp),
                    tint = if (currentRoute == "calendar") selectedColor else unselectedColor
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    BottomBar(navController = rememberNavController(), currentRoute = "main")
}
