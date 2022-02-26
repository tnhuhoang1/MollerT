package com.tnh.mollert.calendar

import com.tnh.mollert.R
import com.tnh.mollert.databinding.DeadlineItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class DeadlineAdapter: SimpleDataBindingListAdapter<Card, DeadlineItemBinding>(R.layout.deadline_item) {
    var onItemClicked: (card: Card) -> Unit = {}

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<DeadlineItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            deadlineItemCardName.text = item.cardName
            deadlineItemTimestamp.text = "${item.startDate.getDate()} - ${item.dueDate.getDate()}"
            root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}