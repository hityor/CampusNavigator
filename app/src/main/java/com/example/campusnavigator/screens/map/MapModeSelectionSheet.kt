package com.example.campusnavigator.screens.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapModeSelectionSheet(
    onDismiss: () -> Unit,
    onModeSelected: (MapMode) -> Unit,
    onClearMap: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        ModeSelectionContent { mode ->
            onModeSelected(mode)
            onClearMap()
            onDismiss()
        }
    }
}

@Composable
fun ModeSelectionContent(
    onModeSelected: (MapMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        MapMode.entries.forEach { entry ->
            Button(onClick = { onModeSelected(entry) }, modifier = Modifier.fillMaxWidth()) {
                Text(text = entry.title)
            }
        }
    }
}