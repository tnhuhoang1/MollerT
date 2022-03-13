package com.tnh.mollert.calendar

import android.graphics.Color
import com.tnh.mollert.R
import com.tnh.mollert.databinding.DayItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class DayAdapter: SimpleDataBindingListAdapter<DayWithDeadline, DayItemBinding>(R.layout.day_item) {
    var onItemClicked: (card: Card) -> Unit = {}
    override fun onBindViewHolder(holder: DataBindingViewHolder<DayItemBinding>, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            dayItemDayName.text = item.day
            dayItemDayNumber.text = item.dayNumber
            if(item.dayNumber == System.currentTimeMillis().getDate("dd")){
                dayItemDayName.setTextColor(Color.parseColor("#3D49CB"))
                dayItemCircle.setBackgroundColor(Color.parseColor("#3D49CB"))
            }else if(item.dayNumber > System.currentTimeMillis().getDate("dd")){
                dayItemDayName.setTextColor(Color.parseColor("#313A46"))
                dayItemCircle.setBackgroundColor(Color.parseColor("#313A46"))
            }else{
                dayItemDayName.setTextColor(Color.parseColor("#DF4146"))
                dayItemCircle.setBackgroundColor(Color.parseColor("#DF4146"))
            }
            val adapter = DeadlineAdapter()
            dayItemRecycler.adapter = adapter
            adapter.onItemClicked = onItemClicked
            adapter.submitList(item.listCard)
        }
    }
}