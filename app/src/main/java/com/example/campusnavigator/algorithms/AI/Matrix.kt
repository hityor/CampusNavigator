package com.example.campusnavigator.algorithms.AI

class Matrix(data: Array<DoubleArray>) {

    constructor(rows: Int, cols: Int) : this(Array(rows) { DoubleArray(cols) })

    constructor(rows: Int, cols: Int, init: (Int, Int) -> Double) : this(rows, cols) {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                data[i][j] = init(i, j)
            }
        }
    }

    private val data: Array<DoubleArray> = Array(data.size) { i ->
        data[i].copyOf()
    }

    val rows: Int = data.size
    val cols: Int = if (data.isNotEmpty()) data[0].size else 0

    operator fun get(i: Int, j: Int): Double = data[i][j]

    operator fun set(i: Int, j: Int, value: Double) {
        data[i][j] = value
    }

    fun map(transform: (Double) -> Double): Matrix {
        val result = Array(rows) { DoubleArray(cols) }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i][j] = transform(data[i][j])
            }
        }

        return Matrix(result)
    }

    fun mapWithIndex(transform: (i: Int, j: Int, value: Double) -> Double) : Matrix {

        val result = Array(rows) { DoubleArray(cols) }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i][j] = transform(i,j, data[i][j])
            }
        }

        return Matrix(result)
    }

    fun sum(): Double {

        var res = 0.0

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                res += data[i][j]
            }
        }

        return res
    }

    operator fun times(other: Matrix): Matrix {

        require(cols == other.rows) { "невозможно перемножить матрицы, размеры разные" }

        val result = Array(rows) { DoubleArray(other.cols) }

        for (i in 0 until rows) {
            for (j in 0 until other.cols) {
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += data[i][k] * other.data[k][j]
                }
                result[i][j] = sum
            }
        }

        return Matrix(result)
    }




    operator fun plus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Матрицы не одноразмерные" }
        return mapWithIndex { i, j, value -> value + other.data[i][j] }
    }

    operator fun minus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Матрицы не одноразмерные" }
        return mapWithIndex { i, j, value -> value - other.data[i][j] }
    }

    operator fun times(value: Double): Matrix {
        return map {it * value}
    }

    fun transpose() : Matrix {
        val result = Array(cols) { DoubleArray(rows) }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[j][i] = data[i][j]
            }
        }

        return Matrix(result)
    }
}