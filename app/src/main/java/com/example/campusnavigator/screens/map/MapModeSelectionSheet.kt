package com.example.campusnavigator.screens.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.screens.map.models.MapMode
import com.example.campusnavigator.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapModeSelectionSheet(
    onDismiss: () -> Unit,
    onModeSelected: (MapMode) -> Unit,
    onResetModeState: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        Text(
            text = "Выберите режим",
            style = MaterialTheme.typography.titleMedium,
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        MapMode.entries.forEach { entry ->
            Button(
                onClick = { onModeSelected(entry) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(text = entry.title)
            }
        }
    }
}