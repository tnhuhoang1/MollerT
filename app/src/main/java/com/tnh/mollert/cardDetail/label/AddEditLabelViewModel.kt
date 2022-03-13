package com.tnh.mollert.cardDetail.label

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.remote.model.RemoteLabel
import com.tnh.mollert.utils.FirestoreAction
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.LabelPreset
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditLabelViewModel @Inject constructor(
    private val firestore: FirestoreAction,
    private val repository: DataSource
): BaseViewModel() {
    var mode = MODE_CREATE
    val allLabels = repository.labelDao.getAll().asLiveData()

    fun addLabel(
        workspaceId: String,
        boardId: String,
        colorPreset: LabelPreset.ColorPreset,
        name: String
    ){
        val labelId = "${boardId}_${name}"
        val labelDoc = firestore.getLabelDoc(workspaceId, boardId, labelId)
        viewModelScope.launch {
            val remoteModel = RemoteLabel(labelId, name, colorPreset.color, boardId)
            if(firestore.addDocument(
                labelDoc,
                remoteModel
            )){
                notifyMember(boardId, labelDoc.path, "Add label successfully")
                dispatchClickEvent(EVENT_ADD_OK)
            }
        }
    }

    fun editLabel(
        workspaceId: String,
        boardId: String,
        labelId: String,
        colorPreset: LabelPreset.ColorPreset,
        name: String
    ){
        val labelDoc = firestore.getLabelDoc(workspaceId, boardId, labelId)
        viewModelScope.launch {
            val remoteModel = RemoteLabel(labelId, name, colorPreset.color, boardId)
            if(firestore.addDocument(
                    labelDoc,
                    remoteModel
                )){
                notifyMember(boardId, labelDoc.path, "Edit label successfully")
                dispatchClickEvent(EVENT_EDIT_OK)
            }
        }
    }

    private suspend fun notifyMember(boardId: String, ref: String, message: String){
        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
            listMember.forEach { mem->
                val tracking = firestore.getTrackingDoc(mem.email)
                if(firestore.insertToArrayField(tracking, "labels", ref)){
                    postMessage(message)
                }
            }
        }
    }

    fun deleteLabel(workspaceId: String, boardId: String, labelId: String) {
        val labelDoc = firestore.getLabelDoc(workspaceId, boardId, labelId)
        viewModelScope.launch {
            if(firestore.deleteDocument(labelDoc)){
                repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                    listMember.forEach { mem->
                        val tracking = firestore.getTrackingDoc(mem.email)
                        if(firestore.insertToArrayField(tracking, "delLabels", labelDoc.path)){
                            repository.labelDao.getLabelById(labelId)?.let { label->
                                repository.labelDao.deleteOne(label)
                            }
                            postMessage("Label deleted")
                            dispatchClickEvent(EVENT_DELETE_OK)
                        }
                    }
                }
            }else{
                postMessage("Failed")
            }
        }
    }

    companion object{
        const val EVENT_BACK = "back"
        const val EVENT_OK = "ok"
        const val EVENT_DELETE_OK = "delete_ok"
        const val EVENT_EDIT_OK = "edit_ok"
        const val EVENT_ADD_OK = "add_ok"
        const val MODE_CREATE = "create"
        const val MODE_EDIT = "edit"
    }
}