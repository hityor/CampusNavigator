package com.example.campusnavigator.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.algorithms.AntResult
import com.example.campusnavigator.epsg3857ToGridCell
import com.example.campusnavigator.gridCellToLatLng
import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.NavyPrimary
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import com.example.campusnavigator.screens.map.models.CoworkingPlace

fun createLabeledMarkerBitmap(label: String, color: Int): Bitmap {
    val size = 80
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, Paint().apply {
        this.color = Color.WHITE
        isAntiAlias = true
    })

    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 5f, Paint().apply {
        this.color = color
        isAntiAlias = true
    })

    canvas.drawText(label, size / 2f, size / 2f + 6f, Paint().apply {
        this.color = Color.WHITE
        textSize = 34f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    })

    return bitmap
}

fun renderAStar(
    map: MapLibreMap,
    context: Context,
    gridMap: GridMap,
    startCell: GridCell?,
    finishCell: GridCell?,
    path: List<GridCell>
) {
    if (startCell != null) {
        val pos = gridCellToLatLng(startCell.row, startCell.col, gridMap)
        val icon = IconFactory.getInstance(context)
            .fromBitmap(createLabeledMarkerBitmap("A", GreenAccent.toArgb()))
        map.addMarker(MarkerOptions().position(pos).title("Старт").icon(icon))
    }

    if (finishCell != null) {
        val pos = gridCellToLatLng(finishCell.row, finishCell.col, gridMap)
        val icon = IconFactory.getInstance(context)
            .fromBitmap(createLabeledMarkerBitmap("B", NavyPrimary.toArgb()))
        map.addMarker(MarkerOptions().position(pos).title("Финиш").icon(icon))
    }

    if (path.isNotEmpty()) {
        val pathPoints = path.map { cell -> gridCellToLatLng(cell.row, cell.col, gridMap) }
        map.addPolyline(
            PolylineOptions()
                .addAll(pathPoints)
                .color(GreenAccent.toArgb())
                .width(5f)
        )
    }
}

fun getColorForCluster(index: Int): Int {
    val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN)
    return colors[index % colors.size]
}

fun createMarkerBitmap(color: Int): Bitmap {
    val size = 80
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    canvas.drawCircle(size / 2f, size / 2f + 3f, size / 2.6f, Paint().apply {
        this.color = color
        isAntiAlias = true
    })
    canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, Paint().apply {
        this.color = color
        isAntiAlias = true
    })
    canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, Paint().apply {
        this.color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
    })

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
        val icon = iconFactory.fromBitmap(createMarkerBitmap(color))
        map.addMarker(
            MarkerOptions()
                .position(LatLng(item.place.lat, item.place.lon))
                .title("${item.place.name}. кластер ${item.clusterIndex + 1}")
                .icon(icon)
        )
    }
}

fun renderAnt(
    map: MapLibreMap,
    context: Context,
    gridMap: GridMap,
    startCell: GridCell?,
    result: AntResult?,
    coworkingSpots: List<CoworkingPlace>,
    coworkingCells: List<GridCell>
) {
    if (startCell != null) {
        val pos = gridCellToLatLng(startCell.row, startCell.col, gridMap)
        val icon = IconFactory.getInstance(context)
            .fromBitmap(createLabeledMarkerBitmap("S", GreenAccent.toArgb()))
        map.addMarker(MarkerOptions().position(pos).title("Старт").icon(icon))
    }

    coworkingSpots.forEachIndexed { index, spot ->
        val pos = gridCellToLatLng(coworkingCells[index].row, coworkingCells[index].col, gridMap)
        val load = result?.locationLoads?.getOrNull(index) ?: 0
        val capacity = spot.capacity
        val color = when {
            load >= capacity -> Color.RED
            load > capacity * 0.8 -> Color.YELLOW
            else -> Color.GREEN
        }
        val bitmap = createLabeledMarkerBitmap("$load/$capacity", color)
        val icon = IconFactory.getInstance(context).fromBitmap(bitmap)
        map.addMarker(
            MarkerOptions()
                .position(pos)
                .title("${spot.name} (комфорт: ${"%.1f".format(spot.comfort)})")
                .icon(icon)
        )
    }
}