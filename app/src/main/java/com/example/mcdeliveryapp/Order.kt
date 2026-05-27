package com.example.mcdeliveryapp

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val contactNumber: String = "",
    val deliveryAddress: Map<String, String> = emptyMap(),
    val branchId: String = "",
    val branchName: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "Pending",
    val createdAt: Timestamp? = null
)
