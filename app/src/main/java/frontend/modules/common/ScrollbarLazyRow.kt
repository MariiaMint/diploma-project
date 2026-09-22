package frontend.modules.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ScrollbarLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    scrollbarHeight: Dp = 3.dp,
    scrollbarMinWidth: Dp = 36.dp,
    scrollbarColor: Color = Color(0xFF9A9A9A),
    alwaysShowScrollbar: Boolean = false,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyRow(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = scrollbarHeight + 6.dp),
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )

        LazyRowScrollbar(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            height = scrollbarHeight,
            minWidth = scrollbarMinWidth,
            color = scrollbarColor,
            alwaysShowScrollbar = alwaysShowScrollbar
        )
    }
}

@Composable
private fun LazyRowScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    minWidth: Dp = 36.dp,
    color: Color = Color(0xFF9A9A9A),
    alwaysShowScrollbar: Boolean = false
) {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val hasOverflow = layoutInfo.totalItemsCount > visibleItems.size && visibleItems.isNotEmpty()
    if (!hasOverflow && !alwaysShowScrollbar) return

    val density = LocalDensity.current
    val minThumbWidthPx = with(density) { minWidth.toPx() }

    val viewportWidth = layoutInfo.viewportSize.width.toFloat().coerceAtLeast(1f)
    val avgItemSize = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
    val estimatedContentWidth = max(viewportWidth, avgItemSize * layoutInfo.totalItemsCount)
    val trackWidth = viewportWidth
    val thumbWidth = if (hasOverflow) {
        max(minThumbWidthPx, (viewportWidth * viewportWidth / estimatedContentWidth))
    } else {
        max(minThumbWidthPx, trackWidth * 0.22f)
    }

    val thumbOffset = if (hasOverflow) {
        val maxScroll = max(1f, estimatedContentWidth - viewportWidth)
        val scrolledPx = (state.firstVisibleItemIndex * avgItemSize) + state.firstVisibleItemScrollOffset
        val progress = (scrolledPx / maxScroll).coerceIn(0f, 1f)
        (trackWidth - thumbWidth) * progress
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
    ) {
        drawRoundRect(
            color = color.copy(alpha = 0.12f),
            topLeft = Offset(x = 0f, y = size.height / 2f - size.height / 6f),
            size = Size(size.width, size.height / 3f),
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(x = thumbOffset, y = 0f),
            size = Size(thumbWidth.roundToInt().toFloat(), size.height),
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
        )
    }
}

