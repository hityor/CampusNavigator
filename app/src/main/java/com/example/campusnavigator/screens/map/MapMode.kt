package com.example.campusnavigator.screens.map

enum class MapMode(val title: String) {
    ASTAR("Навигация (A*)"),
    CLUSTERING("Кластеризация"),
    GENETIC("Маршрут для еды"),
    ANT("Тур по кампусу"),
    COWORKING("Коворкинги")
}