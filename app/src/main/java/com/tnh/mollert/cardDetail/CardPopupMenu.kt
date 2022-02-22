package com.tnh.mollert.cardDetail

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.tnh.mollert.R

class CardPopupMenu(
    context: Context,
    anchor: View
): PopupMenu(context,anchor) {

    init {
        inflate(R.menu.card_detail_menu)
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        super.setOnMenuItemClickListener(listener)
    }
}