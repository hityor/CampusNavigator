package com.example.campusnavigator.algorithms

import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.screens.map.models.FoodPlace
import kotlinx.coroutines.delay
import kotlin.random.Random

data class GeneticRoute(
    val visitedPlaceIndices: List<Int>,
    val purchasedItems: Set<String>,
    val missingItems: Set<String>,
    val totalDistanceCells: Int,
    val totalMinutes: Int,
    val segments: List<List<GridCell>>
)

data class GeneticGeneration(
    val generation: Int,
    val bestFitness: Double,
    val bestRoute: GeneticRoute
)
class GeneticAlgorithm(
    private val gridMap: GridMap,
    private val startCell: GridCell,
    private val places: List<FoodPlace>,
    private val placeCells: List<GridCell>,
    private val requiredItems: Set<String>,
    private val currentMinuteOfDay: Int,
    private val populationSize: Int = 40,
    private val generations: Int = 40,
    private val mutationRate: Double = 0.25,
    private val eliteCount: Int = 2,
    private val tournamentSize: Int = 3,
    private val cellMeters: Double = 7.0,
    private val walkingSpeedKmh: Double = 5.0,
    private val random: Random = Random,
    private val onGeneration: (suspend (GeneticGeneration) -> Unit)? = null
) {

    private val candidateIndices: List<Int> = places.indices.filter { i ->
        places[i].isOpenAt(currentMinuteOfDay) &&
                places[i].menu.any { it in requiredItems }
    }

    private lateinit var distance: Array<IntArray>
    private lateinit var paths: Array<Array<List<GridCell>?>>

    private fun buildDistanceMatrix() {
        val size = candidateIndices.size + 1
        distance = Array(size) { IntArray(size) { 0 } }
        paths = Array(size) { arrayOfNulls<List<GridCell>?>(size) }

        val nodes: List<GridCell> = listOf(startCell) +
                candidateIndices.map { placeCells[it] }

        for (i in 0 until size) {
            for (j in 0 until size) {
                if (i == j) {
                    distance[i][j] = 0
                    paths[i][j] = listOf(nodes[i])
                    continue
                }
                val p = findPathAStar(nodes[i], nodes[j], gridMap)
                if (p == null) {
                    distance[i][j] = -1
                    paths[i][j] = null
                } else {
                    distance[i][j] = p.size - 1
                    paths[i][j] = p
                }
            }
        }
    }

    private fun decode(chromosome: IntArray): GeneticRoute {
        val visited = mutableListOf<Int>()
        val segments = mutableListOf<List<GridCell>>()
        val bought = mutableSetOf<String>()
        var totalCells = 0
        var totalMinutes = 0
        var currentNode = 0
        var currentTime = currentMinuteOfDay

        for (geneIdx in chromosome) {
            if (bought.containsAll(requiredItems)) break

            val placeIdx = candidateIndices[geneIdx]
            val place = places[placeIdx]

            val useful = place.menu.any { it in requiredItems && it !in bought }
            if (!useful) continue

            val nextNode = geneIdx + 1
            val dist = distance[currentNode][nextNode]
            if (dist < 0) {
                continue
            }

            val minutes = cellsToMinutes(dist)
            val arrivalTime = currentTime + minutes
            if (!place.isOpenAt(arrivalTime)) {
                continue
            }

            val seg = paths[currentNode][nextNode] ?: continue
            segments.add(seg)
            totalCells += dist
            totalMinutes += minutes
            currentTime = arrivalTime
            currentNode = nextNode

            visited.add(placeIdx)
            bought.addAll(place.menu.intersect(requiredItems))
        }

        val missing = requiredItems - bought

        return GeneticRoute(
            visitedPlaceIndices = visited,
            purchasedItems = bought,
            missingItems = missing,
            totalDistanceCells = totalCells,
            totalMinutes = totalMinutes,
            segments = segments
        )
    }

    private fun cellsToMinutes(cells: Int): Int {
        val meters = cells * cellMeters
        val hours = meters / 1000.0 / walkingSpeedKmh
        return (hours * 60).toInt()
    }

    private fun fitness(route: GeneticRoute): Double {
        val missingPenalty = route.missingItems.size * 10_000.0
        val timeCost = route.totalMinutes.toDouble()

        var urgencyBonus = 0.0
        for (placeIdx in route.visitedPlaceIndices) {
            val place = places[placeIdx]
            val left = place.minutesUntilClose(currentMinuteOfDay)

            if (left in 1..60) {
                urgencyBonus += (60 - left)
            }
        }

        return missingPenalty + timeCost - urgencyBonus
    }

    private fun randomChromosome(): IntArray {
        val arr = IntArray(candidateIndices.size) { it }
        for (i in arr.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val t = arr[i]; arr[i] = arr[j]; arr[j] = t
        }
        return arr
    }

    private fun tournamentSelect(
        pop: List<IntArray>,
        fits: DoubleArray
    ): IntArray {
        var bestIdx = random.nextInt(pop.size)
        for (k in 1 until tournamentSize) {
            val idx = random.nextInt(pop.size)
            if (fits[idx] < fits[bestIdx]) bestIdx = idx
        }
        return pop[bestIdx].copyOf()
    }

    private fun crossover(a: IntArray, b: IntArray): IntArray {
        val size = a.size
        if (size < 2) return a.copyOf()

        val start = random.nextInt(size)
        val end = random.nextInt(size)
        val lo = minOf(start, end)
        val hi = maxOf(start, end)

        val child = IntArray(size) { -1 }
        val taken = BooleanArray(size)
        for (i in lo..hi) {
            child[i] = a[i]
            taken[a[i]] = true
        }

        var writeIdx = (hi + 1) % size
        var readIdx = (hi + 1) % size
        while (writeIdx != lo) {
            val gene = b[readIdx]
            if (!taken[gene]) {
                child[writeIdx] = gene
                taken[gene] = true
                writeIdx = (writeIdx + 1) % size
            }
            readIdx = (readIdx + 1) % size
        }
        return child
    }

    private fun mutate(chromosome: IntArray) {
        if (chromosome.size < 2) return
        if (random.nextDouble() >= mutationRate) return
        val i = random.nextInt(chromosome.size)
        var j = random.nextInt(chromosome.size)
        while (j == i) j = random.nextInt(chromosome.size)
        val t = chromosome[i]; chromosome[i] = chromosome[j]; chromosome[j] = t
    }

    suspend fun run(): GeneticRoute {
        if (requiredItems.isEmpty() || candidateIndices.isEmpty()) {
            return GeneticRoute(
                visitedPlaceIndices = emptyList(),
                purchasedItems = emptySet(),
                missingItems = requiredItems.toSet(),
                totalDistanceCells = 0,
                totalMinutes = 0,
                segments = emptyList()
            )
        }

        buildDistanceMatrix()

        var population = List(populationSize) { randomChromosome() }
        var fits = DoubleArray(populationSize) { 0.0 }
        var decoded = arrayOfNulls<GeneticRoute>(populationSize)

        fun evaluate(pop: List<IntArray>): Triple<DoubleArray, Array<GeneticRoute?>, Int> {
            val f = DoubleArray(pop.size)
            val d = arrayOfNulls<GeneticRoute>(pop.size)
            var bestIdx = 0
            for (i in pop.indices) {
                val route = decode(pop[i])
                d[i] = route
                f[i] = fitness(route)
                if (f[i] < f[bestIdx]) bestIdx = i
            }
            return Triple(f, d, bestIdx)
        }

        val (initFits, initDecoded, initBestIdx) = evaluate(population)
        fits = initFits
        decoded = initDecoded
        var overallBest: GeneticRoute = decoded[initBestIdx]!!
        var overallBestFit = fits[initBestIdx]

        onGeneration?.invoke(
            GeneticGeneration(
                generation = 0,
                bestFitness = overallBestFit,
                bestRoute = overallBest
            )
        )

        for (gen in 1..generations) {
            val sortedIdx = fits.indices.sortedBy { fits[it] }
            val newPop = mutableListOf<IntArray>()
            for (k in 0 until eliteCount.coerceAtMost(populationSize)) {
                newPop.add(population[sortedIdx[k]].copyOf())
            }

            while (newPop.size < populationSize) {
                val p1 = tournamentSelect(population, fits)
                val p2 = tournamentSelect(population, fits)
                val child = crossover(p1, p2)
                mutate(child)
                newPop.add(child)
            }

            population = newPop
            val (nf, nd, bestIdx) = evaluate(population)
            fits = nf
            decoded = nd

            if (fits[bestIdx] < overallBestFit) {
                overallBestFit = fits[bestIdx]
                overallBest = decoded[bestIdx]!!
            }

            onGeneration?.invoke(
                GeneticGeneration(
                    generation = gen,
                    bestFitness = overallBestFit,
                    bestRoute = overallBest
                )
            )
        }

        return overallBest
    }
}

private fun findPathAStar(
    startCell: GridCell,
    finishCell: GridCell,
    gridMap: GridMap
): List<GridCell>? {
    if (gridMap.grid[startCell.row][startCell.col] != 1) return null
    if (gridMap.grid[finishCell.row][finishCell.col] != 1) return null

    val gScore = mutableMapOf<GridCell, Int>()
    val openQueue = java.util.PriorityQueue<GridCell>(compareBy { cell ->
        (gScore[cell] ?: Int.MAX_VALUE) + heuristic(cell, finishCell)
    })
    val closedSet = mutableSetOf<GridCell>()
    val cameFrom = mutableMapOf<GridCell, GridCell>()

    openQueue.add(startCell)
    gScore[startCell] = 0

    while (openQueue.isNotEmpty()) {
        val current = openQueue.poll()!!
        if (current in closedSet) continue
        closedSet.add(current)

        if (current == finishCell) {
            return reconstructPath(finishCell, cameFrom)
        }

        val neighbors = getWalkableNeighbors(current, gridMap.grid)
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
