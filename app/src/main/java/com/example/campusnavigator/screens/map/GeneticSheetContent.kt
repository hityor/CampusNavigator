package com.example.campusnavigator.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.algorithms.GeneticRoute
import com.example.campusnavigator.screens.map.models.FoodItem
import com.example.campusnavigator.screens.map.models.FoodPlace
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.NavyPrimary
import com.example.campusnavigator.ui.theme.TextSecondary

@Composable
fun GeneticSheetContent(
    startCell: GridCell?,
    selectedItems: Set<String>,
    onToggleItem: (String) -> Unit,
    isRunning: Boolean,
    progress: Float,
    generationText: String,
    route: GeneticRoute?,
    places: List<FoodPlace>,
    onRun: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Маршрут для покупки еды",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = NavyPrimary
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Стартовая точка",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (startCell != null)
                            "Выбрана (${startCell.row}, ${startCell.col})"
                        else
                            "Нажмите на карту, чтобы выбрать",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (startCell != null) NavyPrimary else Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Что хотите купить?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))

        ItemsFlow(
            items = FoodItem.ALL,
            selected = selectedItems,
            onToggle = onToggleItem,
            enabled = !isRunning
        )

        Spacer(Modifier.height(16.dp))

        if (isRunning || route != null) {
            Text(
                generationText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp),
                color = GreenAccent
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onRun,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = startCell != null && selectedItems.isNotEmpty() && !isRunning,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Идёт поиск...")
                } else {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Построить маршрут")
                }
            }

            IconButton(
                onClick = onClear,
                enabled = !isRunning && (route != null || selectedItems.isNotEmpty()),
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }

        if (route != null) {
            Spacer(Modifier.height(16.dp))
            GeneticResultBlock(route = route, places = places)
        }
    }
}

@Composable
private fun ItemsFlow(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    enabled: Boolean
) {
    val perRow = 2
    val rows = items.chunked(perRow)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = item in selected,
                        onClick = { if (enabled) onToggle(item) },
                        enabled = enabled,
                        label = { Text(item, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                if (row.size < perRow) {
                    repeat(perRow - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneticResultBlock(
    route: GeneticRoute,
    places: List<FoodPlace>
) {
    Text(
        text = "Найденный маршрут",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = NavyPrimary
    )
    Spacer(Modifier.height(4.dp))

    Text(
        text = "Длина: ${metersFromCells(route.totalDistanceCells)} м  ·  " +
                "Время: ${formatMinutes(route.totalMinutes)}",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )

    if (route.missingItems.isNotEmpty()) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Не удалось купить: ${route.missingItems.joinToString()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB00020)
        )
    }

    Spacer(Modifier.height(8.dp))

    if (route.visitedPlaceIndices.isEmpty()) {
        Text(
            "Маршрут пуст — возможно, нужные заведения закрыты.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.heightIn(max = 260.dp)
    ) {
        items(route.visitedPlaceIndices.withIndex().toList()) { (order, placeIdx) ->
            val place = places[placeIdx]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .size(28.dp)
                        .background(NavyPrimary, RoundedCornerShape(14.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${order + 1}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        place.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Купим: " +
                                place.menu.intersect(route.purchasedItems)
                                    .joinToString().ifEmpty { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private fun metersFromCells(cells: Int): Int = (cells * 7)

private fun formatMinutes(total: Int): String {
    if (total < 60) return "$total мин"
    val h = total / 60
    val m = total % 60
    return if (m == 0) "$h ч" else "$h ч $m мин"
}
