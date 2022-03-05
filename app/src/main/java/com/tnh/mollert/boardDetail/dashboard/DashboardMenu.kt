package com.tnh.mollert.boardDetail.dashboard

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.tnh.mollert.R

class DashboardMenu(
    context: Context,
    anchor: View
): PopupMenu(context, anchor) {
    init {
        inflate(R.menu.dashboard_menu)
    }
}