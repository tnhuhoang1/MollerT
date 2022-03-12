package com.tnh.mollert.home.addWorkspace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.textview.MaterialTextView
import com.tnh.mollert.R
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef
import com.tnh.mollert.datasource.remote.model.RemoteWorkspace
import com.tnh.mollert.datasource.remote.model.RemoteWorkspaceRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWorkspaceViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {
    private val wsTypes = listOf(
        Workspace.TYPE_EDUCATION,
        Workspace.TYPE_ENGINEERING_IT,
        Workspace.TYPE_HUMAN_RESOURCES,
        Workspace.TYPE_MARKETING,
        Workspace.TYPE_SALES_CRM,
        Workspace.TYPE_SMALL_BUSINESS,
        Workspace.TYPE_OTHER
    )

    val listTv = mutableListOf<MaterialTextView>()

    var lastSelected: Int? = null
        private set

    private var _selectedPosition = MutableLiveData<Int>(null)
    val selectedPosition = _selectedPosition.toLiveData()

    private var _isExpanded = MutableLiveData(false)
    val isExpanded = _isExpanded.toLiveData()

    private var _isLoading = MutableLiveData(false)
    val isLoading = _isLoading.toLiveData()

    fun toggleType(){
        _isExpanded.value = _isExpanded.value?.not()
    }

    fun showProgress(){
        _isLoading.postValue(true)
    }

    fun hideProgress(){
        _isLoading.postValue(false)
    }


    /**
     * this is not an atomic transaction
     *
     */
    fun addWorkspace(name: String, type: String, desc: String){
        if(name.isBlank() || type.isEmpty()){
            postMessage("Please fill out the required fields")
        }else{
            viewModelScope.launch {
                showProgress()
                repository.addWorkspace(
                    name,
                    type,
                    desc,
                    {
                        onCreateFailed("Something went wrong")
                    },
                    {
                        onCreateSuccess("Create workspace successfully")
                    }

                )
                hideProgress()
            }
        }

    }

    private fun onCreateFailed(mess: String){
        postMessage(mess)
        hideProgress()
        dispatchClickEvent(EVENT_FAILED)
    }

    private fun onCreateSuccess(mess: String){
        postMessage(mess)
        hideProgress()
        dispatchClickEvent(EVENT_SUCCESS)
    }


    fun changeSelectItem(pos: Int){
        lastSelected = _selectedPosition.value
        _selectedPosition.value = pos
    }

    fun changeIcon(pos: Int){
        lastSelected?.let {
            listTv[it].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_circle_default, 0, 0, 0)
        }
        listTv[pos].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_checked, 0, 0, 0)
    }


    fun setupListType(block: (typeName: String) -> MaterialTextView){
        listTv.clear()
        wsTypes.forEach {
            listTv.add(block(it))
        }
    }

    companion object{
        const val EVENT_ADD_CLICKED = "clicked"
        const val EVENT_BACK_CLICKED = "back"
        const val EVENT_SUCCESS = "success"
        const val EVENT_FAILED = "failed"
    }
}