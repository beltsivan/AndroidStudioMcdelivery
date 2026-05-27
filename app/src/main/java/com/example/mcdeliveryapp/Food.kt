package com.example.mcdeliveryapp

data class Food(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val image: String = "",
    val categoryId: String = "",
    val order: Int = 0,
    val quantity: Int = 1,
    val isAvailable: Boolean = true
)

data class MenuCategory(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val order: Int = 0
)
