package com.example.campusnavigator.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.gridCellToLatLng
import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import androidx.core.graphics.createBitmap
import android.graphics.Paint
import org.maplibre.android.annotations.IconFactory

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

fun getColorForCluster(index: Int): Int {
    val colors = listOf(
        android.graphics.Color.RED,
        android.graphics.Color.BLUE,
        android.graphics.Color.GREEN,
        android.graphics.Color.YELLOW,
        android.graphics.Color.MAGENTA,
        android.graphics.Color.CYAN
    )

    return colors[index % colors.size]
}

fun createMarkerBitmap(color: Int): Bitmap {
    val size = 100

    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
    }

    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

    return bitmap
}

fun renderClustering(
    map: MapLibreMap,
    context: Context,
    clusteredPlaces: List<ClusteredFoodPlace>
) {
    val iconFactory = IconFactory.getInstance(context)

    clusteredPlaces.forEach { item ->
        val color = getColorForCluster(item.clusterIndex)
        val bitmap = createMarkerBitmap(color)
        val icon = iconFactory.fromBitmap(bitmap)

        map.addMarker(
            MarkerOptions()
                .position(LatLng(item.place.lat, item.place.lon))
                .title("${item.place.name}. кластер ${item.clusterIndex + 1}")
                .icon(icon)
        )
    }
}