package frontend.modules.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mymedbook.R
import frontend.theme.Accent2
import frontend.theme.Primary
import frontend.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(navController: NavController, currentRoute: String?) {

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
//                        Primary,
//                        Primary,
//                        Primary,
//                        Primary,
//                        Primary,
//                        Primary,
//                        Color(101, 147, 150, 238),
//                        Color(100, 144, 148, 211),
//                        Color(121, 106, 203, 220),
//                        Accent2.copy(alpha = 0.88f),
                        Color(176, 160, 78, 255),
                        Accent2.copy(alpha = 0.88f),
                        Color(121, 106, 203, 220),
                        Color(100, 144, 148, 211),
                        Color(101, 147, 150, 238),
                        Primary,
                        Primary,
                        Primary,
                        Primary,
                        Primary,
                        Primary,

                    )
                )
            ),
        title = { Text("", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary) },
        navigationIcon = {
            if (navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = "Назад",
                        modifier = Modifier.size(18.dp),
                        tint = TextPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )

}
