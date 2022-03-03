package com.tnh.mollert.notification

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.tnh.mollert.R
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace

class NotificationMenu(
    context: Context,
    anchorView: View
){
    val popupMenu = PopupMenu(context, anchorView)
    init {
        popupMenu.inflate(R.menu.notification_menu)
    }

    fun show(){
        popupMenu.show()
    }
}