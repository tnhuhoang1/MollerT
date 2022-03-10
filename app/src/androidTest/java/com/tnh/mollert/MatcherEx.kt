package com.tnh.mollert

import android.view.View
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun getElementFromMatcherAtPosition(matcher: Matcher<View>, position: Int): BaseMatcher<View> {
    return object : BaseMatcher<View>() {
        var counter = 0
        override fun describeTo(description: Description?) {
            description?.appendText("Element at hierarchy position $position")
        }

        override fun matches(actual: Any?): Boolean {
            actual?.let {
                if(matcher.matches(actual)){
                    if(counter == position){
                        counter++
                        return true
                    }
                    counter++
                }
                return false
            }
            return false
        }
    }
}