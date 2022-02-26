package com.tnh.mollert.notification

import com.tnh.mollert.R
import com.tnh.mollert.databinding.NotificationItemBinding
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.utils.bindImageUri
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny

class NotificationAdapter: SimpleDataBindingListAdapter<MemberAndActivity, NotificationItemBinding>(R.layout.notification_item) {

    var onItemClicked: ((memberAndActivity: MemberAndActivity)-> Unit)? = null

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<NotificationItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            notificationItemMessage.text = MessageMaker.getDecodedSpannable(item.activity.message)
            notificationItemAvatar.bindImageUri(item.member.avatar)
            notificationItemTimestamp.text = item.activity.timestamp.getDate("dd/MM/yyyy HH:mm")
            root.setOnClickListener {
                onItemClicked?.invoke(item)
            }
        }

    }
}