package com.tnh.mollert.cardDetail

import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentReference
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.CardWithLabels
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.mollert.datasource.remote.model.RemoteLabelRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardDetailFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val reposiory: AppRepository,
): BaseViewModel() {

    private var email = UserWrapper.getInstance()?.currentUserEmail ?: ""

    var card: LiveData<Card> = MutableLiveData(null)
    private set

    var labels: LiveData<List<Label>> = MutableLiveData(null)
        private set

    var cardWithLabels: LiveData<CardWithLabels> = MutableLiveData(null)

    suspend fun getCardWithLabels(cardId: String): CardWithLabels{
        return reposiory.appDao.getCardWithLabels(cardId)
    }

    private var cardDoc: DocumentReference? = null

    fun setCardDoc(workspaceId: String, boardId: String, listId: String, cardId: String){
        cardDoc = firestore.getCardDoc(workspaceId, boardId, listId, cardId)
    }

    fun getCardById(cardId: String){
        card = reposiory.cardDao.getCardById(cardId).asLiveData()
        cardWithLabels = reposiory.appDao.getCardWithLabelsFlow(cardId).asLiveData()
    }

    fun getLabelById(boardId: String){
        labels = reposiory.labelDao.getLabelsWithBoardId(boardId).asLiveData()
    }


    fun applyLabelsToCard(workspaceId: String, boardId: String, newList: List<Label>){
        val list = newList.map { label->
            val labelDoc = firestore.getLabelDoc(workspaceId, boardId, label.labelId)
            RemoteLabelRef(
                labelDoc.path,
                label.labelId
            )
        }
        viewModelScope.launch {
            cardDoc?.let { doc->
                if(firestore.mergeDocument(doc, mapOf("labels" to list))){
                    if(firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "cards",
                            mapOf(
                                "what" to "label",
                                "ref" to doc.path
                            )
                    )){
                        postMessage("Set labels successfully")
                    }
                }
            }
        }
    }


    fun saveCardDescription(desc: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                    doc,
                    mapOf("desc" to desc)
                )){
                    if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "cards",
                            mapOf(
                                "what" to "info",
                                "ref" to doc.path
                            )
                    )){
                        postMessage("Change description successfully")
                    }
                }
            }

        }
    }
}