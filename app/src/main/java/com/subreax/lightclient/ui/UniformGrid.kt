package com.subreax.lightclient.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.BitSet


data class GridSpan(val x: Int, val y: Int)

interface UniformGridScope {
    @Stable
    fun Modifier.span(horizontal: Int = 1, vertical: Int = 1) = this.then(
        GridItemData(horizontal, vertical)
    )

    @Stable
    fun Modifier.span(span: GridSpan) = span(span.x, span.y)

    companion object : UniformGridScope
}



private data class GridItemData(
    val spanX: Int,
    val spanY: Int
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@GridItemData
}

private val Measurable.gridItemData: GridItemData
    get() = (parentData as? GridItemData) ?: GridItemData(1, 1)


private class Grid(private val columns: Int) {
    private val bitSet = BitSet()

    operator fun get(x: Int, y: Int): Boolean {
        return bitSet[y * columns + x]
    }

    fun isFree(x: Int, y: Int): Boolean {
        return x < columns && !bitSet[y * columns + x]
    }

    fun fits(x: Int, y: Int, span: GridItemData): Boolean {
        for (i in x until (x + span.spanX)) {
            for (j in y until (y + span.spanY)) {
                if (!isFree(i, j)) {
                    return false
                }
            }
        }
        return true
    }

    fun occupy(x: Int, y: Int) {
        bitSet[y * columns + x] = true
    }

    fun occupy(x: Int, y: Int, span: GridItemData) {
        for (i in x until (x + span.spanX)) {
            for (j in y until (y + span.spanY)) {
                occupy(i, j)
            }
        }
    }
}

private data class GridItemPosition(val x: Int, val y: Int)

private fun calcSpan(span: Int, cellSize: Int, spacing: Int): Int {
    return cellSize + (cellSize + spacing) * (span - 1)
}

private data class GridCalculationResult(val rows: Int, val itemsPositions: List<GridItemPosition>)

private fun calcGrid(columnsCount: Int, measurables: List<Measurable>): GridCalculationResult {
    val grid = Grid(columnsCount)
    val itemsPos = mutableListOf<GridItemPosition>()
    var rowsCount = if (measurables.isNotEmpty()) 1 else 0
    for (item in measurables) {
        val itemData = item.gridItemData

        var y = 0
        var isPlaced = false
        while (!isPlaced) {
            for (x in 0 until columnsCount) {
                if (grid.fits(x, y, itemData)) {
                    grid.occupy(x, y, itemData)
                    itemsPos.add(GridItemPosition(x, y))
                    isPlaced = true
                    rowsCount = maxOf(y+itemData.spanY, rowsCount)
                    break
                }
            }
            y += 1
        }
    }

    return GridCalculationResult(rowsCount, itemsPos)
}

@Composable
fun UniformGrid(
    modifier: Modifier = Modifier,
    minCellSize: Dp = 96.dp,
    spacing: Dp = 8.dp,
    content: @Composable UniformGridScope.() -> Unit
) {
    Layout(
        modifier = modifier,
        content = { UniformGridScope.content() }
    ) { measurables, constraints ->
        val maxW = constraints.maxWidth
        val spacingPx = spacing.toPx().toInt()
        val columnsCount = ((maxW + spacingPx) / (minCellSize.toPx() + spacingPx)).toInt()
        // todo: keep at least 1 column even when minCellSize is too big
        check(columnsCount >= 1) {
            "Columns count = 0. Decrease spacing or min cell size to fix it"
        }

        val ( rowsCount, itemsPos ) = calcGrid(columnsCount, measurables)

        val cellSize = (maxW - spacingPx * (columnsCount - 1)) / columnsCount
        val placeables = measurables.map {
            val cellConstraints = Constraints.fixed(
                calcSpan(it.gridItemData.spanX, cellSize, spacingPx),
                calcSpan(it.gridItemData.spanY, cellSize, spacingPx)
            )
            it.measure(cellConstraints)
        }

        val maxH = calcSpan(rowsCount, cellSize, spacingPx)

        layout(maxW, maxH) {
            for (i in placeables.indices) {
                val placeable = placeables[i]
                val pos = itemsPos[i]
                val x = pos.x * (cellSize + spacingPx)
                val y = pos.y * (cellSize + spacingPx)
                placeable.place(x, y)
            }
        }
    }
}
