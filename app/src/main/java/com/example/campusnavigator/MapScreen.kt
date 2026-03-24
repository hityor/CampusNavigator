package com.example.campusnavigator

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import org.maplibre.android.geometry.LatLngQuad


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(gridMap: GridMap, gridBitMap: Bitmap, latLngQuad: LatLngQuad) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetDragHandle =  null,
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


                Button(onClick = {}) { Text("Запустить A*") }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun MapViewContainer(gridMap: GridMap, gridBitmap: Bitmap, latLngQuad: LatLngQuad, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    AndroidView(
        modifier = modifier,
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

                        val rasterLayer = org.maplibre.android.style.layers.RasterLayer("grid-mask-layer", "grid-mask-source")
                        style.addLayer(rasterLayer)
                    }

                    map.addOnMapClickListener { point ->
                        val (x, y) = LatLngToEpsg3857(point.latitude, point.longitude)
                        val tappedCell = epsg3857ToGridCell(x, y, gridMap)


                        val selectedCell = findNearestWalkableCell(tappedCell, gridMap, maxRadius = 3)

                        if (selectedCell != null) {
                            val finalPos = gridCellToLatLng(selectedCell.row, selectedCell.col, gridMap)
                            map.addMarker(org.maplibre.android.annotations.MarkerOptions().position(finalPos))
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