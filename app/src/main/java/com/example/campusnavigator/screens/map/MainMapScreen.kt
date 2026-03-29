package com.example.campusnavigator.screens.map

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusnavigator.Algorithms.findPath
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.gridCellToLatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLngQuad
import org.maplibre.android.maps.MapLibreMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen(
    gridMap: GridMap, gridBitmap: Bitmap, latLngQuad: LatLngQuad, navController: NavController
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    var currentMode by remember { mutableStateOf(MapMode.ASTAR) }
    var showModeSheet by remember { mutableStateOf(false) }

    var startCell by remember { mutableStateOf<GridCell?>(null) }
    var finishCell by remember { mutableStateOf<GridCell?>(null) }
    var path by remember { mutableStateOf<List<GridCell>>(emptyList()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(currentMode.title) }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }, actions = {
                IconButton(onClick = { showModeSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu, contentDescription = "Выбрать режим"
                    )
                }
            })
        }) { innerPadding ->

        BottomSheetScaffold(
            modifier = Modifier.padding(innerPadding),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 180.dp,
            sheetDragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(4.dp)
                        .width(40.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge
                ) {}
            },
            sheetContent = {
                when (currentMode) {
                    MapMode.ASTAR -> {
                        AStarSheetContent(
                            startCell = startCell,
                            finishCell = finishCell,
                            onBuildRoute = {
                                val start = startCell
                                val finish = finishCell

                                if (start != null && finish != null) {
                                    val result = findPath(start, finish, gridMap)
                                    path = result ?: emptyList()
                                }
                            },
                            onClear = {
                                startCell = null
                                finishCell = null
                                path = emptyList()
                            })
                    }

                    MapMode.CLUSTERING -> {
                        ClusteringSheetContent()
                    }

                    MapMode.GENETIC -> {
                        GeneticSheetContent()
                    }

                    MapMode.ANT -> {
                        AntSheetContent()
                    }

                    MapMode.COWORKING -> {
                        CoworkingSheetContent()
                    }
                }
            }) {

            MapViewContainer(
                gridMap = gridMap,
                gridBitmap = gridBitmap,
                latLngQuad = latLngQuad,
                currentMode = currentMode,
                startCell = startCell,
                finishCell = finishCell,
                path = path,
                onCellSelected = { cell ->
                    if (currentMode == MapMode.ASTAR) {
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
                    }
                }
            )
        }

        if (showModeSheet) {
            MapModeSelectionSheet(
                onDismiss = {showModeSheet = false},
                onModeSelected = {currentMode = it},
                onClearMap = {
                    startCell = null
                    finishCell = null
                    path = emptyList()
                }
            )
        }
    }
}