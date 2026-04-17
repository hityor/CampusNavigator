package com.example.campusnavigator.screens.DecisionTree


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        color = if (leftChild.node.isHighlighted) Color(0xFF00C853) else Color(
                            0xFF90A4AE
                        ),
                        start = Offset(pos.x + pos.width / 2, pos.y + pos.height),
                        end = Offset(leftChild.x + leftChild.width / 2, leftChild.y),
                        strokeWidth = lineHeight
                    )
                }
                if (rightChild != null) {
                    drawLine(
                        color = if (rightChild.node.isHighlighted) Color(0xFF00C853) else Color(
                            0xFF90A4AE
                        ),
                        start = Offset(pos.x + pos.width / 2, pos.y + pos.height),
                        end = Offset(rightChild.x + rightChild.width / 2, rightChild.y),
                        strokeWidth = lineHeight
                    )
                }
            }
        }

        nodePositions.forEach { pos ->
            drawRoundRect(
                color = if (pos.node.isHighlighted) Color(0xFF00C853) else
                    if (pos.node.isLeaf) Color(0xFF66BB6A) else Color(0xFF42A5F5),
                topLeft = Offset(pos.x, pos.y),
                size = androidx.compose.ui.geometry.Size(pos.width, pos.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            val text = if (pos.node.isLeaf) {
                "${pos.node.result}"
            } else {
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

@Composable
fun DrawTreeNode(
    node: GraphNode,
    depth: Int,
    path: List<String>,
    xOffset: Dp
) {
    val nodeWidth = 600.dp
    val nodeHeight = 200.dp
    val verticalSpacing = 80.dp
    val horizontalOffset = 100.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(nodeWidth)
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(nodeWidth)
                .height(nodeHeight)
                .background(
                    color = if (node.isHighlighted) Color(0xFFEF5350) else
                        if (node.isLeaf) Color(0xFF66BB6A) else Color(0xFF42A5F5),
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (node.isLeaf) "${node.result}" else "${node.feature}\n== ${node.value}",
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 3
            )
        }

        if (!node.isLeaf) {
            if (node.left != null || node.right != null) {
                Canvas(modifier = Modifier
                    .width(2.dp)
                    .height(verticalSpacing / 2)) {
                    drawLine(
                        color = if (node.isHighlighted) Color(0xFFEF5350) else Color(0xFF90A4AE),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 3f
                    )
                }

                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)) {
                    drawLine(
                        color = if (node.isHighlighted) Color(0xFFEF5350) else Color(0xFF90A4AE),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 3f
                    )
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(verticalSpacing),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (node.left != null) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = if (node.left.isHighlighted) Color(0xFFEF5350) else Color(
                                        0xFF90A4AE
                                    ),
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = 3f
                                )
                            }
                            DrawTreeNode(node.left, depth + 1, path, xOffset - horizontalOffset)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(verticalSpacing),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (node.right != null) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = if (node.right.isHighlighted) Color(0xFFEF5350) else Color(
                                        0xFF90A4AE
                                    ),
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = 3f
                                )
                            }
                            DrawTreeNode(node.right, depth + 1, path, xOffset + horizontalOffset)
                        }
                    }
                }
            }
        }
    }
}