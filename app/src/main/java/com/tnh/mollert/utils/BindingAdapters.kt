package com.tnh.mollert.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.tnh.mollert.R
import com.tnh.tnhlibrary.logAny


@BindingAdapter("app:bindImageUri")
fun ImageView.bindImageUri(uri: String?){
    if(uri.isNullOrEmpty().not()){
        "go here".logAny()
        Glide.with(this.context).load(uri).into(this)
    }else {
        "K here".logAny()
        setImageResource(R.drawable.app_icon)
    }
}