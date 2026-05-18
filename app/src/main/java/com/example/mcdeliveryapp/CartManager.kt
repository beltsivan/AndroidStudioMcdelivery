package com.example.mcdeliveryapp

object CartManager {
    val cartList = mutableListOf<Food>()

    fun addItem(food: Food) {
        val existing = cartList.find { it.id == food.id && it.name == food.name }
        if (existing != null) {
            val index = cartList.indexOf(existing)
            cartList[index] = existing.copy(quantity = existing.quantity + food.quantity)
        } else {
            cartList.add(food)
        }
    }

    fun incrementQuantity(position: Int) {
        if (position < cartList.size) {
            val item = cartList[position]
            cartList[position] = item.copy(quantity = item.quantity + 1)
        }
    }

    fun decrementQuantity(position: Int): Boolean {
        if (position < cartList.size) {
            val item = cartList[position]
            if (item.quantity > 1) {
                cartList[position] = item.copy(quantity = item.quantity - 1)
                return false
            } else {
                cartList.removeAt(position)
                return true
            }
        }
        return false
    }

    fun getSubtotal(): Double {
        return cartList.sumOf { it.price * it.quantity }
    }
}