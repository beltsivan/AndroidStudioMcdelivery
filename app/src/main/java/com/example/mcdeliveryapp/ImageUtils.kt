package com.example.mcdeliveryapp

import android.widget.ImageView
import com.bumptech.glide.Glide

fun loadImage(view: ImageView, imageName: String) {
    if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
        Glide.with(view.context)
            .load(imageName)
            .into(view)
    } else {
        val resId = view.context.resources.getIdentifier(imageName, "drawable", view.context.packageName)
        if (resId != 0) {
            Glide.with(view.context).load(resId).into(view)
        } else {
            view.setImageDrawable(null)
        }
    }
}
