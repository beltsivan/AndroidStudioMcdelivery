package com.example.mcdeliveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val orders: List<Map<String, Any>>
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtOrderId: TextView = view.findViewById(R.id.txtOrderId)
        val txtOrderStatus: TextView = view.findViewById(R.id.txtOrderStatus)
        val txtOrderDate: TextView = view.findViewById(R.id.txtOrderDate)
        val txtItemsSummary: TextView = view.findViewById(R.id.txtItemsSummary)
        val txtDeliveryAddress: TextView = view.findViewById(R.id.txtDeliveryAddress)
        val txtPaymentMethod: TextView = view.findViewById(R.id.txtPaymentMethod)
        val txtOrderSubtotal: TextView = view.findViewById(R.id.txtOrderSubtotal)
        val txtOrderDeliveryFee: TextView = view.findViewById(R.id.txtOrderDeliveryFee)
        val txtOrderTotal: TextView = view.findViewById(R.id.txtOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val orderId = (order["orderId"] as? String) ?: ""
        holder.txtOrderId.text = "Order #${orderId.take(8).uppercase()}"

        val status = order["status"] as? String ?: "Pending"
        holder.txtOrderStatus.text = status

        if (status == "Delivered") {
            holder.txtOrderStatus.setTextColor(android.graphics.Color.WHITE)
            holder.txtOrderStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            holder.txtOrderStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            holder.txtOrderStatus.background = null
        }

        val timestamp = order["createdAt"] as? com.google.firebase.Timestamp
        if (timestamp != null) {
            val date = timestamp.toDate()
            val sdf = SimpleDateFormat("MMM dd, yyyy \u2022 hh:mm a", Locale.getDefault())
            holder.txtOrderDate.text = sdf.format(date)
        } else {
            holder.txtOrderDate.text = ""
        }

        val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
        val itemNames = items.mapNotNull { it["name"] as? String }
        val summary = if (itemNames.size <= 3) {
            itemNames.joinToString(", ")
        } else {
            "${itemNames.take(3).joinToString(", ")} and ${itemNames.size - 3} more"
        }
        holder.txtItemsSummary.text = summary

        @Suppress("UNCHECKED_CAST")
        val address = order["deliveryAddress"] as? Map<String, Any>
        if (address != null) {
            val parts = listOfNotNull(
                address["street"] as? String,
                address["barangay"] as? String,
                address["municipality"] as? String,
                address["province"] as? String,
                address["postalCode"] as? String
            ).filter { it.isNotEmpty() }
            holder.txtDeliveryAddress.text = parts.joinToString(", ")
        } else {
            holder.txtDeliveryAddress.text = ""
        }

        val paymentMethod = order["paymentMethod"] as? String ?: ""
        holder.txtPaymentMethod.text = paymentMethod

        val subtotal = (order["subtotal"] as? Double) ?: 0.0
        val deliveryFee = (order["deliveryFee"] as? Double) ?: 0.0
        val total = (order["total"] as? Double) ?: 0.0

        holder.txtOrderSubtotal.text = String.format("\u20B1%.2f", subtotal)
        holder.txtOrderDeliveryFee.text = String.format("\u20B1%.2f", deliveryFee)
        holder.txtOrderTotal.text = String.format("\u20B1%.2f", total)
    }

    override fun getItemCount() = orders.size
}
