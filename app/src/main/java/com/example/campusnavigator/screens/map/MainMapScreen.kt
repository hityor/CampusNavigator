package com.example.campusnavigator.screens.map

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.algorithms.findPathWithStepsStreaming
import com.example.campusnavigator.algorithms.runKMeans
import com.example.campusnavigator.createAStarOverlayBitmap
import com.example.campusnavigator.createEmptyOverlayBitmap
import com.example.campusnavigator.isInsideGrid
import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import com.example.campusnavigator.screens.map.models.MapMode
import com.example.campusnavigator.ui.theme.NavyPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLngQuad
import com.example.campusnavigator.algorithms.AntColonyOptimization
import com.example.campusnavigator.algorithms.AntResult
import com.example.campusnavigator.screens.map.models.CoworkingPlace
import com.example.campusnavigator.screens.map.sampleCoworkingPlaces
import com.example.campusnavigator.LatLngToEpsg3857
import com.example.campusnavigator.createAntOverlayBitmap
import com.example.campusnavigator.createAntResultOverlayBitmap
import com.example.campusnavigator.epsg3857ToGridCell
import com.example.campusnavigator.findNearestWalkableCell
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen(
    gridMap: GridMap,
    baseBitmap: Bitmap,
    latLngQuad: LatLngQuad,
    navController: NavController
) {
    BackHandler(enabled = true) { }

    val scaffoldState = rememberBottomSheetScaffoldState()

    var currentMode by remember { mutableStateOf(MapMode.ASTAR) }
    var showModeSheet by remember { mutableStateOf(false) }

    var startCell by remember { mutableStateOf<GridCell?>(null) }
    var finishCell by remember { mutableStateOf<GridCell?>(null) }
    var path by remember { mutableStateOf<List<GridCell>>(emptyList()) }
    var routeNotFound by remember { mutableStateOf(false) }
    var isDrawingObstacles by remember { mutableStateOf(false) }
    var extraObstacles by remember { mutableStateOf<Set<GridCell>>(emptySet()) }
    var isAnimating by remember { mutableStateOf(false) }

    var overlayBitmap by remember { mutableStateOf(gridMap.createEmptyOverlayBitmap()) }

    val scope = rememberCoroutineScope()

    var clusterCount by remember { mutableIntStateOf(3) }
    var clusteredPlaces by remember { mutableStateOf<List<ClusteredFoodPlace>>(emptyList()) }

    var antJob by remember { mutableStateOf<Job?>(null) }
    var antStartCell by remember { mutableStateOf<GridCell?>(null) }
    var antResult by remember { mutableStateOf<AntResult?>(null) }
    var antIsAnimating by remember { mutableStateOf(false) }
    var antNumAnts by remember { mutableIntStateOf(100) }
    var antPlacedStudents by remember { mutableStateOf(0) }

    val coworkingSpots = remember { sampleCoworkingPlaces }
    val coworkingLocationCells = remember(coworkingSpots) {
        coworkingSpots.map { spot ->
            val (x, y) = LatLngToEpsg3857(spot.lat, spot.lon)
            val rawCell = epsg3857ToGridCell(x, y, gridMap)
            findNearestWalkableCell(rawCell, gridMap, maxRadius = 50) ?: rawCell
        }
    }

    fun refreshAStarOverlay(
        visited: Set<GridCell> = emptySet(),
        current: GridCell? = null,
        currentPath: List<GridCell> = path
    ) {
        overlayBitmap = gridMap.createAStarOverlayBitmap(
            visited = visited,
            current = current,
            obstacles = extraObstacles,
            path = currentPath
        )
    }

    fun buildObstacleBrush(center: GridCell, radius: Int = 2): Set<GridCell> {
        val result = mutableSetOf<GridCell>()

        for (row in center.row - radius..center.row + radius) {
            for (col in center.col - radius..center.col + radius) {
                val cell = GridCell(row, col)

                if (!isInsideGrid(cell, gridMap)) continue

                result.add(cell)
            }
        }

        return result
    }

    fun clearAStarStates() {
        startCell = null
        finishCell = null
        path = emptyList()
        routeNotFound = false
        extraObstacles = emptySet()
        isAnimating = false
        overlayBitmap = gridMap.createEmptyOverlayBitmap()
    }

    fun clearClusteringState() {
        clusterCount = 3
        clusteredPlaces = emptyList()
    }

    fun clearAntStates() {
        antJob?.cancel()
        antJob = null
        antStartCell = null
        antResult = null
        antIsAnimating = false
        antPlacedStudents = 0
    }

    fun resetAllModeStates() {
        clearAStarStates()
        clearClusteringState()
        clearAntStates()
    }

    BottomSheetScaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = currentMode.title, color = Color.White
                )
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            }, actions = {
                IconButton(onClick = { showModeSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Выбрать режим",
                        tint = Color.White
                    )
                }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = NavyPrimary
            )
        )
    }, scaffoldState = scaffoldState, sheetPeekHeight = 64.dp, sheetDragHandle = {
        Surface(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(4.dp)
                .width(40.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = MaterialTheme.shapes.extraLarge
        ) {}
    }, sheetContent = {
        when (currentMode) {
            MapMode.ASTAR -> {
                AStarSheetContent(
                    gridMap = gridMap,
                    startCell = startCell,
                    finishCell = finishCell,
                    path = path,
                    routeNotFound = routeNotFound,
                    obstacleCount = extraObstacles.size,
                    isDrawingObstacles = isDrawingObstacles,
                    isAnimating = isAnimating,
                    onToggleDrawMode = {
                        isDrawingObstacles = !isDrawingObstacles
                    },
                    onClearObstacles = {
                        extraObstacles = emptySet()
                        overlayBitmap = gridMap.createEmptyOverlayBitmap()
                    },
                    onBuildRoute = {
                        val start = startCell
                        val finish = finishCell

                        if (start != null && finish != null) {
                            scope.launch {
                                isAnimating = true
                                path = emptyList()

                                val resultPath = withContext(Dispatchers.Default) {
                                    findPathWithStepsStreaming(
                                        start,
                                        finish,
                                        gridMap,
                                        extraObstacles
                                    ) { visited, current ->
                                        val bmp = gridMap.createAStarOverlayBitmap(
                                            visited = visited,
                                            current = current,
                                            obstacles = extraObstacles,
                                            path = emptyList()
                                        )
                                        withContext(Dispatchers.Main) {
                                            overlayBitmap = bmp
                                        }
                                        delay(10)

                                    }
                                }

                                overlayBitmap = withContext(Dispatchers.Default) {
                                    gridMap.createAStarOverlayBitmap(
                                        visited = emptySet(),
                                        current = null,
                                        obstacles = extraObstacles,
                                        path = resultPath ?: emptyList()
                                    )
                                }

                                path = resultPath ?: emptyList()
                                routeNotFound = resultPath == null
                                isAnimating = false
                            }

                        }
                    },
                    onClear = {
                        clearAStarStates()
                    })
            }

            MapMode.CLUSTERING -> {
                ClusteringSheetContent(
                    clusterCount = clusterCount,
                    clusteredPlaces = clusteredPlaces,
                    onClusterCountChange = { clusterCount = it },
                    onRun = {
                        clusteredPlaces = runKMeans(sampleFoodPlaces, clusterCount)
                    },
                    onClear = {
                        clearClusteringState()
                    })
            }

            MapMode.GENETIC -> {
                GeneticSheetContent()
            }

            MapMode.ANT -> {
                AntSheetContent(
                    startCell = antStartCell,
                    coworkingSpots = coworkingSpots,
                    isAnimating = antIsAnimating,
                    result = antResult,
                    numAnts = antNumAnts,
                    onNumAntsChange = { antNumAnts = it },
                    placedStudents = antPlacedStudents,
                    onStartSelected = {},
                    onRun = { numStudents ->
                        antJob?.cancel()
                        antJob = scope.launch {
                            antIsAnimating = true
                            antResult = null
                            antPlacedStudents = 0

                            val start = antStartCell ?: return@launch
                            val comforts = coworkingSpots.map { it.comfort }
                            val capacities = coworkingSpots.map { it.capacity }

                            val aco = AntColonyOptimization(
                                gridMap = gridMap,
                                homeCell = start,
                                locations = coworkingLocationCells,
                                locationComforts = comforts,
                                locationCapacities = capacities,
                                totalStudentsToPlace = numStudents,
                                numAnts = antNumAnts,
                                onStep = { homePher, foodPher, ants, placed ->
                                    antPlacedStudents = placed
                                    overlayBitmap = gridMap.createAntOverlayBitmap(
                                        homePheromone = homePher,
                                        foodPheromone = foodPher,
                                        ants = ants
                                    )
                                }
                            )

                            val result = withContext(Dispatchers.Default) { aco.run() }
                            antResult = result
                            antIsAnimating = false
                            overlayBitmap = gridMap.createAntResultOverlayBitmap(result.paths)
                        }
                    },
                    onClear = {
                        clearAntStates()
                        overlayBitmap = gridMap.createEmptyOverlayBitmap()
                    }
                )
            }

            MapMode.COWORKING -> {
                CoworkingSheetContent()
            }
        }
    }) {

        MapViewContainer(
            gridMap = gridMap,
            baseBitmap = baseBitmap,
            overlayBitmap = overlayBitmap,
            latLngQuad = latLngQuad,
            currentMode = currentMode,
            startCell = startCell,
            finishCell = finishCell,
            path = path,
            isDrawingObstacles = isDrawingObstacles,
            isAnimating = isAnimating,
            onObstacleTapped = { cell ->
                val brushCells = buildObstacleBrush(cell, radius = 3)

                val shouldRemove = brushCells.all { it in extraObstacles }

                val newObstacles = if (shouldRemove) {
                    extraObstacles - brushCells
                } else {
                    extraObstacles + brushCells
                }

                extraObstacles = newObstacles

                overlayBitmap = gridMap.createAStarOverlayBitmap(
                    visited = emptySet(),
                    current = null,
                    obstacles = newObstacles,
                    path = emptyList()
                )
            },
            clusteredPlaces = clusteredPlaces,
            onCellSelected = { cell ->
                if (currentMode == MapMode.ASTAR) {
                    when {
                        startCell == null -> {
                            startCell = cell
                            path = emptyList()
                            routeNotFound = false
                        }

                        finishCell == null -> {
                            finishCell = cell
                            path = emptyList()
                            routeNotFound = false
                        }

                        else -> {
                            startCell = cell
                            finishCell = null
                            path = emptyList()
                            routeNotFound = false
                        }
                    }

                    refreshAStarOverlay(currentPath = emptyList())
                }
            },
            modifier = Modifier
                .fillMaxSize(),
            antStartCell = antStartCell,
            antResult = antResult,
            onAntStartSelected = { cell ->
                antStartCell = cell
                antResult = null
            },
            coworkingSpots = coworkingSpots,
            coworkingCells = coworkingLocationCells
        )
    }

    if (showModeSheet) {
        MapModeSelectionSheet(
            onDismiss = { showModeSheet = false },
            onModeSelected = {
                currentMode = it
                overlayBitmap = gridMap.createEmptyOverlayBitmap()
            },
            onResetModeState = {
                resetAllModeStates()
            })
    }
}