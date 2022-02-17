package com.tnh.mollert.cardDetail.label

import android.graphics.Color
import com.google.android.material.textview.MaterialTextView
import com.tnh.mollert.R
import com.tnh.mollert.databinding.ColorItemBinding
import com.tnh.mollert.utils.LabelPreset
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.trace

class LabelColorAdapter(
    val onItemClicked: (String, String)-> Unit
): SimpleDataBindingListAdapter<LabelPreset.ColorPreset, ColorItemBinding>(R.layout.color_item) {
    private var lastSelected: MaterialTextView? = null
    var selectedPosition: Int? = null
    private set


    override fun onBindViewHolder(holder: DataBindingViewHolder<ColorItemBinding>, position: Int) {
        holder.binding.apply {
            try {
                val item = getItem(position)
                val color = Color.parseColor(item.color)
                colorItemName.setBackgroundColor(color)
                colorItemName.text = item.name
                colorItemName.setOnClickListener {
                    selectItem(colorItemName, position, item)
                }
            }catch (e: Exception){
                trace(e)
            }
        }
    }

    fun selectItem(tv: MaterialTextView, position: Int, color: LabelPreset.ColorPreset){
        lastSelected?.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
        lastSelected = tv
        selectedPosition = position
        lastSelected?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vd_tick_white, 0)
        onItemClicked(color.name, color.color)
    }
}