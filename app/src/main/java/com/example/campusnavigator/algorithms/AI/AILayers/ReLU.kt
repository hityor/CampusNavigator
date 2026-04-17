package com.example.campusnavigator.algorithms.AI.AILayers

import com.example.campusnavigator.algorithms.AI.Matrix

class ReLU {

    private var lastInput: Matrix? = null

    fun forward(input: Matrix): Matrix {
        lastInput = input
        return input.map { if (it > 0) it else 0.0 }
    }
}