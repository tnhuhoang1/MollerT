package com.tnh.mollert.test

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.*
import com.tnh.mollert.R
import com.tnh.mollert.cardDetail.CardDetailFragmentArgs
import com.tnh.mollert.cardDetail.label.AddEditLabelFragmentArgs
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestLabelPickerDat : ActivityTestWithDataBindingIdlingResources() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTask = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    var mainCoroutine = MainCoroutineRule()
    private lateinit var dataSource: DataSource

    override fun setUp() {
        hiltRule.inject()
        super.setUp()
        FirebaseAuth.getInstance().signOut()
        runBlocking {
            coroutineScope {
                dataSource = DataSource.enableTest(ApplicationProvider.getApplicationContext())
                loginWithTestAccount()
            }
        }
    }

    override fun tearDown() {
        super.tearDown()
        dataSource.close()
        FirebaseAuth.getInstance().signOut()
    }

    private val member = Member("dat@test.com","dat")
    private val workspace = Workspace("board_id","board_name")
    private val memberWorkSpace = MemberWorkspaceRel(member.email,workspace.workspaceId)
    private val board = Board("board_id","board_name",workspace.workspaceId)
    private val memberBoardRel = MemberBoardRel(member.email, board.boardId, MemberBoardRel.ROLE_OWNER)
    private val list = List("list_id","list_name",board.boardId, List.STATUS_ACTIVE,0)
    private val card = Card("card_id","card_name",0,list.listId,0L, Card.STATUS_ACTIVE)
    private val label = Label("label_id","Green","label_name",board.boardId)
    private fun initArgs(): Bundle {
        mainCoroutine.runBlockingTest {
            dataSource.memberDao.insertOne(member)
            dataSource.memberWorkspaceDao.insertOne(memberWorkSpace)
            dataSource.workspaceDao.insertOne(workspace)
            dataSource.boardDao.insertOne(board)
            dataSource.listDao.insertOne(list)
            dataSource.cardDao.insertOne(card)
            dataSource.memberBoardDao.insertOne(memberBoardRel)
        }
        return CardDetailFragmentArgs.Builder(workspace.workspaceId,board.boardId,list.listId,card.cardId).build().toBundle()
    }

    @Test
    fun test_all_label() {
        val args = initArgs()
        add_label_no_color()
        add_label_success(args)
        edit_label(args)
        delete_label(args)
    }

    private fun add_label_no_color() = mainCoroutine.runBlockingTest {
        val args = AddEditLabelFragmentArgs.Builder(workspace.workspaceId,board.boardId,list.listId,card.cardId,"","").build().toBundle()
        launchTestFragmentWithContainer(R.id.addEditLabelFragment,args) {
            onView(withId(R.id.add_label_name)).perform(typeText("label_name"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Please select color")).check(matches(isDisplayed()))
        }
    }

    private fun add_label_success(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            onView(withId(R.id.label_picker_root)).perform(clickItemWithId(R.id.label_picker_new_label))
            onView(withId(R.id.add_label_name)).perform(typeText("label_name"))
            onView(withText("Green")).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            dataSource.labelDao.insertOne(label)
            onView(withText("Add label successfully")).check(matches(isDisplayed()))
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            onView(withText(label.labelName)).check(matches(isDisplayed()))
        }
    }

    private fun edit_label(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            dataSource.labelDao.insertOne(Label("label_id","Red","Dat day",board.boardId))
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            onView(withRecyclerView(R.id.label_item_recycler).atPosition(0)).perform(clickItemWithId(R.id.label_item_edit))
            onView(withId(R.id.add_label_name)).perform(clearText(),typeText(label.labelName))
            closeSoftKeyboard()
            onView(withText("Green")).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            dataSource.labelDao.insertOne(label)
            onView(withText("Edit label successfully")).check(matches(isDisplayed()))
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            onView(withText(label.labelName)).check(matches(isDisplayed()))
        }
    }

    private fun delete_label(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            dataSource.labelDao.insertOne(label)
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            onView(withRecyclerView(R.id.label_item_recycler).atPosition(0)).perform(clickItemWithId(R.id.label_item_edit))
            onView(withText("Click to remove")).perform(click())
            onView(withText("DELETE")).perform(click())
            onView(withText("Label deleted")).check(matches(isDisplayed()))
            onView(withText(card.cardName)).check(matches(isDisplayed()))
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            sleep(500)
        }
    }
}