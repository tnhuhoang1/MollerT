package com.tnh.mollert.cardDetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AddAttachmentBinding
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class AddAttachmentDialog(context: Context, container: ViewGroup?): BottomSheetDialog(context) {
    val binding: AddAttachmentBinding = AddAttachmentBinding.inflate(LayoutInflater.from(context), container, false)

    var onCloseClicked: () -> Unit = {}
    var onImageClicked: () -> Unit = {}
    var onLinkClicked: () -> Unit = {}

    init {

        binding.addAttachmentToolbar.twoActionToolbarStartIcon.show()
        binding.addAttachmentToolbar.twoActionToolbarStartIcon.setOnClickListener {
            onCloseClicked()
        }
        binding.addAttachmentToolbar.twoActionToolbarTitle.text = "Add attachment"

        binding.addAttachmentImage.attachmentItemImage.setBackgroundResource(R.drawable.vd_image)
        binding.addAttachmentImage.attachmentItemText.text = "Image"
        binding.addAttachmentImage.root.setOnClickListener{
            onImageClicked()
        }

        binding.addAttachmentFile.root.gone()
        binding.addAttachmentFile.attachmentItemImage.setBackgroundResource(R.drawable.app_icon)
        binding.addAttachmentFile.attachmentItemText.text = "File"

        binding.addAttachmentLink.attachmentItemImage.setBackgroundResource(R.drawable.vd_link)
        binding.addAttachmentLink.attachmentItemText.text = "Link"
        binding.addAttachmentLink.root.setOnClickListener {
            onLinkClicked()
        }

        setContentView(binding.root)
    }
}