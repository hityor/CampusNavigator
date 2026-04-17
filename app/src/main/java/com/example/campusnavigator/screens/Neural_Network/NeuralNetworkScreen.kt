package com.example.campusnavigator.screens.Neural_Network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.algorithms.AI.NeuralAlgorithm.recognizeDigit

@Composable
fun NeuralNetworkScreen() {

    var showResult by remember { mutableStateOf(false) }
    var digit by remember { mutableIntStateOf(0) }

    var selectedEstablishment by remember { mutableStateOf<String?>(null) }

    val establishments = listOf("Main_Cafeteria", "Vending_Machine",
        "Second_Building_Cafe", "Yarche", "Starbooks",
        "Bus_Stop_Coffee", "Siberian_Pancakes")

    Column(modifier = Modifier.fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp))
    {


        if (showResult) {
            DrawResult(
                result = digit,
                drawAgain = { showResult = false }
            )
        } else {
            DrawingCanvas(
                onRecognize = { matrix ->
                    digit = recognizeDigit(matrix)
                    showResult = true
                }
            )
            EstablishmentList(options = establishments,
                selectedOption = selectedEstablishment,
                onOptionSelected =  {selectedEstablishment = it})
        }
    }


}