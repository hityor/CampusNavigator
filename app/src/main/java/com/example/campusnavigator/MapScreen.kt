package com.example.campusnavigator

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngQuad
import org.maplibre.android.maps.MapView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(gridMap: GridMap, gridBitMap: Bitmap, latLngQuad: LatLngQuad) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    var startCell by remember { mutableStateOf<GridCell?>(null) }
    var finishCell by remember { mutableStateOf<GridCell?>(null) }

    var path by remember { mutableStateOf<List<GridCell>>(emptyList()) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetDragHandle = null,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(width = 40.dp, height = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge
                ) {}

                Spacer(modifier = Modifier.height(16.dp))
                Text("Настройки навигации", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(20.dp))


                Button(onClick = {
                    if (startCell != null && finishCell != null) {
                        val foundPath = findPath(startCell!!, finishCell!!, gridMap)
                        path = foundPath ?: emptyList()
                        if (path.isNotEmpty()) {
                            Log.d("astar", "path length: ${path.size}")
                        } else {
                            Log.d("astar", "path is not found")
                        }
                    }
                }, enabled = startCell != null && finishCell != null) { Text("Запустить A*") }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "© OpenStreetMap contributors, © MapLibre",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    ) { paddingValues ->

        MapViewContainer(
            gridMap = gridMap,
            gridBitmap = gridBitMap,
            latLngQuad = latLngQuad,
            onCellSelected = { cell ->
                when {
                    startCell == null -> {
                        startCell = cell
                        path = emptyList()
                    }

                    finishCell == null -> {
                        finishCell = cell
                        path = emptyList()
                    }

                    else -> {
                        startCell = cell
                        finishCell = null
                        path = emptyList()
                    }

                }

            },
            startCell = startCell,
            finishCell = finishCell,
            path = path,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun MapViewContainer(
    gridMap: GridMap,
    gridBitmap: Bitmap,
    latLngQuad: LatLngQuad,
    onCellSelected: (GridCell) -> Unit,
    startCell: GridCell?,
    finishCell: GridCell?,
    path: List<GridCell>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    AndroidView(
        modifier = modifier,
        update = { mapView ->
            mapView.getMapAsync { map ->
                map.clear()

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
                    val pathPoints = path.map { point ->
                        gridCellToLatLng(point.row, point.col, gridMap)
                    }

                    map.addPolyline(
                        PolylineOptions()
                            .addAll(pathPoints)
                            .width(3f)
                            .color(android.graphics.Color.BLUE)
                    )
                }
            }

        },
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(android.os.Bundle())
                getMapAsync { map ->
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->

                        val imageSource = org.maplibre.android.style.sources.ImageSource(
                            "grid-mask-source",
                            latLngQuad,
                            gridBitmap
                        )
                        style.addSource(imageSource)

                        val rasterLayer = org.maplibre.android.style.layers.RasterLayer(
                            "grid-mask-layer",
                            "grid-mask-source"
                        )
                        style.addLayer(rasterLayer)
                    }

                    map.addOnMapClickListener { point ->
                        val (x, y) = LatLngToEpsg3857(point.latitude, point.longitude)
                        val tappedCell = epsg3857ToGridCell(x, y, gridMap)


                        val selectedCell =
                            findNearestWalkableCell(tappedCell, gridMap, maxRadius = 3)

                        if (selectedCell != null) {
                            onCellSelected(selectedCell)
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

                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(56.475, 84.947))
                        .zoom(16.0)
                        .build()
                }
            }
        }
    )
}