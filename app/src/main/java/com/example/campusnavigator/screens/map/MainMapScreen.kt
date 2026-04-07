package com.example.campusnavigator.screens.map

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animate
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
import com.example.campusnavigator.algorithms.AStarStep
import com.example.campusnavigator.algorithms.findPathWithSteps
import com.example.campusnavigator.algorithms.runKMeans
import com.example.campusnavigator.createAnimatedBitmap
import com.example.campusnavigator.screens.map.models.ClusteredFoodPlace
import com.example.campusnavigator.screens.map.models.MapMode
import com.example.campusnavigator.ui.theme.NavyPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLngQuad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen(
    gridMap: GridMap, gridBitmap: Bitmap, latLngQuad: LatLngQuad, navController: NavController
) {
    BackHandler(enabled = true) { }

    val scaffoldState = rememberBottomSheetScaffoldState()

    var currentMode by remember { mutableStateOf(MapMode.ASTAR) }
    var showModeSheet by remember { mutableStateOf(false) }

    var startCell by remember { mutableStateOf<GridCell?>(null) }
    var finishCell by remember { mutableStateOf<GridCell?>(null) }
    var path by remember { mutableStateOf<List<GridCell>>(emptyList()) }
    var routeMessage by remember { mutableStateOf<String?>(null) }
    var isDrawingObstacles by remember { mutableStateOf(false) }
    var extraObstacles by remember { mutableStateOf<Set<GridCell>>(emptySet()) }
    var isAnimating by remember { mutableStateOf(false) }
    var animatedBitmap by remember { mutableStateOf(gridBitmap) }

    val scope = rememberCoroutineScope()

    var clusterCount by remember { mutableIntStateOf(3) }
    var clusteredPlaces by remember { mutableStateOf<List<ClusteredFoodPlace>>(emptyList()) }

    fun clearAStarStates() {
        startCell = null
        finishCell = null
        path = emptyList()
        routeMessage = null
        extraObstacles = emptySet()
        isAnimating = false
        animatedBitmap = gridBitmap
    }

    fun clearClusteringState() {
        clusterCount = 3
        clusteredPlaces = emptyList()
    }

    fun resetAllModeStates() {
        clearAStarStates()
        clearClusteringState()
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
                    startCell = startCell,
                    finishCell = finishCell,
                    routeMessage = routeMessage,
                    obstacleCount = extraObstacles.size,
                    isDrawingObstacles = isDrawingObstacles,
                    isAnimating = isAnimating,
                    onToggleDrawMode = {
                        isDrawingObstacles = !isDrawingObstacles
                    },
                    onClearObstacles = {
                        extraObstacles = emptySet()
                        path = emptyList()
                        routeMessage = null
                        animatedBitmap = gridBitmap
                    },
                    onBuildRoute = {
                        val start = startCell
                        val finish = finishCell

                        if (start != null && finish != null) {
                            scope.launch {
                                isAnimating = true
                                path = emptyList()

                                val (resultPath, steps) = withContext(Dispatchers.Default) {
                                    findPathWithSteps(start, finish, gridMap, extraObstacles)
                                }
                                val animatedVisited = mutableSetOf<GridCell>()
                                val skipEvery = 15

                                for (i in steps.indices) {
                                    val step = steps[i]
                                    animatedVisited.add(step.current)

                                    if (i % skipEvery == 0 || i == steps.lastIndex) {
                                        val bmp = withContext(Dispatchers.Default) {
                                            gridMap.createAnimatedBitmap(
                                                visited = animatedVisited,
                                                current = step.current,
                                                obstacles = extraObstacles,
                                                path = emptyList()
                                            )
                                        }

                                        animatedBitmap = bmp
                                        delay(50)
                                    }
                                }

                                animatedBitmap = withContext(Dispatchers.Default) {
                                    gridMap.createAnimatedBitmap(
                                        visited = emptySet(),
                                        current = null,
                                        obstacles = extraObstacles,
                                        path = resultPath ?: emptyList()
                                    )
                                }

                                path = resultPath ?: emptyList()
                                isAnimating = false
                                routeMessage =
                                    if (path.isNotEmpty()) "Маршрут: ${path.size} клеток"
                                    else "Маршрут не найден"

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
                AntSheetContent()
            }

            MapMode.COWORKING -> {
                CoworkingSheetContent()
            }
        }
    }) {

        MapViewContainer(
            gridMap = gridMap,
            gridBitmap = animatedBitmap,
            latLngQuad = latLngQuad,
            currentMode = currentMode,
            startCell = startCell,
            finishCell = finishCell,
            path = path,
            isDrawingObstacles = isDrawingObstacles,
            isAnimating = isAnimating,
            onObstacleTapped = { cell ->
                extraObstacles = if (cell in extraObstacles) {
                    extraObstacles - cell
                } else {
                    extraObstacles + cell
                }

                path = emptyList()
                routeMessage = null

                animatedBitmap = gridMap.createAnimatedBitmap(
                    visited = emptySet(),
                    current = null,
                    obstacles = extraObstacles,
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
                            routeMessage = null
                        }

                        finishCell == null -> {
                            finishCell = cell
                            path = emptyList()
                            routeMessage = null
                        }

                        else -> {
                            startCell = cell
                            finishCell = null
                            path = emptyList()
                            routeMessage = null
                        }
                    }
                }
            })
    }

    if (showModeSheet) {
        MapModeSelectionSheet(
            onDismiss = { showModeSheet = false },
            onModeSelected = { currentMode = it },
            onResetModeState = {
                resetAllModeStates()
            })
    }
}