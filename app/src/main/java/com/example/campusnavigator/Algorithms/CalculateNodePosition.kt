package com.example.campusnavigator.Algorithms

import com.example.campusnavigator.screens.DecisionTree.GraphNode
import com.example.campusnavigator.screens.DecisionTree.TreeNodePosition

fun calculateNodePositions(root: GraphNode): Pair<List<TreeNodePosition>, androidx.compose.ui.geometry.Size> {
    val positions = mutableListOf<TreeNodePosition>()
    val nodeWidth = 320f
    val nodeHeight = 90f
    val verticalSpacing = 120f
    val minHorizontalSpacing = 80f
    val nodesByLevel = mutableMapOf<Int, MutableList<GraphNode>>()

    fun collectNodesByLevel(node: GraphNode, level: Int) {
        if (!nodesByLevel.containsKey(level)) {
            nodesByLevel[level] = mutableListOf()
        }
        nodesByLevel[level]!!.add(node)

        if (!node.isLeaf) {
            if (node.left != null) collectNodesByLevel(node.left, level + 1)
            if (node.right != null) collectNodesByLevel(node.right, level + 1)
        }
    }

    collectNodesByLevel(root, 0)

    val maxLevel = nodesByLevel.keys.maxOrNull() ?: 0
    val centerX = 500f

    for (level in 0..maxLevel) {
        val nodes = nodesByLevel[level]!!
        val nodesCount = nodes.size

        val totalNodesWidth = nodesCount * nodeWidth
        val totalSpacingWidth = (nodesCount + 1) * minHorizontalSpacing
        val levelWidth = totalNodesWidth + totalSpacingWidth

        val startX = centerX - levelWidth / 2

        for ((index, node) in nodes.withIndex()) {
            val xPos = startX + (index + 1) * minHorizontalSpacing + index * nodeWidth + nodeWidth / 2

            positions.add(TreeNodePosition(
                node = node,
                x = xPos,
                y = level * (nodeHeight + verticalSpacing),
                width = nodeWidth,
                height = nodeHeight
            ))
        }
    }

    val offsetX = 50f
    val offsetY = 50f
    positions.replaceAll { it.copy(x = it.x + offsetX, y = it.y + offsetY) }


    val maxX = positions.maxOfOrNull { it.x + it.width } ?: 0f
    val maxY = positions.maxOfOrNull { it.y + it.height } ?: 0f
    val treeSize = androidx.compose.ui.geometry.Size(
        width = maxX + 50f,
        height = maxY + 50f
    )

    return Pair(positions, treeSize)
}