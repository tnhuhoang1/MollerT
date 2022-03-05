package com.tnh.mollert.boardDetail.dashboard

import com.tnh.mollert.R
import com.tnh.mollert.databinding.DashboardItemBinding
import com.tnh.mollert.utils.dpToPx
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class DashboardAdapter: SimpleDataBindingListAdapter<DashboardItem, DashboardItemBinding>(R.layout.dashboard_item) {
    var max: Int = 1
    private var maxHeightPixels = 1

    fun setMaxHeightPixels(height: Int){
        maxHeightPixels = height - 40.dpToPx
    }

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<DashboardItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            val height = (item.value / max.toFloat() * maxHeightPixels).toInt()
            dashboardItemValue.setBackgroundColor(item.color)
            dashboardItemValue.height = height
            dashboardItemTitle.text = item.title
        }
    }
}