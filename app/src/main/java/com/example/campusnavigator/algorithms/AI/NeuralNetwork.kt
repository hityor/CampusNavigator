package com.example.campusnavigator.algorithms.AI

import android.content.Context
import com.example.campusnavigator.algorithms.AI.AILayers.softmax
import com.example.campusnavigator.algorithms.AI.AILayers.DenseLayer
import com.example.campusnavigator.algorithms.AI.AILayers.ReLU


class NeuralNetwork() {

    private val layers = mutableListOf<Any>()

    fun addDense(inputSize: Int, outputSize: Int) {
        layers.add(DenseLayer(inputSize, outputSize))
    }

    fun addReLU() {
        layers.add(ReLU())
    }

    private fun forwardRaw(input: Matrix): Matrix {
        var current = input
        for (layer in layers) {
            current = when (layer) {
                is DenseLayer -> layer.forward(current)
                is ReLU -> layer.forward(current)
                else -> error("Неизвестный тип слоя")
            }
        }
        return current
    }


    fun predict(input: Matrix): Matrix {
        val raw = forwardRaw(input)
        return softmax(raw)
    }

    fun loadWeightsFromFile(context: Context, filename: String) {
        val inputStream = context.assets.open(filename)
        val lines = inputStream.bufferedReader().readLines()
        var idx = 0

        for (layer in layers) {
            if (layer is DenseLayer) {
                for (i in 0 until layer.getOutputSize()) {
                    for (j in 0 until layer.getInputSize()) {
                        layer.setWeights(i,j,lines[idx++].toDouble())
                    }
                }

                for (i in 0 until layer.getOutputSize()) {
                    layer.setBias(i,0,lines[idx++].toDouble())
                }
            }
        }
    }
}