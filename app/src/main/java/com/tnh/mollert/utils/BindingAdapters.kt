package com.tnh.mollert.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.tnh.mollert.R


@BindingAdapter("app:bindImageUri")
fun ImageView.bindImageUri(uri: String?){
    if(uri.isNullOrEmpty().not()){
        Glide.with(this.context).load(uri).into(this)
    }else {
        setImageResource(R.drawable.app_icon)
    }
}