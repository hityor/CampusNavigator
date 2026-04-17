package com.example.campusnavigator

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusnavigator.algorithms.AI.NeuralAlgorithm
import com.example.campusnavigator.screens.DecisionTree.TreeScreen
import com.example.campusnavigator.screens.HomeScreen
import com.example.campusnavigator.screens.SplashScreen
import com.example.campusnavigator.screens.Neural_Network.NeuralNetworkScreen
import com.example.campusnavigator.screens.map.MainMapScreen
import com.example.campusnavigator.ui.theme.CampusNavigatorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLngQuad

class MainActivity : ComponentActivity() {
    private val gridMapState = mutableStateOf<GridMap?>(null)
    private val baseBitmapState = mutableStateOf<Bitmap?>(null)
    private val gridQuadState = mutableStateOf<LatLngQuad?>(null)
    private val neuralAlgorithmState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        MapLibre.getInstance(this)
        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            NeuralAlgorithm.init(this@MainActivity)

            withContext(Dispatchers.Main) {
                neuralAlgorithmState.value = true
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val myGrid = makeGridFromCsv("grid_passability.csv", this@MainActivity)
            val bitmap = myGrid.createBaseBitmap()
            val quad = myGrid.getLatLngQuad()

            withContext(Dispatchers.Main) {
                gridMapState.value = myGrid
                baseBitmapState.value = bitmap
                gridQuadState.value = quad
            }
        }


        setContent {
            CampusNavigatorTheme {
                val grid = gridMapState.value
                val baseBitmap = baseBitmapState.value
                val quad = gridQuadState.value
                val neural = neuralAlgorithmState.value

                if (grid == null || baseBitmap == null || quad == null || !neural) {
                    SplashScreen()
                } else {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {

                        composable("home") { HomeScreen(navController) }

                        composable("mainMap") {
                            MainMapScreen(
                                gridMap = grid,
                                baseBitmap = baseBitmap,
                                latLngQuad = quad,
                                navController = navController
                            )
                        }

                        composable("decisionTree") {
                            TreeScreen(this@MainActivity)
                        }

                        composable("neural") {
                            NeuralNetworkScreen()
                        }
                    }
                }

            }
        }
    }
}