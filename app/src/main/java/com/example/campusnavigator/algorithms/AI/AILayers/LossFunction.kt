package com.example.campusnavigator.algorithms.AI.AILayers

import com.example.campusnavigator.algorithms.AI.Matrix
import kotlin.math.ln

fun softmax(input: Matrix): Matrix {
    val exp = input.map { Math.exp(it) }
    val sumExp = exp.sum()
    return exp.map { it / sumExp }
}