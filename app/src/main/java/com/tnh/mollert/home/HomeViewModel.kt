package com.tnh.mollert.home

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(): BaseViewModel() {
    fun getBoardTest() : ArrayList<Board> {
        var a = arrayListOf<Board>()
        for (i in 1..20) {
            a.add(Board("id $i","haha $i","hello $i", background = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/800px-Image_created_with_a_mobile_phone.png"))
        }
        return a
    }

    fun getWorkSpaceTest() : ArrayList<Workspace> {
        var a = arrayListOf<Workspace>()
        for (i in 1..20) {
            a.add(Workspace(
                "id $i",
                "w name $i",
                "haha $i",
            ))
        }
        return a
    }


}