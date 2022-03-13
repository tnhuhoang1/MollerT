package com.tnh.mollert.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: DataSource,
): BaseViewModel() {
    var cardHasDate: LiveData<List<Card>> = MutableLiveData()
    private set
    var email: String = ""
    init {
        UserWrapper.getInstance()?.currentUserEmail?.let {
            var currentTime = System.currentTimeMillis()
            try {
                currentTime = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentTime.getDate("dd/MM/yyyy"))?.time ?: currentTime
            }catch (e: Exception){

            }
            email = it
            cardHasDate = repository.cardDao.getCardHasDateFlow(email).asLiveData()
        }
    }

}