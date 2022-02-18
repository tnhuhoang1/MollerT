package com.tnh.mollert.notification

import com.tnh.mollert.R
import com.tnh.mollert.databinding.NotificationItemBinding
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class NotificationAdapter: SimpleDataBindingListAdapter<MemberAndActivity, NotificationItemBinding>(R.layout.notification_item) {

    var onItemClicked: ((memberAndActivity: MemberAndActivity)-> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<NotificationItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            notificationItemTextView.text = MessageMaker.getDecodedSpannable(item.activity.message)
//            Glide.with(notificationItemImgView).load(item.)
            root.setOnClickListener {
                onItemClicked?.invoke(item)
            }
        }

    }
}