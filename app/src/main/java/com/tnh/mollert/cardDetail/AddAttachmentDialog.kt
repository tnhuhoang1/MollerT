package com.tnh.mollert.cardDetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AddAttachmentBinding

class AddAttachmentDialog(context: Context, container: ViewGroup?): BottomSheetDialog(context) {
    val binding: AddAttachmentBinding = AddAttachmentBinding.inflate(LayoutInflater.from(context), container, false)
    init {
        binding.addAttachmentImage.attachmentItemImage.setBackgroundResource(R.drawable.app_icon)
        binding.addAttachmentImage.attachmentItemText.text = "Image"

        binding.addAttachmentFile.attachmentItemImage.setBackgroundResource(R.drawable.app_icon)
        binding.addAttachmentFile.attachmentItemText.text = "File"

        binding.addAttachmentLink.attachmentItemImage.setBackgroundResource(R.drawable.app_icon)
        binding.addAttachmentLink.attachmentItemText.text = "Link"

        setContentView(binding.root)
    }
}