package com.tnh.mollert.boardDetail

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.IdRes
import com.tnh.mollert.R
import com.tnh.mollert.datasource.local.model.Board

class BoardPopupMenu(context: Context, anchorView: View): PopupMenu(context, anchorView) {
    private var visibilityItem: MenuItem? = null
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

    fun setNewVisibility(@IdRes id: Int){
        visibilityItem?.setIcon(R.drawable.vd_invisible)
        visibilityItem = menu?.findItem(id)
        visibilityItem?.setIcon(R.drawable.vd_tick)
    }


    fun setVisibility(visible: String) {
        menu?.findItem(R.id.board_detail_menu_visibility)?.isVisible = true
        val privateItem = menu?.findItem(R.id.board_detail_menu_private)
        val workspaceItem = menu?.findItem(R.id.board_detail_menu_workspace)
        val publicItem = menu?.findItem(R.id.board_detail_menu_public)
        val inviteItem = menu?.findItem(R.id.board_detail_menu_invite)

        privateItem?.isVisible = true
        workspaceItem?.isVisible = true
        publicItem?.isVisible = true

        when(visible){
            Board.VISIBILITY_PRIVATE->{
                visibilityItem = privateItem
                privateItem?.setIcon(R.drawable.vd_tick)
                workspaceItem?.setIcon(R.drawable.vd_invisible)
                publicItem?.setIcon(R.drawable.vd_invisible)
            }
            Board.VISIBILITY_WORKSPACE->{
                visibilityItem = workspaceItem
                privateItem?.setIcon(R.drawable.vd_invisible)
                workspaceItem?.setIcon(R.drawable.vd_tick)
                publicItem?.setIcon(R.drawable.vd_invisible)
                inviteItem?.isVisible = true
            }
            Board.VISIBILITY_PUBLIC->{
                visibilityItem = publicItem
                privateItem?.setIcon(R.drawable.vd_invisible)
                workspaceItem?.setIcon(R.drawable.vd_invisible)
                publicItem?.setIcon(R.drawable.vd_tick)
                inviteItem?.isVisible = true
            }
        }
    }
}