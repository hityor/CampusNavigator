package com.example.campusnavigator.algorithms

import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import kotlin.math.abs

fun heuristic(a: GridCell, b: GridCell): Int {
    return abs(a.row - b.row) + abs(a.col - b.col)
}

fun getWalkableNeighbors(cell: GridCell, grid: Array<IntArray>): List<GridCell> {
    val neighbors = mutableListOf<GridCell>()

    val directions = listOf(
        Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)
    )

    for ((dRow, dCol) in directions) {
        val newRow = cell.row + dRow
        val newCol = cell.col + dCol

        val isInsideGrid = newRow >= 0 && newRow < grid.size && newCol >= 0 && newCol < grid[0].size

        if (isInsideGrid && grid[newRow][newCol] == 1) {
            neighbors.add(GridCell(newRow, newCol))
        }
    }

    return neighbors
}

fun findPath(startCell: GridCell, finishCell: GridCell, gridMap: GridMap): List<GridCell>? {
    if (gridMap.grid[startCell.row][startCell.col] != 1) return null
    if (gridMap.grid[finishCell.row][finishCell.col] != 1) return null

    val openList = mutableListOf<GridCell>()
    val closedSet = mutableSetOf<GridCell>()
    val cameFrom = mutableMapOf<GridCell, GridCell>()
    val gScore = mutableMapOf<GridCell, Int>()

    openList.add(startCell)
    gScore[startCell] = 0

    while (openList.isNotEmpty()) {
        val current = openList.minBy { cell ->
            (gScore[cell] ?: Int.MAX_VALUE) + heuristic(cell, finishCell)
        }

        if (current == finishCell) {
            return reconstructPath(finishCell, cameFrom)
        }

        openList.remove(current)
        closedSet.add(current)

        val neighbors = getWalkableNeighbors(current, gridMap.grid)
        for (neighbor in neighbors) {
            if (neighbor in closedSet) continue

            val tentativeG = (gScore[current] ?: Int.MAX_VALUE) + 1
            if (tentativeG < (gScore[neighbor] ?: Int.MAX_VALUE)) {
                cameFrom[neighbor] = current
                gScore[neighbor] = tentativeG

                if (neighbor !in openList) openList.add(neighbor)
            }
        }
    }

    return null
}

fun reconstructPath(finishCell: GridCell, cameFrom: Map<GridCell, GridCell>): List<GridCell> {
    var current: GridCell? = finishCell
    val path = mutableListOf<GridCell>()

    while (current != null) {
        path.add(current)
        current = cameFrom[current]
    }

    return path.reversed()
}