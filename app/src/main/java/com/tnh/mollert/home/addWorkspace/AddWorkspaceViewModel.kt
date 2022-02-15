package com.tnh.mollert.home.addWorkspace

import com.tnh.mollert.datasource.AppRepository
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddWorkspaceViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {
}