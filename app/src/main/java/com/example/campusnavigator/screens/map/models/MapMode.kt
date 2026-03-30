package com.example.campusnavigator.screens.map.models

enum class MapMode(val title: String) {
    ASTAR("Навигация (A*)"),
    CLUSTERING("Кластеризация"),
    GENETIC("Маршрут для еды"),
    ANT("Тур по кампусу"),
    COWORKING("Коворкинги")
}