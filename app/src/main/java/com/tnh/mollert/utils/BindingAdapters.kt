package com.tnh.mollert.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.tnh.mollert.R
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show


@BindingAdapter("app:bindImageUri")
fun ImageView.bindImageUri(uri: String?){
    if(uri.isNullOrEmpty().not()){
        Glide.with(this.context).load(uri).into(this)
    }else {
        setImageResource(R.drawable.app_icon)
    }
}

@BindingAdapter("app:bindImageUriOrHide")
fun ImageView.bindImageUriOrHide(uri: String?){
    if(uri.isNullOrEmpty().not()){
        Glide.with(this.context).load(uri).into(this)
        show()
    }else {
        gone()
    }
}


