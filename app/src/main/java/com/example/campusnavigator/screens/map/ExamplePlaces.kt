package com.example.campusnavigator.screens.map

import com.example.campusnavigator.screens.map.models.FoodItem
import com.example.campusnavigator.screens.map.models.FoodPlace

val sampleFoodPlaces = listOf(
    FoodPlace("1", "Столовая ТГУ", 56.4694, 84.9468,
        menu = setOf(FoodItem.SOUP, FoodItem.SALAD, FoodItem.MAIN_DISH, FoodItem.SIDE_DISH, FoodItem.SODA),
        openFromMinutes = 9 * 60, openToMinutes = 17 * 60
    ),
    FoodPlace("2", "Старбукс", 56.4695, 84.9461,
        menu = setOf(FoodItem.COFFEE, FoodItem.TEA, FoodItem.PASTRY, FoodItem.DESSERT, FoodItem.SNACK),
        openFromMinutes = 8 * 60, openToMinutes = 22 * 60
    ),
    FoodPlace("3", "Сибирские блины", 56.4693, 84.9466,
        menu = setOf(FoodItem.BREAKFAST, FoodItem.DESSERT, FoodItem.SODA, FoodItem.SNACK),
        openFromMinutes = 9 * 60, openToMinutes = 21 * 60
    ),
    FoodPlace("4", "Ярче 1", 56.4739, 84.9446,
        menu = setOf(FoodItem.SNACK, FoodItem.SANDWICH, FoodItem.DISPOSABLE_TABLEWARE, FoodItem.COFFEE, FoodItem.SODA),
        openFromMinutes = 7 * 60, openToMinutes = 23 * 60
    ),
    FoodPlace("5", "Ярче 2", 56.4714, 84.9536,
        menu = setOf(FoodItem.SNACK, FoodItem.SANDWICH, FoodItem.DISPOSABLE_TABLEWARE, FoodItem.SODA),
        openFromMinutes = 7 * 60, openToMinutes = 23 * 60
    ),
    FoodPlace("6", "Кафе второго корпуса", 56.4686, 84.9451,
        menu = setOf(FoodItem.SOUP, FoodItem.SALAD, FoodItem.MAIN_DISH, FoodItem.COFFEE, FoodItem.TEA, FoodItem.DESSERT),
        openFromMinutes = 9 * 60, openToMinutes = 16 * 60
    ),
    FoodPlace("7", "Абрикос", 56.4714, 84.9411,
        menu = setOf(FoodItem.COFFEE, FoodItem.TEA, FoodItem.DESSERT, FoodItem.PASTRY, FoodItem.SNACK),
        openFromMinutes = 8 * 60, openToMinutes = 21 * 60
    ),
    FoodPlace("8", "Ростикс", 56.4691, 84.9513,
        menu = setOf(FoodItem.BURGER, FoodItem.SNACK, FoodItem.COFFEE, FoodItem.SODA, FoodItem.DESSERT),
        openFromMinutes = 10 * 60, openToMinutes = 22 * 60
    ),
    FoodPlace("9", "СырБор", 56.4707, 84.9461,
        menu = setOf(FoodItem.PIZZA, FoodItem.SALAD, FoodItem.SODA, FoodItem.DESSERT),
        openFromMinutes = 11 * 60, openToMinutes = 23 * 60
    ),
    FoodPlace("10", "Минутка", 56.4693, 84.9468,
        menu = setOf(FoodItem.SNACK, FoodItem.COFFEE, FoodItem.TEA, FoodItem.DISPOSABLE_TABLEWARE, FoodItem.PASTRY),
        openFromMinutes = 7 * 60, openToMinutes = 20 * 60
    ),
    FoodPlace("11", "Магнолия", 56.4732, 84.9445,
        menu = setOf(FoodItem.SUSHI, FoodItem.SALAD, FoodItem.SODA, FoodItem.DESSERT, FoodItem.SNACK),
        openFromMinutes = 8 * 60, openToMinutes = 23 * 60
    ),
)
