package com.example.campusnavigator.algorithms

import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import java.util.PriorityQueue
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

suspend fun findPathWithStepsStreaming(
    startCell: GridCell,
    finishCell: GridCell,
    gridMap: GridMap,
    extraObstacles: Set<GridCell> = emptySet(),
    onStep: suspend (visited: Set<GridCell>, current: GridCell) -> Unit
): List<GridCell>? {

    if (gridMap.grid[startCell.row][startCell.col] != 1) return null
    if (gridMap.grid[finishCell.row][finishCell.col] != 1) return null
    if (startCell in extraObstacles || finishCell in extraObstacles) return null


    val gScore = mutableMapOf<GridCell, Int>()
    val openQueue = PriorityQueue<GridCell>(compareBy { cell ->
        (gScore[cell] ?: Int.MAX_VALUE) + heuristic(cell, finishCell)
    })
    val closedSet = mutableSetOf<GridCell>()
    val cameFrom = mutableMapOf<GridCell, GridCell>()

    openQueue.add(startCell)
    gScore[startCell] = 0

    var iterationCount = 0
    val reportEvery = 10

    while (openQueue.isNotEmpty()) {
        val current = openQueue.poll()!!

        if (current in closedSet) continue
        closedSet.add(current)

        if (iterationCount % reportEvery == 0) {
            onStep(closedSet, current)
        }
        iterationCount++

        if (current == finishCell) {
            return reconstructPath(finishCell, cameFrom)
        }

        val neighbors = getWalkableNeighbors(current, gridMap.grid)
            .filter { it !in extraObstacles }

        for (neighbor in neighbors) {
            if (neighbor in closedSet) continue
            val tentativeG = (gScore[current] ?: Int.MAX_VALUE) + 1
            if (tentativeG < (gScore[neighbor] ?: Int.MAX_VALUE)) {
                cameFrom[neighbor] = current
                gScore[neighbor] = tentativeG
                openQueue.add(neighbor)
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