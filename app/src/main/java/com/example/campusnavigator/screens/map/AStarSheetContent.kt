package com.example.campusnavigator.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.GreenLight

@Composable
fun AStarSheetContent(
    startCell: GridCell?,
    finishCell: GridCell?,
    routeMessage: String?,
    obstacleCount: Int,
    isAnimating: Boolean,
    isDrawingObstacles: Boolean,
    onToggleDrawMode: () -> Unit,
    onClearObstacles: () -> Unit,
    onBuildRoute: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Построение маршрута",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
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
            enabled = startCell != null && finishCell != null && !isAnimating,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenAccent,
                disabledContentColor = GreenLight
            )
        ) {
            Text(if (isAnimating) "Строю маршрут..." else "Построить маршрут")
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

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onToggleDrawMode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnimating,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isDrawingObstacles) GreenAccent.copy(alpha = 0.15f) else Color.Transparent,
                contentColor = if (isDrawingObstacles) GreenAccent else MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isDrawingObstacles) GreenAccent else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isDrawingObstacles) "Режим: ставлю препятствия" else "Режим: выбираю точки")
        }

        if (obstacleCount > 0) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {
                Text(
                    text = "Препятствий: $obstacleCount клеток",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onClearObstacles, enabled = !isAnimating) {
                    Text(
                        text = "Убрать",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

    }
}