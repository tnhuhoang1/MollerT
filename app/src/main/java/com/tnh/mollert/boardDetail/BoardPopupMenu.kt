package com.tnh.mollert.boardDetail

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.tnh.mollert.R

class BoardPopupMenu(context: Context, anchorView: View): PopupMenu(context, anchorView) {
    init {
        menuInflater.inflate(R.menu.board_detail_menu, menu)
    }
}