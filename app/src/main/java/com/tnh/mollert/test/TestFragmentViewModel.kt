package com.tnh.mollert.test

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TestFragmentViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {


}