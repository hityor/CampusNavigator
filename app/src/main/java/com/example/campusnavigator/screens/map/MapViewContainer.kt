package com.example.campusnavigator.screens.map

import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.LatLngToEpsg3857
import com.example.campusnavigator.epsg3857ToGridCell
import com.example.campusnavigator.findNearestWalkableCell
import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import com.example.campusnavigator.screens.map.models.MapMode
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.geometry.LatLngQuad
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.ImageSource

@Composable
fun MapViewContainer(
    gridMap: GridMap,
    gridBitmap: Bitmap,
    latLngQuad: LatLngQuad,
    currentMode: MapMode,
    onCellSelected: (GridCell) -> Unit,
    startCell: GridCell?,
    finishCell: GridCell?,
    path: List<GridCell>,
    isDrawingObstacles: Boolean,
    isAnimating: Boolean,
    onObstacleTapped: (GridCell) -> Unit,
    clusteredPlaces: List<ClusteredFoodPlace>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    val currentModeState by rememberUpdatedState(currentMode)
    val onCellSelectedState by rememberUpdatedState(onCellSelected)
    val isDrawingObstaclesState by rememberUpdatedState(isDrawingObstacles)
    val onObstacleTappedState by rememberUpdatedState(onObstacleTapped)
    val isAnimatingState by rememberUpdatedState(isAnimating)

    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(gridBitmap) {
        val map = mapRef ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect
        val source = style.getSourceAs<ImageSource>("grid-mask-source")
        source?.setImage(gridBitmap)
    }

    LaunchedEffect(mapRef, currentMode, startCell, finishCell, path, clusteredPlaces) {
        val map = mapRef ?: return@LaunchedEffect
        map.clear()
        when (currentMode) {
            MapMode.ASTAR -> renderAStar(map, gridMap, startCell, finishCell, path)
            MapMode.CLUSTERING -> renderClustering(map, context, clusteredPlaces)
            MapMode.GENETIC -> {}
            MapMode.ANT -> {}
            MapMode.COWORKING -> {}
        }
    }


    AndroidView(
        modifier = modifier,
        factory = { ctx ->
        MapView(ctx).apply {
            onCreate(Bundle())
            getMapAsync { map ->

                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->

                    val imageSource = ImageSource(
                        "grid-mask-source", latLngQuad, gridBitmap
                    )
                    style.addSource(imageSource)

                    val rasterLayer = RasterLayer(
                        "grid-mask-layer", "grid-mask-source"
                    )
                    style.addLayer(rasterLayer)

                    mapRef = map
                }

                map.addOnMapClickListener { point ->
                    if (isAnimating) return@addOnMapClickListener true

                    val (x, y) = LatLngToEpsg3857(point.latitude, point.longitude)
                    val tappedCell = epsg3857ToGridCell(x, y, gridMap)
                    val selectedCell = findNearestWalkableCell(tappedCell, gridMap, maxRadius = 3)


                    if (currentModeState == MapMode.ASTAR) {
                        if (selectedCell != null) {
                            if (isDrawingObstaclesState) {
                                onObstacleTappedState(selectedCell)
                            } else {
                                onCellSelectedState(selectedCell)
                            }
                        }
                    }
                    true
                }

                map.uiSettings.isLogoEnabled = false
                map.uiSettings.isAttributionEnabled = false
                map.uiSettings.apply {
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                    isRotateGesturesEnabled = true
                    isTiltGesturesEnabled = true
                }

                val bitmapBounds = LatLngBounds.Builder()
                    .include(latLngQuad.topLeft)
                    .include(latLngQuad.topRight)
                    .include(latLngQuad.bottomLeft)
                    .include(latLngQuad.bottomRight)
                    .build()

                val latPad = (bitmapBounds.latitudeNorth - bitmapBounds.latitudeSouth) * 0.25
                val lonPad = (bitmapBounds.longitudeEast - bitmapBounds.longitudeWest) * 0.18

                val tightBounds = LatLngBounds.from(
                    bitmapBounds.latitudeNorth - latPad,
                    bitmapBounds.longitudeEast - lonPad,
                    bitmapBounds.latitudeSouth + latPad,
                    bitmapBounds.longitudeWest + lonPad
                )

                map.setLatLngBoundsForCameraTarget(tightBounds)

                map.setMinZoomPreference(14.5)
                map.setMaxZoomPreference(20.0)

                map.cameraPosition =
                    CameraPosition.Builder().target(LatLng(56.469449, 84.947971)).zoom(14.5).build()
            }
        }
    })
}