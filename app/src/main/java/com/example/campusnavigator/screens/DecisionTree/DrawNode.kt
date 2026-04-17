package com.example.campusnavigator.screens.DecisionTree


import android.text.Highlights
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.campusnavigator.algorithms.calculateNodePositions


data class GraphNode(
    val id: String,
    val feature: String?,
    val value: String?,
    val result: String?,
    val isLeaf: Boolean,
    val left: GraphNode?,
    val right: GraphNode?,
    val isHighlighted: Boolean = false
)

data class TreeNodePosition(
    val node: GraphNode,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Composable
fun DrawTreeRoot(node: GraphNode, path: List<String>) {
    val (nodePositions, treeSize) = remember(node) { calculateNodePositions(node) }

    Canvas(
        modifier = Modifier
            .width(treeSize.width.dp)
            .height(treeSize.height.dp)
    ) {
        val lineHeight = 6f

        nodePositions.forEach { pos ->
            if (!pos.node.isLeaf) {
                val leftChild = nodePositions.find { it.node.id == "${pos.node.id}_L" }
                val rightChild = nodePositions.find { it.node.id == "${pos.node.id}_R" }

                if (leftChild != null) {
                    drawLine(
                        color = if (leftChild.node.isHighlighted) Color(0xFF00C853) else Color(0xFF90A4AE),
                        start = Offset(pos.x + pos.width / 2, pos.y + pos.height),
                        end = Offset(leftChild.x + leftChild.width / 2, leftChild.y),
                        strokeWidth = lineHeight
                    )
                }
                if (rightChild != null) {
                    drawLine(
                        color = if (rightChild.node.isHighlighted) Color(0xFF00C853) else Color(0xFF90A4AE),
                        start = Offset(pos.x + pos.width / 2, pos.y + pos.height),
                        end = Offset(rightChild.x + rightChild.width / 2, rightChild.y),
                        strokeWidth = lineHeight
                    )
                }
            }
        }

        nodePositions.forEach { pos ->
            drawRoundRect(
                color = if (pos.node.isHighlighted) Color(0xFF00C853) else Color(0xFF42A5F5),
                topLeft = Offset(pos.x, pos.y),
                size = androidx.compose.ui.geometry.Size(pos.width, pos.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            val text = if (pos.node.isLeaf) {
                "${pos.node.result}"
            }
            else {
                "${pos.node.feature}\n== ${pos.node.value}"
            }

            val textPaint = android.graphics.Paint().apply {
                color = Color.White.toArgb()
                textSize = 32f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            drawContext.canvas.nativeCanvas.apply {
                val lines = text.split("\n")
                val textLineHeight = 36f
                val totalTextHeight = lines.size * textLineHeight
                val startY = pos.y + pos.height / 2 - totalTextHeight / 2 + textLineHeight / 2

                lines.forEachIndexed { index, line ->
                    drawText(
                        line,
                        pos.x + pos.width / 2,
                        startY + index * textLineHeight,
                        textPaint
                    )
                }
            }
        }
    }
}

