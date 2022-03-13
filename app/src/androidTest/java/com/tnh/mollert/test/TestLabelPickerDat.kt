package com.tnh.mollert.test

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.ActivityTestWithDataBindingIdlingResources
import com.tnh.mollert.MainCoroutineRule
import com.tnh.mollert.R
import com.tnh.mollert.cardDetail.CardDetailFragmentArgs
import com.tnh.mollert.clickItemWithId
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
        //FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun test_all() {
        val args = initArgs()
        add_label_no_color(args)
    }

    val member = Member("dat@test.com","dat")
    val workspace = Workspace("board_id","board_name")
    val memberWorkSpace = MemberWorkspaceRel(member.email,workspace.workspaceId)
    val board = Board("board_id","board_name",workspace.workspaceId)
    val memberBoardRel = MemberBoardRel(member.email, board.boardId, MemberBoardRel.ROLE_OWNER)
    val list = List("list_id","list_name",board.boardId, List.STATUS_ACTIVE,0)
    val card = Card("card_id","card_name",0,list.listId,0L, Card.STATUS_ACTIVE)

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

    private fun add_label_no_color(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_label)).perform(click())
            onView(withId(R.id.label_picker_root)).perform(clickItemWithId(R.id.label_picker_new_label))
            onView(withId(R.id.add_label_name)).perform(typeText("Label_name"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Please select color")).check(matches(isDisplayed()))
        }
    }
}