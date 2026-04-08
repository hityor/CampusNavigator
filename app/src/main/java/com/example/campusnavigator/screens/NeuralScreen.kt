package com.example.campusnavigator.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.example.campusnavigator.algorithms.NeuralAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingBottomSheet(
    onDismiss: () -> Unit,
    onSubmitted: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var recognizedDigit by remember { mutableStateOf<String>("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Нарисуйте цифру",
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )


            DrawingCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                onBitmapReady = { bitmap ->
                    scope.launch(Dispatchers.IO) {
                        val digit = NeuralAlgorithm.recognizeDigit(bitmap)
                        withContext(Dispatchers.Main) {
                            recognizedDigit = digit.toString()
                        }
                    }
                }
            )

            if (recognizedDigit.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Цифра: $recognizedDigit",
                        color = Color.Green,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { _recognizeTrigger = true },
                modifier = Modifier.padding(16.dp),
                enabled = recognizedDigit.isEmpty()
            ) {
                Text("Распознать")
            }

            if (recognizedDigit.isNotEmpty()) {
                Button(
                    onClick = {
                        recognizedDigit = ""
                        _recognizeTrigger = false
                        _clearTrigger = true
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Нарисовать заново")
                }
            }


            Button(
                onClick = onDismiss,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Закрыть")
            }
        }
    }
}

private var _recognizeTrigger by mutableStateOf(false)
private var _clearTrigger by mutableStateOf(false)

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    onBitmapReady: (Bitmap) -> Unit
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(_recognizeTrigger) {
        if (_recognizeTrigger) {
            val bitmap = pathsToBitmap(paths, size = 28)
            onBitmapReady(bitmap)
            _recognizeTrigger = false
        }
    }

    LaunchedEffect(_clearTrigger) {
        if (_clearTrigger) {
            paths.clear()
            currentPath.clear()
            _clearTrigger = false
        }
    }

    Canvas(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    currentPath.clear()
                    currentPath.add(down.position)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                        val offset = event.changes.first().position
                        currentPath.add(offset)
                    } while (event.changes.any { it.pressed })
                    if (currentPath.isNotEmpty()) {
                        paths.add(currentPath.toList())
                        currentPath.clear()
                    }
                }
            }
    ) {
        paths.forEach { path ->
            if (path.size > 1) {
                val composePath = Path().apply {
                    moveTo(path[0].x, path[0].y)
                    for (i in 1 until path.size) lineTo(path[i].x, path[i].y)
                }
                drawPath(path = composePath, color = Color.White, style = Stroke(width = 40f))
            }
        }
        if (currentPath.isNotEmpty()) {
            val composePath = Path().apply {
                moveTo(currentPath[0].x, currentPath[0].y)
                for (i in 1 until currentPath.size) lineTo(currentPath[i].x, currentPath[i].y)
            }
            drawPath(path = composePath, color = Color.White, style = Stroke(width = 40f))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                paths.clear()
                currentPath.clear()
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Очистить")
        }
    }
}

private fun pathsToBitmap(paths: List<List<Offset>>, size: Int): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.BLACK)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        strokeWidth = 3f
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE

    paths.forEach { path ->
        path.forEach { offset ->
            minX = minOf(minX, offset.x)
            minY = minOf(minY, offset.y)
            maxX = maxOf(maxX, offset.x)
            maxY = maxOf(maxY, offset.y)
        }
    }

    val drawWidth = maxX - minX
    val drawHeight = maxY - minY
    val scale = if (drawWidth > 0 || drawHeight > 0) {
        minOf((size - 4) / drawWidth, (size - 4) / drawHeight)
    } else {
        1f
    }

    val offsetX = (size - drawWidth * scale) / 2 - minX * scale
    val offsetY = (size - drawHeight * scale) / 2 - minY * scale

    paths.forEach { path ->
        if (path.size > 1) {
            val androidPath = android.graphics.Path().apply {
                moveTo(path[0].x * scale + offsetX, path[0].y * scale + offsetY)
                for (i in 1 until path.size) {
                    lineTo(path[i].x * scale + offsetX, path[i].y * scale + offsetY)
                }
            }
            canvas.drawPath(androidPath, paint)
        }
    }

    return bitmap
}