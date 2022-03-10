package com.tnh.mollert.profile

import android.content.ContentResolver
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.safeResume
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

    private val email = UserWrapper.getInstance()?.currentUserEmail ?: ""
    val member: LiveData<Member?> = repository.local.memberDao.getByEmailFlow(email).asLiveData()

    private val _process = MutableLiveData<Boolean>(false)
    val process = _process.toLiveData()

    fun showProcess(){
        _process.postValue(true)
    }

    fun hideProcess(){
        _process.postValue(false)
    }

    val memberName = Transformations.map(member){ m-> m?.name ?: "" }
    val memberAvatar = Transformations.map(member){m-> m?.avatar ?: "" }
    val memberEmail = Transformations.map(member){ m-> m?.email ?: ""}
    val bio = Transformations.map(member){m-> m?.biography ?: ""}

    private val _editAvatar = MutableLiveData("")
    val editAvatar = _editAvatar.toLiveData()

    fun setImageUri(uriString: String){
        _editAvatar.postValue(uriString)
    }

    suspend fun getBoardByEmail(email: String) = repository.getBoardByEmail(email)

    suspend fun saveMemberInfoToFirestore(name: String, bio: String, contentResolver: ContentResolver) {
        if(email.isNotEmpty()){
            val avatarUri = editAvatar.value ?: ""
            val avatar: String = repository.uploadAvatar(email, avatarUri, contentResolver)
            repository.saveMemberInfoToFirestore(email, name, avatar, bio){
                postMessage("Change profile successfully")
            }
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        memberEmail.value?.let { email->
            if(repository.changePassword(email, oldPassword, newPassword).isEmpty()){
                return true
            }
        }
        return false
    }

    private suspend fun notifyInfoChanged(email: String) {
        repository.notifyInfoChanged(email)
    }

    companion object {
        const val EVENT_LOGOUT_CLICKED = "logout_clicked"
        const val EVENT_PROFILE_IMAGE_CLICKED = "profile_image_clicked"
        const val EVENT_SUCCESS = "success"
        const val CHANGE_PASSWORD_SUCCESS = "change_pass_success"
        const val CHANGE_PASSWORD_FAILURE = "change_pass_failure"
    }
}