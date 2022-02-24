package com.tnh.mollert.cardDetail

import com.tnh.mollert.R
import com.tnh.mollert.databinding.MemberAvatarItemBinding
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class MemberAdapter: SimpleDataBindingListAdapter<Member, MemberAvatarItemBinding>(R.layout.member_avatar_item) {
    override fun onBindViewHolder(
        holder: DataBindingViewHolder<MemberAvatarItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            memberAvatarItemAvatar.bindImageUri(item.avatar)
        }
    }
}