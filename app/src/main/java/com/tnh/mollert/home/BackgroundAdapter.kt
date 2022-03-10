package com.tnh.mollert.home

import android.widget.TextView
import com.google.android.material.textview.MaterialTextView
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BackgroudItemBinding
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny

class BackgroundAdapter: SimpleDataBindingListAdapter<String, BackgroudItemBinding>(R.layout.backgroud_item) {
    private var selectedItem: MaterialTextView? = null
    var selectedLink: String? = null
    private set

    var onBackgroundSelected: () -> Unit = {}

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<BackgroudItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            if(selectedItem == null && selectedLink.isNullOrEmpty()){
                selectItem(backgroundItemCheckBox, item)
            }else{
//                backgroundItemCheckBox.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_invisible, 0, 0, 0)
                checkSelected(backgroundItemCheckBox, item)
            }
            backgroundItemBackground.bindImageUri(item)
            root.setOnClickListener {
                selectItem(backgroundItemCheckBox, item)
                onBackgroundSelected()
            }
        }
    }


    private fun checkSelected(textView: MaterialTextView, item: String){
        if(selectedLink == item){
            selectedItem = textView
            selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_checked, 0, 0, 0)
        }else{
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_invisible, 0, 0, 0)
        }
    }

    fun setSelectedUri(uri: String){
        selectedLink = uri
    }

    fun clearSelected(){
        selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_invisible, 0, 0, 0)
        selectedItem = null
        selectedLink = ""
    }

    private fun selectItem(textView: MaterialTextView, url: String){
        selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_invisible, 0, 0, 0)
        selectedItem = textView
        selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_checked, 0, 0, 0)
        selectedLink = url
    }
}