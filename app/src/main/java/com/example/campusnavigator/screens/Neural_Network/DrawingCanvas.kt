package com.example.campusnavigator.screens.Neural_Network

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.algorithms.AI.Matrix
import kotlin.math.abs

private fun createEmptyPixels(): IntArray = IntArray(2500) {0}

private fun drawLine(pixels: IntArray, x0: Int, y0: Int, x1: Int, y1: Int): IntArray {
    val result = pixels.copyOf()
    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    val sx = if (x0 < x1) 1 else -1
    val sy = if (y0 < y1) 1 else -1
    var err = dx - dy
    var x = x0
    var y = y0
    val brushRadius = 1

    while (true) {
        for (by in -brushRadius..brushRadius){
            for (bx in -brushRadius..brushRadius) {
                val px = x + bx
                val py = y + by
                if (px in 0..49 && py in 0..49) result[py * 50 + px] = 1
            }
        }

        if (x == x1 && y == y1) break
        val e2 = 2 * err
        if (e2 > -dy) {err -= dy; x += sx}
        if (e2 < dx) {err += dx; y += sy}
    }
    return result
}

private fun pixelsToMatrix(pixels: IntArray): Matrix {
    val matrix = Matrix(2500,1)
    for (i in 0 until 2500) {
        matrix[i,0] = pixels[i].toDouble()
    }
    return matrix
}

private fun DrawScope.renderDrawing(pixels: IntArray, cellSize: Float) {
    for (y in 0 until 50) {
        for (x in 0 until 50) {
            if (pixels[y * 50 + x] == 1) {
                drawRect(
                    color = Color.White,
                    topLeft = Offset(x * cellSize,y * cellSize),
                    size = Size(cellSize,cellSize))
            }
        }
    }

    val gridColor = Color.Gray.copy(alpha =  0.15f)
    for (i in 0 until 50) {
        drawLine(gridColor,
            Offset(i * cellSize, 0f),
            Offset(i * cellSize,size.height),
            strokeWidth = 0.5f)
        drawLine(gridColor,
            Offset(0f, i * cellSize),
            Offset(size.width, i * cellSize),
            strokeWidth = 0.5f)
    }
}
@Composable
fun DrawingCanvas(onRecognize: (Matrix) -> Unit) {
    var pixels by remember {mutableStateOf(createEmptyPixels())}
    var lastTouchPos by remember { mutableStateOf<Offset?>(null)}

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

        Box(modifier = Modifier.size(340.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF42A5F5))
            .padding(6.dp)) {

            Canvas(modifier = Modifier.fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit){

                    detectDragGestures(
                        onDragStart =  {lastTouchPos = it},
                        onDrag =  {change, _ ->
                            val cellSize = this.size.width / 50f
                            val cx = (change.position.x / cellSize).toInt().coerceIn(0,49)
                            val cy = (change.position.y / cellSize).toInt().coerceIn(0,49)
                            lastTouchPos?.let { start ->
                                val sx = (start.x / cellSize).toInt().coerceIn(0,49)
                                val sy = (start.y / cellSize).toInt().coerceIn(0,49)
                                pixels = drawLine(pixels, sx,sy,cx,cy)
                            }
                            lastTouchPos = change.position
                        },
                        onDragEnd =  {lastTouchPos = null},
                        onDragCancel = {lastTouchPos = null}
                    )

            }) {
                renderDrawing(pixels, size.width / 50f)
            }
        }

        Button(onClick = {onRecognize(pixelsToMatrix(pixels))}) {Text("Распознать") }

        Button(onClick = {
            pixels = createEmptyPixels()
            lastTouchPos = null
        }) {
            Text("Очистить")
        }
    }

}