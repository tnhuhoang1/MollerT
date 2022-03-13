package com.tnh.mollert.profile

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

    suspend fun saveMemberInfoToFirestore(name: String, bio: String, contentResolver: ContentResolver): Boolean{
        if(email.isNotEmpty()){
            var avatar: String = member.value?.avatar.toString()
            val editAvatar = editAvatar.value.toString()
            if(editAvatar.isNotEmpty() && editAvatar != avatar) {
                val newAvatar = repository.uploadAvatar(email, editAvatar, contentResolver)
                if(newAvatar.isNotEmpty()){
                    avatar = newAvatar
                }
            }
            return repository.saveMemberInfoToFirestore(email, name, avatar, bio)
        }
        return false
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): String {
        memberEmail.value?.let { email->
            return repository.changePassword(email, oldPassword, newPassword)
        }
        return ""
    }

    fun isValidInput(
        name: String,
        bio: String,
        oldPassword: String,
        newPassword: String,
    ): String {
        var event = "nothing"
        if(memberName.value.toString() != name || bio != this.bio.value.toString() || member.value?.avatar != editAvatar.value){
            event = "info"
        }
        if (name.isBlank()) {
            postMessage("Your name can't be empty")
            return ""
        }

        // If user change password
        if ((oldPassword.isBlank() && newPassword.isNotBlank()) ||
                oldPassword.isNotBlank() && newPassword.isBlank()) {
            postMessage("You need old password and new password to change password")
            return ""
        }else if(oldPassword.isNotBlank() && newPassword.isNotBlank()){
            if (ValidationHelper.getInstance().isValidPassword(oldPassword) && ValidationHelper.getInstance().isValidPassword(oldPassword)){
                if(event == "info"){
                    event = "info_password"
                }else{
                    event = "password"
                }
            }else{
                postMessage("Invalid password, please try again")
                return ""

            }
        }
        return event
    }

    suspend fun logout(prefManager: PrefManager){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            prefManager.putString("$email+sync+all", "")
            getBoardByEmail(email).forEach {
                prefManager.putString("$email+${it.boardId}", "")
            }
            FirebaseAuth.getInstance().signOut()
        }
    }

    companion object {
        const val EVENT_LOGOUT_CLICKED = "logout_clicked"
        const val EVENT_PROFILE_IMAGE_CLICKED = "profile_image_clicked"
        const val EVENT_SUCCESS = "success"
        const val CHANGE_PASSWORD_SUCCESS = "change_pass_success"
        const val CHANGE_PASSWORD_FAILURE = "change_pass_failure"
    }
}