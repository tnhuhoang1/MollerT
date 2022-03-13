package com.tnh.mollert

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.core.internal.deps.dagger.internal.Preconditions
import androidx.test.espresso.matcher.ViewMatchers
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.utils.DataBindingIdlingResource
import com.tnh.mollert.utils.monitorActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.After
import org.junit.Before
import kotlin.coroutines.resume

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

//    inline fun <reified T : Fragment> launchFragmentInHiltContainer(
//        fragmentArgs: Bundle? = null,
//        @StyleRes themeResId: Int = androidx.fragment.testing.R.style.FragmentScenarioEmptyFragmentActivityTheme,
//        crossinline action: Fragment.() -> Unit = {}
//    ) {
//        val startActivityIntent = Intent.makeMainActivity(
//            ComponentName(
//                ApplicationProvider.getApplicationContext(),
//                MainActivity::class.java
//            )
//        ).putExtra(
//            "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
//            themeResId
//        )
//
//        ActivityScenario.launch<MainActivity>(startActivityIntent).onActivity { activity ->
//            val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
//                Preconditions.checkNotNull(T::class.java.classLoader),
//                T::class.java.name
//            )
//            fragment.arguments = fragmentArgs
//            activity.supportFragmentManager
//                .beginTransaction()
//                .add(android.R.id.content, fragment, "")
//                .commitNow()
//            fragment.action()
//        }
//    }

    inline fun launchTestFragmentWithContainer(
        @IdRes idNav: Int,
        args: Bundle? = bundleOf(),
        action: ActivityScenario<MainActivity>.() -> Unit = {}
    ){
        launchTestFragment(idNav, args).apply {
            action(this)
            this.close()
        }
    }

    fun launchTestFragment(
        @IdRes idNav: Int,
        args: Bundle? = bundleOf()
    ): ActivityScenario<MainActivity>{
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        monitorActivityScenario(activityScenario)
        activityScenario.onActivity { activity->
            activity.navController.navigate(idNav, args)
        }
        return activityScenario
    }

    suspend fun loginWithTestAccount() = suspendCancellableCoroutine<Unit> { cont->
        FirebaseAuth.getInstance().signInWithEmailAndPassword("h@1.1", "1234567")
        .addOnSuccessListener {
            cont.resume(Unit)
        }.addOnFailureListener {
            cont.resume(Unit)
        }
    }
}