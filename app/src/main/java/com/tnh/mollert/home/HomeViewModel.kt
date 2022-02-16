package com.tnh.mollert.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.MemberWithWorkspaces
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.datasource.remote.model.RemoteWorkspace
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper
): BaseViewModel() {
    private var workspaceListener: ListenerRegistration? = null

    var memberWithWorkspaces = MutableLiveData<MemberWithWorkspaces>(null).toLiveData()

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
            memberWithWorkspaces = repository.appDao.getMemberWithWorkspaces(email).asLiveData()

            workspaceListener = firestore.listenDocument(
                firestore.getTrackingDoc(email),
                {
                    trace(it)
                }
            ){ snap->
                snap?.data?.let { map->
                    (map["workspaces"] as List<String>?)?.let { listRef->
                        if(listRef.isNotEmpty()){
                            listRef.forEach {
                                viewModelScope.launch {
                                    saveWorkspaceFromRemote(email, it)
                                }
                            }
                        }else{
                            viewModelScope.launch {
                                repository.workspaceDao.countOne()?.let {
                                    if(it == 0){
                                        reloadDataFromRemote(email)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveWorkspaceFromRemote(email: String, ref: String){
        firestore.simpleGetDocumentModel<RemoteWorkspace>(
            firestore.getDocRef(ref)
        )?.let {
            it.toModel()?.let { model->
                repository.workspaceDao.insertOne(model)
                repository.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, model.workspaceId))
            }
        }
    }


    private suspend fun reloadDataFromRemote(email: String) {
        "Reloading all workspaces from remote".logAny()
        firestore.simpleGetDocumentModel<RemoteMember>(
            firestore.getMemberDoc(email)
        )?.let { rm->
            rm.workspaces?.forEach { ws->
                ws.ref?.let { ref->
                    saveWorkspaceFromRemote(email, ref)
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