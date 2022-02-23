package com.tnh.mollert.cardDetail.label

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.tnh.mollert.R
import com.tnh.mollert.databinding.LabelItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny

class LabelAdapter: SimpleDataBindingListAdapter<Label, LabelItemBinding>(R.layout.label_item) {
    var onEditClicked: ((labelId: String, labelName: String)-> Unit)? = null
    var onLabelClicked: ((labelId: String)-> Unit)? = null

    private val selectedSet = mutableSetOf<Label>()
    private var originalList: List<Label> = listOf()
    /**
     * set selected list before submit data
     *
     */
    fun setSelectedList(list: List<Label>){
        originalList = list
        selectedSet.clear()
        selectedSet.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelectedList() = selectedSet.toList()
    fun getOriginalList() = originalList


    override fun onBindViewHolder(holder: DataBindingViewHolder<LabelItemBinding>, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            labelItemName.text = item.labelName
            root.background = ColorDrawable(Color.parseColor(item.labelColor))
            setSelectedIfExists(this, item)
            labelItemEdit.setOnClickListener {
                onEditClicked?.invoke(item.labelId, item.labelName)
            }
            labelItemName.setOnClickListener {
                toggleItem(this, item)
            }
        }
    }

    fun setSelectedIfExists(binding: LabelItemBinding, label: Label){
        binding.labelItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.vd_invisible,
            0,
            0,
            0
        )
        selectedSet.find { it == label }?.let {
            binding.labelItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.vd_tick_white,
                0,
                0,
                0
            )
        }
    }

    private fun toggleItem(binding: LabelItemBinding, label: Label){
        selectedSet.find { it == label }?.let {
            selectedSet.remove(label)
            binding.labelItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.vd_invisible,
                0,
                0,
                0
            )
        }?: kotlin.run {
            selectedSet.add(label)
            binding.labelItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.vd_tick_white,
                0,
                0,
                0
            )
        }
    }


}