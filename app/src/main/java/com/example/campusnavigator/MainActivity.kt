package com.example.campusnavigator

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
import org.maplibre.geojson.FeatureCollection

class MainActivity : ComponentActivity() {
    private val gridMapState = mutableStateOf<GridMap?>(null)
    private val gridFeaturesState = mutableStateOf<FeatureCollection?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            val myGrid = makeGridFromCsv("grid_passability.csv", this@MainActivity)
            val gridFeatures = myGrid.generateFeatures()

            withContext(Dispatchers.Main) {
                gridMapState.value = myGrid
                gridFeaturesState.value = gridFeatures
            }
        }


        setContent {
            CampusNavigatorTheme {
                val grid = gridMapState.value
                val features = gridFeaturesState.value

                if (grid == null || features == null) {
                    SplashScreen()
                }
                else {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("map") {MapScreen(gridMap = grid, gridFeatures = features)}
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