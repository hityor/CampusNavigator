package com.example.campusnavigator.screens.map

import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import org.maplibre.android.geometry.LatLngQuad
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
    clusteredPlaces: List<ClusteredFoodPlace>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    val currentModeState by rememberUpdatedState(currentMode)
    val onCellSelectedState by rememberUpdatedState(onCellSelected)

    AndroidView(modifier = modifier, update = { mapView ->
        mapView.getMapAsync { map ->
            map.clear()

            when (currentMode) {
                MapMode.ASTAR -> renderAStar(map, gridMap, startCell, finishCell, path)
                MapMode.CLUSTERING -> {renderClustering(map, clusteredPlaces)}
                MapMode.GENETIC -> {}
                MapMode.ANT -> {}
                MapMode.COWORKING -> {}
            }
        }

    }, factory = { ctx ->
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
                }

                map.addOnMapClickListener { point ->
                    val (x, y) = LatLngToEpsg3857(point.latitude, point.longitude)
                    val tappedCell = epsg3857ToGridCell(x, y, gridMap)


                    val selectedCell = findNearestWalkableCell(tappedCell, gridMap, maxRadius = 3)

                    if (currentModeState == MapMode.ASTAR) {
                        if (selectedCell != null) {
                            onCellSelectedState(selectedCell)
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

                map.setMinZoomPreference(1.0)
                map.setMaxZoomPreference(20.0)

                map.cameraPosition =
                    CameraPosition.Builder().target(LatLng(56.469449, 84.947971)).zoom(16.0).build()
            }
        }
    })
}