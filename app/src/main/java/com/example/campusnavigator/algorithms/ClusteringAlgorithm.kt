package com.example.campusnavigator.algorithms

import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import com.example.campusnavigator.screens.map.models.FoodPlace
import kotlin.math.pow
import kotlin.math.sqrt


data class ClusterCenter(
    val lat: Double,
    val lon: Double
)

fun runKMeans(
    places: List<FoodPlace>,
    k: Int,
    maxIterations: Int = 15
): List<ClusteredFoodPlace> {
    if (places.isEmpty()) return emptyList()

    val actualK = k.coerceIn(1, places.size)

    var centers = places.take(actualK).map {
        ClusterCenter(it.lat, it.lon)
    }

    var assignments = List(places.size) { 0 }

    repeat(maxIterations) {
        assignments = places.map { place ->
            centers.indices.minByOrNull { idx ->
                distance(place.lat, place.lon, centers[idx].lat, centers[idx].lon)
            } ?: 0
        }

        centers = centers.indices.map { clusterIndex ->
            val clusterPoints = places.filterIndexed { idx, _ ->
                assignments[idx] == clusterIndex
            }

            if (clusterPoints.isEmpty()) {
                centers[clusterIndex]
            } else {
                ClusterCenter(
                    lat = clusterPoints.map { it.lat }.average(),
                    lon = clusterPoints.map { it.lon }.average()
                )
            }
        }
    }

    return places.mapIndexed { idx, place ->
        ClusteredFoodPlace(
            place = place,
            clusterIndex = assignments[idx]
        )
    }
}

private fun distance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Double {
    return sqrt((lat1 - lat2).pow(2) + (lon1 - lon2).pow(2))
}