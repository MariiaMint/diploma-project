package frontend.modules.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
fun ScrollbarLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrollbarWidth: Dp = 3.dp,
    scrollbarMinHeight: Dp = 36.dp,
    scrollbarColor: Color = Color(0xFF9A9A9A),
    alwaysShowScrollbar: Boolean = false,
    content: LazyGridScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = columns,
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = scrollbarWidth + 6.dp),
            contentPadding = contentPadding,
            content = content
        )

        LazyGridScrollbar(
            state = state,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            width = scrollbarWidth,
            minHeight = scrollbarMinHeight,
            color = scrollbarColor,
            alwaysShowScrollbar = alwaysShowScrollbar
        )
    }
}

@Suppress("FrequentlyChangingValue")
@Composable
private fun LazyGridScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    width: Dp = 3.dp,
    minHeight: Dp = 36.dp,
    color: Color = Color(0xFF9A9A9A),
    alwaysShowScrollbar: Boolean = false
) {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val hasOverflow = layoutInfo.totalItemsCount > visibleItems.size && visibleItems.isNotEmpty()
    if (!hasOverflow && !alwaysShowScrollbar) return

    val density = LocalDensity.current
    val minThumbHeightPx = with(density) { minHeight.toPx() }

    val viewportHeight = layoutInfo.viewportSize.height.toFloat().coerceAtLeast(1f)
    val avgItemSize = visibleItems.map { it.size.height }.average().toFloat().coerceAtLeast(1f)
    val estimatedContentHeight = max(viewportHeight, avgItemSize * layoutInfo.totalItemsCount)
    val trackHeight = viewportHeight
    val thumbHeight = if (hasOverflow) {
        max(minThumbHeightPx, (viewportHeight * viewportHeight / estimatedContentHeight))
    } else {
        max(minThumbHeightPx, trackHeight * 0.22f)
    }

    val thumbOffset = if (hasOverflow) {
        val maxScroll = max(1f, estimatedContentHeight - viewportHeight)
        val firstVisible = visibleItems.first()
        val scrolledPx = (firstVisible.index * avgItemSize) + firstVisible.offset.y.coerceAtLeast(0)
        val progress = (scrolledPx / maxScroll).coerceIn(0f, 1f)
        (trackHeight - thumbHeight) * progress
    } else {
        0f
    }

    Canvas(modifier = modifier.width(width)) {
        drawRoundRect(
            color = color.copy(alpha = 0.12f),
            topLeft = Offset(x = size.width / 2f - size.width / 6f, y = 0f),
            size = Size(size.width / 3f, size.height),
            cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(x = 0f, y = thumbOffset),
            size = Size(size.width, thumbHeight.roundToInt().toFloat()),
            cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
        )
    }
}


