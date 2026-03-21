package com.example.campusnavigator

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.set

fun createBitmap(context: Context): Bitmap {
    val gridMap = makeGridFromCsv("grid_passability.csv", context)
    val height = gridMap.height
    val width = gridMap.width
    val grid = gridMap.grid

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    for (r in 0 until height) {
        for (c in 0 until width) {
            val color = when (grid[r][c]) {
                1 -> android.graphics.Color.WHITE
                else -> android.graphics.Color.BLACK
            }
            bitmap[c, r] = color
        }
    }

    return bitmap
}

@Composable
fun BitmapScreen() {
    val context = LocalContext.current
    val bitmap = remember { createBitmap(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Map"
        )
    }
}