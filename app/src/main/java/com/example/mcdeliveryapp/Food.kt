package com.example.mcdeliveryapp

data class Food(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val image: String = "",
    val categoryId: String = "",
)

data class MenuCategory(
    val id: String = "",
    val name: String = "",
    val image: String = "",
)
