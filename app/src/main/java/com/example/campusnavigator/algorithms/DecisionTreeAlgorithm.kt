package com.example.campusnavigator.algorithms

import android.content.Context
import com.example.campusnavigator.screens.DecisionTree.GraphNode
import org.json.JSONObject


data class FeatureSpinner(
    val name: String,
    val options: List<String>,
    var selected: String = ""
)

fun loadJsonFromAssets(context: Context, filename: String): String {
    return context.assets.open(filename).bufferedReader().use { it.readText() }
}

fun loadTree(context: Context): JSONObject {
    return JSONObject(loadJsonFromAssets(context, "tree_model.json"))
}

fun loadFeatureNames(context: Context): List<String> {
    val json = loadJsonFromAssets(context, "feature_names.json")
    val array = org.json.JSONArray(json)
    return List(array.length()) { array.getString(it) }
}

fun convertJsonToGraph(node: JSONObject, path: List<String>, nodeId: String = "root"): GraphNode {
    try {
        val isLeaf = node.getBoolean("leaf")

        val feature = if (!isLeaf) node.getString("feature") else null
        val value = if (!isLeaf) node.getString("value") else null
        val result = if (isLeaf) node.getString("class") else null

        val isHighlighted = if (isLeaf) {
            path.lastOrNull()?.contains(result ?: "") == true ||
                    path.any { it.contains(result ?: "") }
        }
        else {
            val condition = "$feature == $value"
            path.any { it == condition || it.contains(condition) }
        }

        return GraphNode(
            id = nodeId,
            feature = feature,
            value = value,
            result = result,
            isLeaf = isLeaf,
            isHighlighted = isHighlighted,
            left = if (!isLeaf) {
                try {
                    convertJsonToGraph(node.getJSONObject("left"), path, "${nodeId}_L")
                } catch (e: Exception) {
                    println("Ошибка левого потомка $nodeId: ${e.message}")
                    null
                }
            } else null,
            right = if (!isLeaf) {
                try {
                    convertJsonToGraph(node.getJSONObject("right"), path, "${nodeId}_R")
                } catch (e: Exception) {
                    println("Ошибка правого потомка $nodeId: ${e.message}")
                    null
                }
            } else null
        )
    } catch (e: Exception) {
        println("Ошибка в узле $nodeId: ${e.message}")
        e.printStackTrace()
        return GraphNode(
            id = nodeId,
            feature = null,
            value = null,
            result = "Ошибка",
            isLeaf = true,
            isHighlighted = false,
            left = null,
            right = null
        )
    }
}

val featureOptions = mapOf(
    "location" to listOf("main_building", "second_building", "campus_center", "bus_stop"),
    "budget" to listOf("low", "medium", "high"),
    "time_available" to listOf("very_short", "short", "medium"),
    "food_type" to listOf("coffee", "pancakes", "full_meal", "snack"),
    "queue_tolerance" to listOf("low", "medium", "high"),
    "weather" to listOf("good", "bad")
)



fun predict(node: JSONObject, userData: Map<String, String>): Pair<String, List<String>> {
    var currentNode = node
    val path = mutableListOf<String>()

    while (!currentNode.getBoolean("leaf")) {
        val feature = currentNode.getString("feature")
        val value = currentNode.getString("value")
        val userValue = userData[feature] ?: ""

        if (userValue == value) {
            path.add("$feature == $value")
            currentNode = currentNode.getJSONObject("left")
        } else {
            path.add("$feature != $value")
            currentNode = currentNode.getJSONObject("right")
        }
    }

    val result = currentNode.getString("class")
    return Pair(result, path)
}

fun renderTree(node: JSONObject, indent: Int = 0): String {
    val prefix = "  ".repeat(indent)
    return if (node.getBoolean("leaf")) {
        "${prefix}${node.getString("class")}\n"
    } else {
        val feature = node.getString("feature")
        val value = node.getString("value")
        val left = node.getJSONObject("left")
        val right = node.getJSONObject("right")

        buildString {
            append("${prefix} $feature == $value\n")
            append("${prefix}")
            append(renderTree(left, indent + 1))
            append("${prefix}")
            append(renderTree(right, indent + 1))
        }
    }
}