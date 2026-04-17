package com.example.campusnavigator.screens.Neural_Network

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun DrawResult(result: Int, drawAgain: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                text = result.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = drawAgain) {
            Text("Нарисовать другую цифру")
        }
    }
}