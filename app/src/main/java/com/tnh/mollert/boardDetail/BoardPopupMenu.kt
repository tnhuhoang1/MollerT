package com.tnh.mollert.boardDetail

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.tnh.mollert.R

class BoardPopupMenu(context: Context, anchorView: View): PopupMenu(context, anchorView) {
    init {
        menuInflater.inflate(R.menu.board_detail_menu, menu)
    }

    fun setLeaveOrClose(isOwner: Boolean){
        if(isOwner){
            menu?.findItem(R.id.board_detail_menu_close)?.let {
                it.isVisible = true
            }
            menu?.findItem(R.id.board_detail_menu_leave)?.let {
                it.isVisible = false
            }
        }else{
            menu?.findItem(R.id.board_detail_menu_close)?.let {
                it.isVisible = false
            }
            menu?.findItem(R.id.board_detail_menu_leave)?.let {
                it.isVisible = true
            }
        }
    }
}