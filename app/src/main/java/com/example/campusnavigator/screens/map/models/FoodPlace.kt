package com.example.campusnavigator.screens.map.models

class FoodPlace(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val menu: Set<String> = emptySet(),
    val openFromMinutes: Int = 0,
    val openToMinutes: Int = 24 * 60
) {
    fun isOpenAt(minuteOfDay: Int): Boolean =
        minuteOfDay in openFromMinutes until openToMinutes
    fun minutesUntilClose(minuteOfDay: Int): Int =
        (openToMinutes - minuteOfDay).coerceAtLeast(0)
}

object FoodItem {
    const val DISPOSABLE_TABLEWARE = "Одноразовая посуда"
    const val SOUP = "Суп"
    const val SALAD = "Салат"
    const val SANDWICH = "Бутерброд"
    const val MAIN_DISH = "Горячее"
    const val SIDE_DISH = "Гарнир"
    const val SODA = "Газировка"
    const val PASTRY = "Выпечка"
    const val PIZZA = "Пицца"
    const val SUSHI = "Суши"
    const val BURGER = "Бургер"
    const val DESSERT = "Десерт"
    const val SNACK = "Закуска"
    const val BREAKFAST = "Завтрак"
    const val COFFEE = "Кофе"
    const val TEA = "Чай"

    val ALL = listOf(
        DISPOSABLE_TABLEWARE, SOUP, SALAD, SANDWICH, MAIN_DISH, SIDE_DISH, SODA, PASTRY,
        PIZZA, SUSHI, BURGER, DESSERT, SNACK, BREAKFAST, COFFEE, TEA
    )
}
