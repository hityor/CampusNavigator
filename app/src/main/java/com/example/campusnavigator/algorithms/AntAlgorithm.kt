package com.example.campusnavigator.algorithms

import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.isWalkable
import kotlin.math.pow
import kotlin.random.Random

enum class AntState {
    SEARCHING,
    RETURNING
}

data class Ant(
    var position: GridCell,
    var state: AntState = AntState.SEARCHING,
    val path: MutableList<GridCell> = mutableListOf(),
    var previousCell: GridCell? = null
)

data class AntResult(
    val totalStudentsPlaced: Int,
    val locationLoads: List<Int>,
    val paths: List<List<GridCell>>
)

class AntColonyOptimization(
    private val gridMap: GridMap,
    private val homeCell: GridCell,
    private val locations: List<GridCell>,
    private val locationComforts: List<Double>,
    private val locationCapacities: List<Int>,
    private val totalStudentsToPlace: Int,
    private val numAnts: Int = 100,
    private val evaporationRate: Double = 0.01,
    private val homePheromoneDeposit: Double = 8.0,
    private val foodPheromoneDeposit: Double = 15.0,
    private val alpha: Double = 1.0,
    private val beta: Double = 1.0,
    private val random: Random = Random,
    val onStep: ((
        homePheromone: Array<DoubleArray>,
        foodPheromone: Array<DoubleArray>,
        ants: List<Ant>,
        placedCount: Int
    ) -> Unit)? = null
) {
    private val height = gridMap.height
    private val width = gridMap.width

    private val homePheromone = Array(height) { DoubleArray(width) { 1.0 } }
    private val foodPheromone = Array(height) { DoubleArray(width) { 1.0 } }

    private val remainingCapacity = locationCapacities.toMutableList()
    private val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

    private val ants = List(numAnts) {
        Ant(position = homeCell).apply { path.add(homeCell) }
    }

    private var placedStudents = 0
    private val successfulPaths = mutableListOf<List<GridCell>>()
    private val loads = MutableList(locations.size) { 0 }

    private fun hasAvailableLocation(): Boolean {
        return locations.indices.any { remainingCapacity[it] > 0 }
    }

    private fun getAvailableLocations(): List<Int> {
        return locations.indices.filter { remainingCapacity[it] > 0 }
    }

    private fun manhattanDistance(a: GridCell, b: GridCell): Int {
        return kotlin.math.abs(a.row - b.row) + kotlin.math.abs(a.col - b.col)
    }

    private fun searchHeuristic(from: GridCell, to: GridCell): Double {
        var attraction = 1.0
        val available = getAvailableLocations()
        if (available.isEmpty()) return 1.0
        for (locIdx in available) {
            val target = locations[locIdx]
            val dist = manhattanDistance(to, target)
            if (dist > 0) {
                attraction += locationComforts[locIdx] / dist
            }
        }
        return attraction
    }

    private fun chooseNextCell(ant: Ant): GridCell? {
        val current = ant.position
        val neighbors = directions.mapNotNull { (dr, dc) ->
            val nr = current.row + dr
            val nc = current.col + dc
            if (nr in 0 until height && nc in 0 until width && isWalkable(GridCell(nr, nc), gridMap)) {
                GridCell(nr, nc)
            } else null
        }.filter { it != ant.previousCell }

        if (neighbors.isEmpty()) return null

        val probabilities = DoubleArray(neighbors.size)
        var sum = 0.0
        for (i in neighbors.indices) {
            val cell = neighbors[i]
            val tau = when (ant.state) {
                AntState.SEARCHING -> foodPheromone[cell.row][cell.col]
                AntState.RETURNING -> homePheromone[cell.row][cell.col]
            }.pow(alpha)
            val eta = when (ant.state) {
                AntState.SEARCHING -> searchHeuristic(current, cell)
                AntState.RETURNING -> 1.0 / (manhattanDistance(cell, homeCell) + 1.0)
            }.pow(beta)
            val p = tau * eta
            probabilities[i] = p
            sum += p
        }
        if (sum <= 0) return neighbors.random(random)

        var r = random.nextDouble() * sum
        for (i in neighbors.indices) {
            r -= probabilities[i]
            if (r <= 0) return neighbors[i]
        }
        return neighbors.last()
    }

    fun run(): AntResult {
        var step = 0
        val maxSteps = 10000

        while (placedStudents < totalStudentsToPlace && hasAvailableLocation() && step < maxSteps) {
            ants.shuffled(random).forEach { ant ->
                if (placedStudents >= totalStudentsToPlace) return@forEach

                val next = chooseNextCell(ant) ?: return@forEach

                ant.previousCell = ant.position
                ant.position = next
                ant.path.add(next)

                when (ant.state) {
                    AntState.SEARCHING -> homePheromone[next.row][next.col] += homePheromoneDeposit
                    AntState.RETURNING -> foodPheromone[next.row][next.col] += foodPheromoneDeposit
                }

                when (ant.state) {
                    AntState.SEARCHING -> {
                        val locIndex = locations.indexOfFirst { it == next }
                        if (locIndex != -1 && remainingCapacity[locIndex] > 0) {
                            remainingCapacity[locIndex]--
                            loads[locIndex]++
                            placedStudents++
                            ant.state = AntState.RETURNING
                            successfulPaths.add(ant.path.toList())
                            ant.path.clear()
                            ant.path.add(next)
                            ant.previousCell = null
                        }
                    }
                    AntState.RETURNING -> {
                        if (next == homeCell) {
                            ant.state = AntState.SEARCHING
                            ant.path.clear()
                            ant.path.add(homeCell)
                            ant.previousCell = null
                        }
                    }
                }
            }

            for (r in 0 until height) {
                for (c in 0 until width) {
                    homePheromone[r][c] *= (1.0 - evaporationRate)
                    foodPheromone[r][c] *= (1.0 - evaporationRate)
                }
            }

            onStep?.invoke(homePheromone, foodPheromone, ants, placedStudents)
            step++
        }

        return AntResult(
            totalStudentsPlaced = placedStudents,
            locationLoads = loads,
            paths = successfulPaths
        )
    }
}