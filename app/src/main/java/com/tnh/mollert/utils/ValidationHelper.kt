package com.tnh.mollert.utils

class ValidationHelper {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String) : Boolean {
        if( password.isEmpty() || password.length < 7 ) {
            return false
        }
        return true
    }
    companion object {
        @Volatile
        private lateinit var instance: ValidationHelper

        fun getInstance(): ValidationHelper {
            if (::instance.isInitialized.not()) {
                instance = ValidationHelper()
            }
            return instance
        }
    }
}