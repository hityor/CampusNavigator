package com.example.campusnavigator.screens.DecisionTree

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusnavigator.algorithms.FeatureSpinner
import com.example.campusnavigator.algorithms.featureOptions
import com.example.campusnavigator.algorithms.loadFeatureNames
import com.example.campusnavigator.algorithms.loadTree
import com.example.campusnavigator.algorithms.predict
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeScreen(context: Context) {
    var tree by remember { mutableStateOf<JSONObject?>(null) }
    var featureNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var spinners by remember { mutableStateOf<List<FeatureSpinner>>(emptyList()) }

    var showResult by remember { mutableStateOf(false) }
    var showTreeDialog by remember { mutableStateOf(false) }
    var recommendation by remember { mutableStateOf<String?>(null) }
    var path by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        tree = loadTree(context)
        featureNames = loadFeatureNames(context)
        spinners = featureNames.map { name ->
            FeatureSpinner(name, featureOptions[name] ?: listOf("unknown"))
        }
    }

    if (tree == null || spinners.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1565C0),
                            Color(0xFF42A5F5)
                        )
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                .shadow(4.dp, shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Выбор места для обеда",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
            )
        }

        spinners.forEachIndexed { index, spinner ->
            var expanded by remember { mutableStateOf(false) }

            Text(
                text = spinner.name.replace("_", " ").uppercase(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = spinner.selected.ifEmpty { "Выберите..." },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    spinner.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                spinners = spinners.mapIndexed { i, s ->
                                    if (i == index) s.copy(selected = option) else s
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showTreeDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Показать дерево")
            }

            Button(
                onClick = {
                    val userData = spinners.associate { it.name to it.selected }
                    val (rec, p) = predict(tree!!, userData)
                    recommendation = rec
                    path = p
                    showResult = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Определить место")
            }
        }

        if (showResult && recommendation != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Рекомендация: $recommendation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )

                    if (path.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Путь решения:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        path.forEach { step ->
                            Text(
                                text = "   • $step",
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTreeDialog && tree != null) {
        val userData = spinners.associate { it.name to it.selected }
        TreeDialog(
            tree = tree!!,
            userData = userData,
            onDismiss = { showTreeDialog = false }
        )
    }
}