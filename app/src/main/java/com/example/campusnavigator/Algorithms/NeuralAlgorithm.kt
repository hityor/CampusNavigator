package com.example.campusnavigator.Algorithms

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import androidx.core.graphics.get

object NeuralAlgorithm {

    private var interpreter: Interpreter? = null
    private const val MODEL_PATH = "Omnissiah.tflite"
    private const val INPUT_SIZE = 28
    private const val NUM_CLASSES = 10

    fun initialize(context: Context) {
        if (interpreter == null) {
            val model = loadModelFile(context)
            interpreter = Interpreter(model)
        }
    }


    private fun loadModelFile(context: Context): ByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    fun recognizeDigit(bitmap: Bitmap): Int {

        if (interpreter == null) {
            throw IllegalStateException("DigitRecognizer не инициализирован! Вызовите initialize()")
        }

        val inputBuffer = preprocessBitmap(bitmap)

        val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) }

        interpreter?.run(inputBuffer, outputBuffer)


        return outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: 0
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE)
        buffer.order(ByteOrder.nativeOrder())

        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixel = bitmap[j, i]

                val gray = android.graphics.Color.red(pixel)

                val normalized = gray / 255.0f

                buffer.putFloat(normalized)
            }
        }

        buffer.rewind()
        return buffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}