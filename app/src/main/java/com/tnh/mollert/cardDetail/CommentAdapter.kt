package com.tnh.mollert.cardDetail

import com.tnh.mollert.R
import com.tnh.mollert.databinding.CommentItemBinding
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.utils.bindImageUri
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class CommentAdapter: SimpleDataBindingListAdapter<MemberAndActivity, CommentItemBinding>(R.layout.comment_item) {
    var onLongClicked: (Activity) -> Unit = {}
    override fun onBindViewHolder(
        holder: DataBindingViewHolder<CommentItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            commentItemAvatar.bindImageUri(item.member.avatar)
            commentItemName.text = item.member.name
            commentItemTextComment.text = MessageMaker.getCommentContent(item.activity.message)
            commentItemTimestamp.text = item.activity.timestamp.getDate("dd/MM/yy HH:mm")
            root.setOnLongClickListener {
                onLongClicked(item.activity)
                true
            }
        }
    }
}