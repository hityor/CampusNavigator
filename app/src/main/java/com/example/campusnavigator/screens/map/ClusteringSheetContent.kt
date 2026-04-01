package com.example.campusnavigator.screens.map

import androidx.compose.foundation.layout.Arrangement
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

@Composable
fun ClusteringSheetContent(
    clusterCount: Int,
    onClusterCountChange: (Int) -> Unit,
    onRun: () -> Unit,
    onClear: () -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Кластеризация заведений")
        Text("Количество кластеров: $clusterCount")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (clusterCount > 1) onClusterCountChange(clusterCount - 1) }) {
                Text("-")
            }

            Button(onClick = { if (clusterCount < 8) onClusterCountChange(clusterCount + 1) }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRun,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Запустить")
        }

        Button(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сбросить")
        }
    }
}