package com.example.campusnavigator.screens.DecisionTree

import android.content.Context
import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.campusnavigator.ui.theme.NavyPrimary
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeScreen(context: Context, onBackClick: () -> Unit) {
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Выбор места для обеда",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }, navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NavyPrimary
                )
            )
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Параметры выбора",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Укажите предпочтения и дерево решений предложит подходящее место",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(spinners) { index, spinner ->
                var expanded by remember { mutableStateOf(false) }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = spinner.name.replace("_", " ").uppercase(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = spinner.selected.ifEmpty { "Выберите..." },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded, onDismissRequest = { expanded = false }) {
                            spinner.options.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    spinners = spinners.mapIndexed { i, s ->
                                        if (i == index) s.copy(selected = option) else s
                                    }
                                    expanded = false
                                })
                            }
                        }
                    }
                }


            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showTreeDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
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
                        }, modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Определить место")
                    }
                }
            }


            if (showResult && recommendation != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Рекомендация",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = recommendation ?: "",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)

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
        }
    }

    if (showTreeDialog && tree != null) {
        val userData = spinners.associate { it.name to it.selected }
        TreeDialog(
            tree = tree!!,
            userData = userData,
            showPath = showResult,
            onDismiss = { showTreeDialog = false })
    }
}