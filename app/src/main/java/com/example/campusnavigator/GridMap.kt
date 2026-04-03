package com.example.campusnavigator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngQuad
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.tan

class PassabilityPoint(
    val x: Double,
    val y: Double,
    val passability: Int
)

data class GridCell(val row: Int, val col: Int)

class GridMap(
    val grid: Array<IntArray>,
    val height: Int,
    val width: Int,
    val minX: Double,
    val maxY: Double,
    val cellSize: Double
)

fun GridMap.createGridBitMap(): Bitmap {
    val scale = 10
    val bmpW = width * scale
    val bmpH = height * scale


    val bitmap = createBitmap(bmpW, bmpH)

    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.argb(130, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    for (i in 0..width) {
        val x = i * scale.toFloat()
        canvas.drawLine(x, 0f, x, bmpH.toFloat(), paint)
    }
    for (i in 0..height) {
        val y = i * scale.toFloat()
        canvas.drawLine(0f, y, bmpW.toFloat(), y, paint)
    }

    return bitmap
}

fun GridMap.getLatLngQuad(): LatLngQuad {
    val left = minX
    val right = minX + (width * cellSize)
    val top = maxY
    val bottom = maxY - (height * cellSize)

    return LatLngQuad(
        epsg3857ToLatLng(left, top),
        epsg3857ToLatLng(right, top),
        epsg3857ToLatLng(right, bottom),
        epsg3857ToLatLng(left, bottom)
    )
}

fun readPointsFromCsv(fileName: String, context: Context): List<PassabilityPoint> {
    val points = mutableListOf<PassabilityPoint>()

    context.assets.open(fileName)
        .bufferedReader()
        .useLines { lines ->
            lines.drop(1).forEach { line ->
                if (line.isNotBlank()) {
                    val parts = line.split(",")

                    points.add(
                        PassabilityPoint(
                            parts[0].toDouble(),
                            parts[1].toDouble(),
                            parts[2].toInt()
                        )
                    )
                }
            }

        }

    return points
}

fun buildGrid(points: List<PassabilityPoint>, cellSize: Double): GridMap {
    val minX = points.minOf { it.x }
    val maxY = points.maxOf { it.y }

    val indexedPoints = points.map { point ->
        val col = ((point.x - minX) / cellSize).roundToInt()
        val row = ((maxY - point.y) / cellSize).roundToInt()
        Triple(col, row, point.passability)
    }

    val width = indexedPoints.maxOf { it.first } + 1
    val height = indexedPoints.maxOf { it.second } + 1

    val grid = Array(height) { IntArray(width) { 0 } }

    for ((col, row, passability) in indexedPoints) {
        if (passability == -1) {
            grid[row][col] = 0
        } else {
            grid[row][col] = passability
        }
    }

    return GridMap(
        grid = grid,
        height = height,
        width = width,
        minX = minX,
        maxY = maxY,
        cellSize = cellSize
    )
}

fun makeGridFromCsv(fileName: String, context: Context): GridMap {
    val points = readPointsFromCsv(fileName, context)
    return buildGrid(points, 7.0)
}

fun epsg3857ToLatLng(x: Double, y: Double): LatLng {
    val lon = x / 20037508.34 * 180.0
    var lat = y / 20037508.34 * 180.0

    lat = 180.0 / PI * (2.0 * atan(exp(lat * PI / 180.0)) - PI / 2.0)

    return LatLng(lat, lon)
}

fun LatLngToEpsg3857(lat: Double, lon: Double): Pair<Double, Double> {
    val x = lon * 20037508.34 / 180.0

    var y = ln(tan((90.0 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0)
    y *= 20037508.34 / 180.0

    return Pair(x, y)
}

fun epsg3857ToGridCell(x: Double, y: Double, gridMap: GridMap): GridCell {
    val col = ((x - gridMap.minX) / gridMap.cellSize).toInt()
    val row = ((gridMap.maxY - y) / gridMap.cellSize).toInt()

    return GridCell(row, col)
}

fun gridCellToLatLng(row: Int, col: Int, gridMap: GridMap): LatLng {
    val x = gridMap.minX + (col + 0.5) * gridMap.cellSize
    val y = gridMap.maxY - (row + 0.5) * gridMap.cellSize
    return epsg3857ToLatLng(x, y)
}

fun isInsideGrid(cell: GridCell, gridMap: GridMap): Boolean {
    return cell.row in 0 until gridMap.height &&
            cell.col in 0 until gridMap.width
}

fun isWalkable(cell: GridCell, gridMap: GridMap): Boolean {
    return gridMap.grid[cell.row][cell.col] == 1
}

fun findNearestWalkableCell(
    tapped: GridCell,
    gridMap: GridMap,
    maxRadius: Int = 1
): GridCell? {
    if (isInsideGrid(tapped, gridMap) && isWalkable(tapped, gridMap)) {
        return tapped
    }

    var bestCell: GridCell? = null
    var bestDistance = Double.MAX_VALUE

    for (radius in 1..maxRadius) {
        for (row in tapped.row - radius..tapped.row + radius) {
            for (col in tapped.col - radius..tapped.col + radius) {
                val cell = GridCell(row, col)

                if (!isInsideGrid(cell, gridMap)) continue
                if (!isWalkable(cell, gridMap)) continue

                val dr = row - tapped.row
                val dc = col - tapped.col
                val distance = (dr * dr + dc * dc).toDouble()

                if (distance < bestDistance) {
                    bestDistance = distance
                    bestCell = cell
                }
            }
        }

        if (bestCell != null) {
            return bestCell
        }
    }

    return null
}