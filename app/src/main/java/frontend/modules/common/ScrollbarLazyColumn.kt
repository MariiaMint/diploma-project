package frontend.modules.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
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
fun ScrollbarLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollbarWidth: Dp = 3.dp,
    scrollbarMinHeight: Dp = 36.dp,
    scrollbarColor: Color = Color(0xFF9A9A9A),
    alwaysShowScrollbar: Boolean = false,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = scrollbarWidth + 6.dp),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )

        LazyColumnScrollbar(
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

@Composable
private fun LazyColumnScrollbar(
    state: LazyListState,
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
    val avgItemSize = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
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
        val scrolledPx = (firstVisible.index * avgItemSize) + firstVisible.offset.coerceAtLeast(0)
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


