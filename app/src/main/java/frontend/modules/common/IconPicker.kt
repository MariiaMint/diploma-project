package frontend.modules.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mymedbook.R
import frontend.theme.SurfaceDark

object IconPickerDefaults {
    val availableIcons = listOf(
        R.drawable.analysis,
        R.drawable.ginecology,
        R.drawable.pill,
        R.drawable.heart,
        R.drawable.kolba,
        R.drawable.rentgen,
        R.drawable.shprits,
        R.drawable.tooth,
        R.drawable.urine,
        R.drawable.uzi,
        R.drawable.urology,
        R.drawable.sergion,
        R.drawable.oculist,
        R.drawable.neurolog,
        R.drawable.dermatolog,
        R.drawable.endocrinolog,
        R.drawable.gastro,
    )
}

@Composable
fun IconPickerDialog(
    icons: List<Int>,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.Gray)
            }
        },
        title = { Text("Выберите значок", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                icons.chunked(5).forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowIcons.forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onPick(icon) }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = "Доступный значок",
                                    tint = medicalIconTintFor(icon),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        },
        containerColor = SurfaceDark
    )
}

fun medicalIconTintFor(iconResId: Int): Color {
    return when (iconResId) {
        R.drawable.analysis -> Color(0xFFB085FA)
        R.drawable.ginecology -> Color(0xFFFF83AD)
        R.drawable.pill -> Color(0xFFFFE082)
        R.drawable.heart -> Color(0xFFF64F49)
        R.drawable.kolba -> Color(0xFF32E789)
        R.drawable.rentgen -> Color(0xFF0091FF)
        R.drawable.shprits -> Color(0xFFE50031)
        R.drawable.tooth -> Color(0xFFDCD0BE)
        R.drawable.urine -> Color(0xFFE7B216)
        R.drawable.uzi -> Color(0xFF58D6F6)
        R.drawable.urology -> Color(0xFF48FF1F)
        R.drawable.sergion -> Color(0xFF84EBFF)
        R.drawable.oculist -> Color(0xFF00ACC1)
        R.drawable.neurolog -> Color(0xFFFFA3EE)
        R.drawable.dermatolog -> Color(0xFFFFB06E)
        R.drawable.endocrinolog -> Color(0xFFFF6C1E)
        R.drawable.gastro -> Color(0xFFD53A8F)
        else -> Color(0xFF90A4AE)
    }
}

fun mapMedicalIconByKeywords(text: String): Int {
    val lower = text.lowercase()
    return when {
        "экг" in lower || "кардио" in lower -> R.drawable.heart
        "кров" in lower -> R.drawable.kolba
        "узи" in lower -> R.drawable.uzi
        "рентген" in lower || "мрт" in lower || "снимок" in lower -> R.drawable.rentgen
        "витамин" in lower -> R.drawable.pill
        "уролог" in lower -> R.drawable.urology
        "зуб" in lower || "стом" in lower -> R.drawable.tooth
        "глаз" in lower || "окул" in lower || "офтальм" in lower -> R.drawable.oculist
        "невро" in lower -> R.drawable.neurolog
        "дермат" in lower || "кож" in lower -> R.drawable.dermatolog
        "эндокрин" in lower || "гормон" in lower -> R.drawable.endocrinolog
        "гастро" in lower -> R.drawable.gastro
        "хирург" in lower || "операц" in lower -> R.drawable.sergion
        "таблетк" in lower -> R.drawable.pill
        "гинек" in lower -> R.drawable.ginecology
        "моч" in lower || "урин" in lower -> R.drawable.urine
        else -> R.drawable.analysis
    }
}



