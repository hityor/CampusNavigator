package com.example.campusnavigator.algorithms.AI.AILayers

import com.example.campusnavigator.algorithms.AI.Matrix
import kotlin.math.sqrt

class DenseLayer( val layerInputSize: Int, val layerOutputSize: Int) {

    private val inputSize = layerInputSize
    private val outputSize = layerOutputSize
    private var weights = Matrix(outputSize, inputSize) { _, _ ->
        (Math.random() - 0.5) * sqrt(2.0 / inputSize)
    }
    private var bias = Matrix(outputSize, 1)
    private var lastInput: Matrix? = null

    fun getInputSize(): Int = inputSize
    fun getOutputSize(): Int = outputSize
    fun getWeights(): Matrix = weights
    fun getBias(): Matrix = bias
    fun getWeights(i: Int, j:Int): Double = weights[i,j]
    fun getBias(i: Int, j:Int): Double = bias[i,j]
    fun setWeights(i:Int, j:Int, value:Double){
        weights[i,j] = value
    }
    fun setBias(i:Int, j:Int, value:Double){
        bias[i,j] = value
    }


    fun forward(input: Matrix) : Matrix {

        lastInput = input

        return (weights * input) + bias
    }


}