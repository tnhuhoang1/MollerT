package com.tnh.mollert.boardDetail

import androidx.lifecycle.*
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.BoardWithLists
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteList
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardDetailFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val repository: AppRepository
): BaseViewModel() {

    var boardWithLists: LiveData<BoardWithLists> = MutableLiveData(null)

    fun getAllList(boardId: String){
        boardWithLists = repository.appDao.getBoardWithLists(boardId).asLiveData()
    }

    fun getConcatList(list: kotlin.collections.List<List>): kotlin.collections.List<List>{
        return list + List("null",
            "",
            "",
            "",
            list.size)
    }

    fun createNewList(workspaceId: String, boardId: String, listName: String){
        val listId = "${listName}_${System.currentTimeMillis()}"
        val listDoc = firestore.getListDoc(workspaceId, boardId, listId)
        boardWithLists.value?.lists?.size?.let { position->
            val remoteList = RemoteList(
                listId,
                listName,
                listDoc.path,
                boardId,
                position
            )
            viewModelScope.launch {
                if(firestore.mergeDocument(listDoc, remoteList)){
                    // notify other members
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(tracking, "lists", listDoc.path)
                        }
                    }
                }else{
                    //failed
                    postMessage("Failed")
                }
            }
        }
    }

    fun checkAndFetchList(prefManager: PrefManager, workspaceId: String, boardId: String){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            if(prefManager.getString("$email+$workspaceId").isEmpty()){
                "Fetching all board content".logAny()
            }
        }

    }



    fun getListTest() : ArrayList<List> {
        val a = arrayListOf<List>()
//        for (i in 1..5) {
//            a.add(
//                List(
//                    "list $i",
//                    "name $i",
//                    "board id $i",
//                    "good",
//                    5
//                )
//            )
//        }
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
