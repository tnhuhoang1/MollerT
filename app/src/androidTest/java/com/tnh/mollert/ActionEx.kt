package com.tnh.mollert

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.tnh.tnhlibrary.logAny
import org.hamcrest.Matcher

fun clickItemWithId(id: Int): ViewAction {
    return object : ViewAction{
        override fun getConstraints(): Matcher<View>? {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Click on a child view with specified id: $id"
        }

        override fun perform(uiController: UiController?, view: View?) {
            val v = view?.findViewById<View>(id)
            v?.performClick()
        }
    }
}

fun requestFocus(): ViewAction {
    return object : ViewAction{
        override fun getConstraints(): Matcher<View>? {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Request focus on a view"
        }

        override fun perform(uiController: UiController?, view: View?) {
            view?.requestFocus()
        }
    }
}

fun clickItemWithTextInViewGroup(text: String): ViewAction {
    return object : ViewAction{
        override fun getConstraints(): Matcher<View>? {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Click on a child view with text: $text"
        }

        override fun perform(uiController: UiController?, view: View?) {
            if(view is ViewGroup){
                view.children.forEach {
                    perform(uiController, it)
                }
            }else{
                if(view is TextView){
                    if(view.text == text){
                        view.performClick()
                    }
                }
            }
        }
    }
}

/**
 * click to the container of the text view
 */
fun clickItemWithIdAndChildText(id: Int, text: String): ViewAction {
    return object : ViewAction{
        override fun getConstraints(): Matcher<View>? {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Click on a child view with text: $text"
        }

        override fun perform(uiController: UiController?, view: View?) {
            val v = view?.findViewById<View>(id)
            checkText(v)
        }

        private fun checkText(view: View?): Boolean{
            if(view is ViewGroup){
                view.children.forEach {
                    if(checkText(it)){
                        view.logAny()
                        view.performClick()
                    }
                }
            }else{
                if(view is TextView){
                    if(view.text == text){
                        return true
                    }
                }
            }
            return false
        }
    }
}


fun clickItemWithIdAndChildTextInRecyclerView(id: Int, text: String): ViewAction {
    return object : ViewAction{
        override fun getConstraints(): Matcher<View>? {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Click on a child view with text: $text"
        }

        override fun perform(uiController: UiController?, view: View?) {
            val v = view?.findViewById<View>(id)
            if(v is RecyclerView){
                v.children.forEach {
                    if(checkText(it)){
                        it.performClick()
                        return@forEach
                    }
                }
            }
        }

        private fun checkText(view: View?): Boolean{
            if(view is ViewGroup){
                view.children.forEach {
                    if(checkText(it)){
                        return true
                    }
                }
            }else{
                if(view is TextView){
                    if(view.text == text){
                        return true
                    }
                }
            }
            return false
        }
    }
}