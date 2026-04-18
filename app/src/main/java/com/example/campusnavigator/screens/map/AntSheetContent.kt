package com.example.campusnavigator.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.algorithms.AntResult
import com.example.campusnavigator.screens.map.models.CoworkingPlace
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.NavyPrimary
import com.example.campusnavigator.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun AntSheetContent(
    startCell: GridCell?,
    coworkingSpots: List<CoworkingPlace>,
    isAnimating: Boolean,
    result: AntResult?,
    numAnts: Int,
    onNumAntsChange: (Int) -> Unit,
    placedStudents: Int,
    onStartSelected: () -> Unit,
    onRun: (numStudents: Int) -> Unit,
    onClear: () -> Unit
) {
    var studentCount by remember { mutableIntStateOf(20) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Распределение студентов по локациям",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = NavyPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Количество студентов: $studentCount", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = studentCount.toFloat(),
            onValueChange = { studentCount = it.roundToInt() },
            valueRange = 1f..50f,
            steps = 49,
            enabled = !isAnimating,
            colors = SliderDefaults.colors(thumbColor = GreenAccent, activeTrackColor = GreenAccent)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Муравьев в колонии: $numAnts", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = numAnts.toFloat(),
            onValueChange = { onNumAntsChange(it.roundToInt()) },
            valueRange = 10f..1000f,
            steps = 49,
            enabled = !isAnimating,
            colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnimating || result != null) {
            val progress = if (isAnimating) {
                placedStudents.toFloat() / studentCount
            } else {
                (result?.totalStudentsPlaced ?: 0).toFloat() / studentCount
            }.coerceIn(0f, 1f)

            Text(
                "Размещено: ${if (isAnimating) placedStudents else result?.totalStudentsPlaced ?: 0} / $studentCount",
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp),
                color = GreenAccent
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onRun(studentCount) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = startCell != null && !isAnimating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isAnimating) "Движение..." else "Распределить")
            }

            IconButton(
                onClick = onClear,
                enabled = startCell != null || result != null,
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

        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Результаты распределения",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Суммарное расстояние: ${(result.paths.sumOf { it.size - 1 } * 7)} метров",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(coworkingSpots.indices.toList()) { idx ->
                    val spot = coworkingSpots[idx]
                    val load = result.locationLoads.getOrElse(idx) { 0 }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${spot.name} (комфорт: ${"%.1f".format(spot.comfort)})",
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$load / ${spot.capacity}",
                            fontWeight = if (load >= spot.capacity) FontWeight.Bold else FontWeight.Normal,
                            color = if (load >= spot.capacity) Color.Red else NavyPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = (load.toFloat() / spot.capacity).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = when {
                            load >= spot.capacity -> Color.Red
                            load > spot.capacity * 0.8 -> Color.Yellow
                            else -> GreenAccent
                        },
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}