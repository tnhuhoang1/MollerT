package com.tnh.mollert.cardDetail

import com.tnh.mollert.datasource.AppRepository
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardDetailFragmentViewModel @Inject constructor(
    private val reposiory: AppRepository
): BaseViewModel() {

}