package com.tnh.mollert.profile

import android.content.ContentResolver
import androidx.lifecycle.*
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper,
    private val storage: StorageHelper
): BaseViewModel() {

    private val email = UserWrapper.getInstance()?.currentUserEmail ?: ""
    val member: LiveData<Member?> = repository.memberDao.getByEmailFlow(email).asLiveData()

    val memberName = Transformations.map(member){ m-> m?.name ?: "" }
    val memberAvatar = Transformations.map(member){m-> m?.avatar ?: "" }
    val memberEmail = Transformations.map(member){ m-> m?.email ?: ""}
    val bio = Transformations.map(member){m-> m?.biography ?: ""}

    // Sử dụng shared view model sẽ khiến mất thông tin của người dùng nếu người dùng xoay màn hình
//    private val _editName = MutableLiveData("")
    private val _editAvatar = MutableLiveData("")
//    private val _editBio = MutableLiveData("")


    val editAvatar = _editAvatar.toLiveData()

    fun setImageUri(uriString: String){
        _editAvatar.postValue(uriString)
    }

    fun saveMemberInfoToFirestore(name: String, bio: String, contentResolver: ContentResolver) {
        if(email.isNotEmpty()){

            val remoteMember =  RemoteMember(email, name, "", bio).info()

            viewModelScope.launch {
                val doc = firestore.getMemberDoc(email)
                if(firestore.mergeDocument(doc, remoteMember)){
                    // succeeded
                    if(notifyInfoChanged(email)){
                        postMessage("Edit profile successfully")
                        dispatchClickEvent(EVENT_SUCCESS)
                    }
                }else{
                    // failed
                }
            }
        }
    }

    private suspend fun notifyInfoChanged(email: String): Boolean{
        return firestore.insertToArrayField(firestore.getTrackingDoc(email), "info", "info")
    }


    companion object {
        const val EVENT_LOGOUT_CLICKED = "logout_clicked"
        const val EVENT_PROFILE_IMAGE_CLICKED = "profile_image_clicked"
        const val EVENT_SUCCESS = "success"
    }
}