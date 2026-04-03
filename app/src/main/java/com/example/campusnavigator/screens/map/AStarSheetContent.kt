package com.example.campusnavigator.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.GreenLight
import com.example.campusnavigator.ui.theme.NavyPrimary
import com.example.campusnavigator.ui.theme.TextSecondary

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
        Text(
            text = "Построение маршрута",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )

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
            enabled = startCell != null && finishCell != null,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenAccent,
                disabledContentColor = GreenLight
            )
        ) {
            Text("Построить маршрут")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth(),
            enabled = startCell != null || finishCell != null,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("Очистить")
        }

        if (routeMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(routeMessage)
        }

    }
}