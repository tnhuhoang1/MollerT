package com.tnh.mollert.boardDetail

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.model.Workspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoardDetailFragmentViewModel @Inject constructor(
    private val repository: AppRepository
): ViewModel() {
    fun getListTest() : ArrayList<List> {
        val a = arrayListOf<List>()
        for (i in 1..5) {
            a.add(
                List(
                    "list $i",
                    "name $i",
                    "board id $i",
                    "good",
                    5
                )
            )
        }
        a.add(
            List("null",
                "",
                "",
                "",
                0))
        return a
    }

    fun getCardTest() : ArrayList<Card> {
        val a = arrayListOf<Card>()
        for (i in 1..10) {
            a.add(
                Card(
                    i,
                    "card name $i",
                    i,
                    cover = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/800px-Image_created_with_a_mobile_phone.png"
                )
            )
        }
        return a
    }
}
