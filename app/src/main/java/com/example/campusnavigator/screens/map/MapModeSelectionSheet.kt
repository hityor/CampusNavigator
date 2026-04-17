package com.example.campusnavigator.screens.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ScatterPlot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.screens.map.models.MapMode
import com.example.campusnavigator.ui.theme.GreenAccent
import com.example.campusnavigator.ui.theme.NavyPrimary
import com.example.campusnavigator.ui.theme.TextSecondary

private data class ModeEntry(
    val mode: MapMode,
    val icon: ImageVector,
    val description: String
)

private val modeEntries = listOf(
    ModeEntry(MapMode.ASTAR, Icons.Default.Navigation, "Построить маршрут между двумя точками"),
    ModeEntry(
        MapMode.CLUSTERING,
        Icons.Default.ScatterPlot,
        "Сгруппировать точки питания по кластерам"
    ),
    ModeEntry(MapMode.GENETIC, Icons.Default.Restaurant, "Оптимальный маршрут по столовым"),
    ModeEntry(MapMode.ANT, Icons.Default.Explore, "Муравьиный алгоритм - тур по кампусу"),
    ModeEntry(MapMode.COWORKING, Icons.Default.Laptop, "Найти подходящее место для работы")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapModeSelectionSheet(
    onDismiss: () -> Unit,
    onModeSelected: (MapMode) -> Unit,
    onResetModeState: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ModeSelectionContent { mode ->
            onResetModeState()
            onModeSelected(mode)
            onDismiss()
        }
    }
}

@Composable
fun ModeSelectionContent(
    onModeSelected: (MapMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            Text(
                text = "Режим карты",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item { Spacer(Modifier.height(4.dp)) }

        items(modeEntries) { entry ->
            ModeCard(entry = entry, onClick = { onModeSelected(entry.mode) })
        }
    }
}

@Composable
private fun ModeCard(entry: ModeEntry, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.mode.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = NavyPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}