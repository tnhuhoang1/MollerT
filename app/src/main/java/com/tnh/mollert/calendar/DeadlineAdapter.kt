package com.tnh.mollert.calendar

import android.graphics.Color
import com.tnh.mollert.R
import com.tnh.mollert.databinding.DeadlineItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny

class DeadlineAdapter: SimpleDataBindingListAdapter<Card, DeadlineItemBinding>(R.layout.deadline_item) {
    var onItemClicked: (card: Card) -> Unit = {}

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<DeadlineItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            if(item.startDate.getDate("dd") >= System.currentTimeMillis().getDate("dd")){
                deadlineItemTimestamp.setTextColor(Color.BLACK)
                deadlineItemCardName.setTextColor(Color.BLACK)
                deadlineItemWidth.setBackgroundResource(R.drawable.blue_stroke_shape)
            }else{
                deadlineItemTimestamp.setTextColor(Color.parseColor("#DF4146"))
                deadlineItemCardName.setTextColor(Color.parseColor("#DF4146"))
                deadlineItemWidth.setBackgroundResource(R.drawable.red_stroke_shape)
            }
            deadlineItemCardName.text = item.cardName
            deadlineItemTimestamp.text = "${item.startDate.getDate()} - ${item.dueDate.getDate()}"
            root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}