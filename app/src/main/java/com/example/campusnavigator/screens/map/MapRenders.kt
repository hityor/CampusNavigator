package com.example.campusnavigator.screens.map

import android.graphics.Color
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.gridCellToLatLng
import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

fun renderAStar(
    map: MapLibreMap,
    gridMap: GridMap,
    startCell: GridCell?,
    finishCell: GridCell?,
    path: List<GridCell>
) {
    if (startCell != null) {
        val pos = gridCellToLatLng(startCell.row, startCell.col, gridMap)
        map.addMarker(
            MarkerOptions().position(pos).title("Start")
        )
    }

    if (finishCell != null) {
        val pos = gridCellToLatLng(finishCell.row, finishCell.col, gridMap)
        map.addMarker(
            MarkerOptions().position(pos).title("Finish")
        )
    }

    if (path.isNotEmpty()) {
        val pathPoints = path.map { cell ->
            gridCellToLatLng(cell.row, cell.col, gridMap)
        }

        map.addPolyline(
            PolylineOptions().addAll(pathPoints).width(3f).color(Color.BLUE)
        )
    }
}

fun renderClustering(
    map: MapLibreMap,
    clusteredPlaces: List<ClusteredFoodPlace>
) {
    clusteredPlaces.forEach { item ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(item.place.lat, item.place.lon))
                .title("${item.place.name}. кластер ${item.clusterIndex + 1}")
        )
    }
}