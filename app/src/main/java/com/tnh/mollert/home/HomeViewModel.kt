package com.tnh.mollert.home

import com.google.firebase.firestore.ListenerRegistration
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirestoreHelper
): BaseViewModel() {
    private var workspaceListener: ListenerRegistration? = null
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

    fun registerWorkspace(){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            workspaceListener = firestore.listenDocument(
                firestore.getTrackingDoc(email),
                {
                    trace(it)
                }
            ){ snap->
                snap?.data?.let { map->
                    (map["workspaces"] as List<String>?)?.let { listRef->
                        if(listRef.isNotEmpty()){
                            // TODO: reload workspace data
                        }
                    }
                }
            }
        }
    }

    fun unregisterWorkspace(){
        workspaceListener?.remove()
        workspaceListener = null
    }

    override fun onCleared() {
        super.onCleared()
        unregisterWorkspace()
    }
}