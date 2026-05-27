package com.example.mcdeliveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CouponAdapter(
    private val coupons: List<Map<String, Any>>
) : RecyclerView.Adapter<CouponAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCouponDiscount: TextView = view.findViewById(R.id.txtCouponDiscount)
        val txtCouponCode: TextView = view.findViewById(R.id.txtCouponCode)
        val txtCouponDesc: TextView = view.findViewById(R.id.txtCouponDesc)
        val txtCouponMin: TextView = view.findViewById(R.id.txtCouponMin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coupon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coupon = coupons[position]
        val code = coupon["code"] as? String ?: "?"
        val desc = coupon["description"] as? String ?: ""
        val discountType = coupon["discountType"] as? String ?: "fixed"
        val discountValue = (coupon["discountValue"] as? Double) ?: (coupon["discountValue"] as? Long)?.toDouble() ?: 0.0
        val minOrder = (coupon["minOrderAmount"] as? Double) ?: (coupon["minOrderAmount"] as? Long)?.toDouble() ?: 0.0

        val discountText = if (discountType == "percentage") "${discountValue.toInt()}%" else "\u20B1${String.format("%.0f", discountValue)}"
        holder.txtCouponDiscount.text = discountText
        holder.txtCouponCode.text = code
        holder.txtCouponDesc.text = desc
        holder.txtCouponMin.text = if (minOrder > 0) "Min. order: \u20B1${String.format("%.0f", minOrder)}" else "No min. order"
    }

    override fun getItemCount() = coupons.size
}
