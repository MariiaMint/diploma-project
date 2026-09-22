package frontend.modules.common


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import frontend.theme.SurfaceDark

@Composable
fun MedCard(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    iconResId: Int? = null,
    onIconClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(
        topEnd = 48.dp,
        topStart = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(width = 2.dp, color = SurfaceDark, shape = RoundedCornerShape(topEnd = 46.dp, topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .clip(cardShape)
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconResId != null) {
                    Box(
                        modifier = Modifier
//                            .clip(CircleShape)
                            .clickable(enabled = onIconClick != null) { onIconClick?.invoke() }
                            .padding(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = "Иконка анализа",
                            tint = medicalIconTintFor(iconResId),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            subtitle?.let {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}