package com.example.campusnavigator.algorithms.AI

import android.content.Context

object NeuralAlgorithm {
    private lateinit var network: NeuralNetwork

    fun init(context: Context) {
        if (!::network.isInitialized) {
            network = NeuralNetwork()
            network.addDense(2500, 512)
            network.addReLU()
            network.addDense(512, 256)
            network.addReLU()
            network.addDense(256, 128)
            network.addReLU()
            network.addDense(128, 64)
            network.addReLU()
            network.addDense(64, 10)
            network.loadWeightsFromFile(context, "weights_50x50.txt")
        }
    }

    fun recognizeDigit(matrix: Matrix): Int {
        val prepared = centerAndScaleTo50x50(matrix)
        val output = network.predict(prepared)

        val inputSum = prepared.sum()
        val top3 = (0 until 10)
            .map { it to output[it, 0] }
            .sortedByDescending { it.second }
            .take(3)

        android.util.Log.d("NN_DEBUG",
            "InputSum: ${"%.1f".format(inputSum)} | " +
                    "Top3: ${top3.joinToString { "${it.first}=${"%.3f".format(it.second)}" }}"
        )

        var maxIndex = 0
        var maxValue = output[0, 0]
        for (i in 1 until 10) {
            if (output[i, 0] > maxValue) {
                maxValue = output[i, 0]
                maxIndex = i
            }
        }
        return maxIndex
    }

    private fun findBoundingBox(matrix50: Matrix): Pair<IntArray, Boolean> {
        val bounds = intArrayOf(50, 0, 50, 0)
        var hasDrawing = false
        for (y in 0 until 50) {
            for (x in 0 until 50) {
                if (matrix50[y * 50 + x, 0] > 0.0) {
                    hasDrawing = true
                    bounds[0] = minOf(bounds[0], x)
                    bounds[1] = maxOf(bounds[1], x)
                    bounds[2] = minOf(bounds[2], y)
                    bounds[3] = maxOf(bounds[3], y)
                }
            }
        }
        return Pair(bounds, hasDrawing)
    }

    private fun centerAndScaleTo50x50(matrix50: Matrix): Matrix {
        val (bounds, hasDrawing) = findBoundingBox(matrix50)
        if (!hasDrawing) return Matrix(2500, 1)

        val (minX, maxX, minY, maxY) = bounds
        val width = maxX - minX + 1
        val height = maxY - minY + 1

        val scale = minOf(40.0 / width, 40.0 / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val result = Matrix(2500, 1)
        val offsetX = (50 - newWidth) / 2
        val offsetY = (50 - newHeight) / 2

        for (ty in 0 until newHeight) {
            for (tx in 0 until newWidth) {
                val srcXStart = (minX + tx / scale).toInt().coerceIn(0, 49)
                val srcXEnd = (minX + (tx + 1) / scale).toInt().coerceIn(0, 49)
                val srcYStart = (minY + ty / scale).toInt().coerceIn(0, 49)
                val srcYEnd = (minY + (ty + 1) / scale).toInt().coerceIn(0, 49)

                var sum = 0.0; var count = 0
                for (sy in srcYStart..srcYEnd) {
                    for (sx in srcXStart..srcXEnd) {
                        sum += matrix50[sy * 50 + sx, 0]
                        count++
                    }
                }

                val dstX = tx + offsetX
                val dstY = ty + offsetY
                if (dstX in 0..49 && dstY in 0..49 && count > 0) {
                    result[dstY * 50 + dstX, 0] = sum / count
                }
            }
        }
        return result
    }
}