package com.tnh.mollert.notification

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.tnh.mollert.R

class NotificationMenu(
    context: Context,
    anchorView: View
): PopupMenu(context, anchorView) {
    init {
        inflate(R.menu.notification_menu)
    }
}