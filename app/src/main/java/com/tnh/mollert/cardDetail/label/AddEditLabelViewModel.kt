package com.tnh.mollert.cardDetail.label

import androidx.lifecycle.asLiveData
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.utils.LabelPreset
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

@HiltViewModel
class AddEditLabelViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

    val allLabels = repository.labelDao.getAll().asLiveData()

    fun addLabel(colorPreset: LabelPreset.ColorPreset){

    }

    companion object{
        const val EVENT_BACK = "back"
        const val EVENT_OK = "ok"
    }
}