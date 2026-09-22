package frontend.modules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.mymedbook.R
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale

@Composable
fun BackgroundScreen(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bk3),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(0.dp),
            contentScale = ContentScale.Crop
        )
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.verticalGradient(
//                        0.0f to Color(0x95DED1AB),
//                        0.1f to Color(0x00FAF2E7),
//                        0.9f to Color(0x00FAF2E7),
//                        1.0f to Color(0x8ACCBF99)
//                    )
//                )
//                .zIndex(1f)
//        )
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.horizontalGradient(
//                        0.0f to Color(0x63C79C3E),
//                        0.1f to Color(0x00FFFEF7),
//                        0.9f to Color(0x00FAF2E7),
//                        1f to Color(0x63C79C3E)
//                    )
//                )
//                .zIndex(1.1f)
//        )
        Box(modifier = Modifier
            .fillMaxSize()
            .zIndex(2f)) {
            content()
        }
    }
}
