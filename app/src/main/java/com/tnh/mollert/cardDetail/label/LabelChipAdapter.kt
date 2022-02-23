package com.tnh.mollert.cardDetail.label

import android.content.res.ColorStateList
import android.graphics.Color
import com.tnh.mollert.R
import com.tnh.mollert.databinding.LabelChipItemBinding
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class LabelChipAdapter: SimpleDataBindingListAdapter<Label, LabelChipItemBinding>(R.layout.label_chip_item) {
    override fun onBindViewHolder(
        holder: DataBindingViewHolder<LabelChipItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            labelChipItemChip.text = item.labelName
            labelChipItemChip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(item.labelColor))
//            labelChipItemChip.isCloseIconVisible = true
//            labelChipItemChip.setOnCloseIconClickListener {
//                "clicked".logAny()
//            }
        }
    }
}