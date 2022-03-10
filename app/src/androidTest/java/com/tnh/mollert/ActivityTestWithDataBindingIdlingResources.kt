package com.tnh.mollert

import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import com.tnh.mollert.utils.DataBindingIdlingResource
import com.tnh.mollert.utils.monitorActivity
import org.junit.After
import org.junit.Before

abstract class ActivityTestWithDataBindingIdlingResources {
    protected val dataBindingIdlingResource = DataBindingIdlingResource()
    @Before
    open fun setUp(){
        registerDataBindingIdlingResources()
    }

    open fun registerDataBindingIdlingResources(){
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    fun unregisterDataBindingIdlingResource(){
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    fun <T: FragmentActivity> monitorActivityScenario(activityScenario: ActivityScenario<T>){
        dataBindingIdlingResource.monitorActivity(activityScenario)
    }

    @After
    open fun tearDown(){
        unregisterDataBindingIdlingResource()
    }

    fun sleep(milliseconds: Long){
        Thread.sleep(milliseconds)
    }

}