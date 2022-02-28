package com.tnh.mollert.home

import com.google.android.material.textview.MaterialTextView
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BackgroudItemBinding
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

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
            if(selectedItem == null){
                selectItem(backgroundItemCheckBox, item)
            }
            backgroundItemBackground.bindImageUri(item)
            root.setOnClickListener {
                selectItem(backgroundItemCheckBox, item)
                onBackgroundSelected()
            }
        }
    }

    fun setSelectedUri(uri: String){
        selectedLink = uri
    }


    fun clearSelected(){
        selectedLink = null
        selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_invisible, 0, 0, 0)
        selectedItem = null
    }

    private fun selectItem(textView: MaterialTextView, url: String){
        selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_invisible, 0, 0, 0)
        selectedItem = textView
        selectedItem?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_checked, 0, 0, 0)
        selectedLink = url
    }
}