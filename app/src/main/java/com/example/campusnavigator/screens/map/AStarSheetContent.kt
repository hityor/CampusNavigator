package com.example.campusnavigator.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.GridCell
import com.example.campusnavigator.GridMap
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.GreenLight
import com.example.campusnavigator.ui.theme.NavyPrimary
import com.example.campusnavigator.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun AStarSheetContent(
    gridMap: GridMap,
    startCell: GridCell?,
    finishCell: GridCell?,
    path: List<GridCell>,
    routeNotFound: Boolean,
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RouteStep(
                title = "A",
                label = "Старт",
                isDone = startCell != null,
                isActive = startCell == null,
                modifier = Modifier.weight(1f)
            )
            RouteStep(
                title = "B",
                label = "Финиш",
                isDone = finishCell != null,
                isActive = startCell != null && finishCell == null,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onBuildRoute,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = startCell != null && finishCell != null && !isAnimating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(contentColor = GreenAccent)
            ) {
                Icon(Icons.Default.Navigation, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isAnimating) "Строю..." else "Построить")
            }

            IconButton(
                onClick = onClear,
                enabled = startCell != null || finishCell != null,
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

        if (path.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(contentColor = GreenLight.copy(0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenAccent)
                    Column {
                        Text(
                            text = "Маршрут построен",
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary
                        )

                        val cellSizeMeters = gridMap.cellSize
                        val distanceMeters = (path.size * cellSizeMeters).roundToInt()
                        val walkMinutes = (distanceMeters / 83.0).roundToInt()

                        Text(
                            text = "$distanceMeters м: ~$walkMinutes мин пешком",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else if (routeNotFound) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Red.copy(0.7f)
                    )
                    Text(text = "Маршрут не найден", color = NavyPrimary)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !isDrawingObstacles,
                onClick = { if (isDrawingObstacles) onToggleDrawMode() },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = { Icon(Icons.Default.LocationOn, null) }
            ) {
                Text("Точки")
            }
            SegmentedButton(
                selected = isDrawingObstacles,
                onClick = { if (!isDrawingObstacles) onToggleDrawMode() },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = { Icon(Icons.Default.Block, null) }
            ) {
                Text("Препятствия")
            }
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

@Composable
fun RouteStep(
    title: String,
    label: String,
    isDone: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when {
                        isDone -> GreenAccent
                        isActive -> NavyPrimary
                        else -> Color.LightGray.copy(alpha = 0.4f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = title,
                    color = if (isActive) Color.White else Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = label,
            color = if (isDone || isActive) NavyPrimary else Color.Gray,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}