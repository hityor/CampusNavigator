package com.example.campusnavigator

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusnavigator.Algorithms.AntScreen
import com.example.campusnavigator.Algorithms.ClusteringScreen
import com.example.campusnavigator.Algorithms.DecisionTreeScreen
import com.example.campusnavigator.Algorithms.GeneticScreen
import com.example.campusnavigator.Algorithms.NeuralScreen
import com.example.campusnavigator.ui.theme.CampusNavigatorTheme
import org.maplibre.android.MapLibre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import org.maplibre.android.geometry.LatLngQuad

class MainActivity : ComponentActivity() {
    private val gridMapState = mutableStateOf<GridMap?>(null)
    private val gridBitmapState = mutableStateOf<Bitmap?>(null)
    private val gridQuadState = mutableStateOf<LatLngQuad?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            val myGrid = makeGridFromCsv("grid_passability.csv", this@MainActivity)
            val bitmap = myGrid.createGridBitMap()
            val quad = myGrid.getLatLngQuad()

            withContext(Dispatchers.Main) {
                gridMapState.value = myGrid
                gridBitmapState.value = bitmap
                gridQuadState.value = quad
            }
        }


        setContent {
            CampusNavigatorTheme {
                val grid = gridMapState.value
                val bitmap = gridBitmapState.value
                val quad = gridQuadState.value

                if (grid == null || bitmap == null || quad == null) {
                    SplashScreen()
                }
                else {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("map") {MapScreen(gridMap = grid, gridBitMap = bitmap, latLngQuad = quad)}
                        composable("astar") { AStarScreen(navController) }
                        composable("clustering") { ClusteringScreen(navController) }
                        composable("genetic") { GeneticScreen(navController) }
                        composable("ant") { AntScreen(navController) }
                        composable("decisionTree") { DecisionTreeScreen(navController) }
                        composable("neural") { NeuralScreen(navController) }
                    }
                }

            }
        }
    }
}