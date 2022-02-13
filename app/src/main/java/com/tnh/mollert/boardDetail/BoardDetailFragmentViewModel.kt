package com.tnh.mollert.boardDetail

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoardDetailFragmentViewModel @Inject constructor(
    private val reposiory: AppRepository
): ViewModel() {
}