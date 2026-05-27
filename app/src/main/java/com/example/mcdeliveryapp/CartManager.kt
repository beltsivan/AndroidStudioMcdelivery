package com.example.mcdeliveryapp

import com.google.firebase.firestore.FirebaseFirestore

object CartManager {
    const val DELIVERY_FEE = 49.0
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

    fun getTotal(): Double {
        return getSubtotal() + DELIVERY_FEE
    }

    fun clearCart() {
        cartList.clear()
    }

    fun saveCartToFirestore(db: FirebaseFirestore, userId: String) {
        val items = cartList.map { food ->
            hashMapOf(
                "id" to food.id,
                "name" to food.name,
                "price" to food.price,
                "image" to food.image,
                "categoryId" to food.categoryId,
                "order" to food.order,
                "quantity" to food.quantity
            )
        }
        db.collection("carts").document(userId).set(hashMapOf("items" to items))
    }

    fun loadCartFromFirestore(db: FirebaseFirestore, userId: String, onLoaded: () -> Unit) {
        db.collection("carts").document(userId).get()
            .addOnSuccessListener { doc ->
                cartList.clear()
                if (doc.exists()) {
                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    for (item in items) {
                        cartList.add(
                            Food(
                                id = (item["id"] as? String) ?: "",
                                name = (item["name"] as? String) ?: "",
                                price = (item["price"] as? Double)
                                    ?: (item["price"] as? Long)?.toDouble() ?: 0.0,
                                image = (item["image"] as? String) ?: "",
                                categoryId = (item["categoryId"] as? String) ?: "",
                                order = ((item["order"] as? Long)?.toInt())
                                    ?: (item["order"] as? Int) ?: 0,
                                quantity = ((item["quantity"] as? Long)?.toInt())
                                    ?: (item["quantity"] as? Int) ?: 1
                            )
                        )
                    }
                }
                onLoaded()
            }
            .addOnFailureListener { onLoaded() }
    }
}