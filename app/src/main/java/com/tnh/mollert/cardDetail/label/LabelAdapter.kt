package com.tnh.mollert.cardDetail.label

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.tnh.mollert.R
import com.tnh.mollert.databinding.LabelItemBinding
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny

class LabelAdapter: SimpleDataBindingListAdapter<Label, LabelItemBinding>(R.layout.label_item) {
    var onEditClicked: ((labelId: String, labelName: String)-> Unit)? = null
    var onLabelClicked: ((labelId: String)-> Unit)? = null

    override fun onBindViewHolder(holder: DataBindingViewHolder<LabelItemBinding>, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            labelItemName.text = item.labelName
            root.background = ColorDrawable(Color.parseColor(item.labelColor))
            labelItemEdit.setOnClickListener {
                onEditClicked?.invoke(item.labelId, item.labelName)
            }
            labelItemName.setOnClickListener {
                onLabelClicked?.invoke(item.labelId)
            }
        }
    }
}