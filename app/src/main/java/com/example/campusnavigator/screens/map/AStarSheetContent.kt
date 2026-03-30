package com.example.campusnavigator.screens.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.GridCell

@Composable
fun AStarSheetContent(
    startCell: GridCell?,
    finishCell: GridCell?,
    routeMessage: String?,
    onBuildRoute: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Построение маршрута")

        Spacer(Modifier.height(6.dp))

        Text("Сначала выберите старт, потом финиш")

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("Стартовая точка: ")
            Text(text = if (startCell != null) "Выбрана" else "Не выбрана")
        }

        Row {
            Text("Конечная точка: ")
            Text(text = if (finishCell != null) "Выбрана" else "Не выбрана")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBuildRoute,
            modifier = Modifier.fillMaxWidth(),
            enabled = startCell != null && finishCell != null
        ) {
            Text("Построить маршрут")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth(),
            enabled = startCell != null || finishCell != null
        ) {
            Text("Очистить")
        }

        if (routeMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(routeMessage)
        }

    }
}