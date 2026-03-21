package com.example.campusnavigator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusnavigator.ui.theme.CampusNavigatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusNavigatorTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("astar") { AStarScreen(navController) }
                    composable("clustering") { ClusteringScreen(navController) }
                    composable("genetic") { GeneticScreen(navController) }
                    composable("ant") { AntScreen(navController) }
                    composable("decisionTree") { DecisionTreeScreen(navController) }
                    composable("neural") { NeuralScreen(navController) }
                    composable("map") { BitmapScreen() }
                }
            }
        }
    }
}