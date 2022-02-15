package com.tnh.mollert.forgotpassword

import com.tnh.tnhlibrary.viewModel.BaseViewModel
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(

) : BaseViewModel() {

    companion object {
        const val EVENT_SEND_PASSWORD_CLICKED = "send_password_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_SIGN_UP_CLICKED = "sign_up_clicked"
    }
}