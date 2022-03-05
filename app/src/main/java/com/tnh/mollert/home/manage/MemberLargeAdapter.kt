package com.tnh.mollert.home.manage

import com.tnh.mollert.R
import com.tnh.mollert.databinding.MemberLargeBinding
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter

class MemberLargeAdapter: SimpleDataBindingListAdapter<Member, MemberLargeBinding>(R.layout.member_large) {
    var leader: String = ""
    var onLongClicked: (member: Member) -> Unit = {}
    var onClicked: (member: Member) -> Unit = {}

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<MemberLargeBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            memberLargeAvatar.bindImageUri(item.avatar)
            memberLargeName.text = item.name
            if(item.email == leader){
                memberLargeName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_key, 0, 0, 0)
            }else{
                memberLargeName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            }
            root.setOnClickListener { onClicked(item) }
            root.setOnLongClickListener {
                onLongClicked(item)
                true
            }
        }
    }
}