package com.tnh.mollert.cardDetail

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AttachmentDetailItemBinding
import com.tnh.mollert.datasource.local.model.Attachment
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class AttachmentAdapter: SimpleDataBindingListAdapter<Attachment, AttachmentDetailItemBinding>(R.layout.attachment_detail_item) {

    var onItemClicked: (Attachment) -> Unit = {}

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<AttachmentDetailItemBinding>,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            when(item.type){
                Attachment.TYPE_IMAGE->{
                    attachmentDetailItemName.text = "Image attachment"
                    attachmentDetailItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_image, 0, 0, 0)
                    attachmentDetailItemImage.bindImageUri(item.linkRemote)
                    attachmentDetailItemImage.show()
                }
                Attachment.TYPE_LINK->{
                    val spannable = SpannableString(item.linkRemote)
                    attachmentDetailItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_link, 0, 0, 0)
                    spannable.setSpan(UnderlineSpan(), 0, item.linkRemote.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    attachmentDetailItemName.text = spannable
                    attachmentDetailItemImage.gone()
                }
            }
            root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}