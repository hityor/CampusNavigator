package com.example.campusnavigator

import android.content.Context
import kotlin.math.roundToInt

class PassabilityPoint(
    val x: Double,
    val y: Double,
    val passability: Int
)

class GridMap(
    val grid: Array<IntArray>,
    val height: Int,
    val width: Int,
    val minX: Double,
    val maxY: Double,
    val cellSize: Double
)

fun readPointsFromCsv(fileName: String, context: Context): List<PassabilityPoint> {
    val points = mutableListOf<PassabilityPoint>()

    context.assets.open(fileName)
        .bufferedReader()
        .useLines() { lines ->
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