package com.example.campusnavigator.screens.DecisionTree

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.campusnavigator.algorithms.convertJsonToGraph
import com.example.campusnavigator.algorithms.predict
import org.json.JSONObject
import kotlin.collections.emptyList

@Composable
fun TreeDialog(
    tree: JSONObject,
    userData: Map<String, String>,
    showPath: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .background(Color.White, shape = MaterialTheme.shapes.large),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дерево решений",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 24.sp)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .background(Color(0xFFF5F5F5), shape = MaterialTheme.shapes.medium)
                        .padding(24.dp)
                ) {
                    val path = if (showPath) {predict(tree,userData).second } else {emptyList()}
                    DrawTreeRoot(
                        node = convertJsonToGraph(tree, path),
                        path = path
                    )
                }
            }
        }
    }
}