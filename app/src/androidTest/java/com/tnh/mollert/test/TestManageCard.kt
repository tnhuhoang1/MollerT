package com.tnh.mollert.test

import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.ActivityTestWithDataBindingIdlingResources
import com.tnh.mollert.MainCoroutineRule
import com.tnh.mollert.R
import com.tnh.mollert.boardDetail.BoardDetailFragmentArgs
import com.tnh.mollert.cardDetail.CardDetailFragmentArgs
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import org.hamcrest.Matcher


@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestManageCard : ActivityTestWithDataBindingIdlingResources() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTask = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    var mainCoroutine = MainCoroutineRule()
    private lateinit var dataSource: DataSource
    val member = Member("h@1.1","Linh","","")
    override fun setUp() {
        hiltRule.inject()
        super.setUp()
        runBlocking {
            coroutineScope {
                dataSource = DataSource.enableTest(ApplicationProvider.getApplicationContext())
                dataSource.memberDao.insertOne(member)
                loginWithTestAccount()
            }
        }
    }

    override fun tearDown() {
        super.tearDown()
        dataSource.close()
        FirebaseAuth.getInstance().signOut()
    }

    fun clickOptionMenuInToolbar(id: Int): ViewAction {
        return object : ViewAction{
            override fun getConstraints(): Matcher<View>? {
                return isDisplayed()
            }

            override fun getDescription(): String {
                return "Click option menu in toolbar"
            }

            override fun perform(uiController: UiController?, view: View?) {
                val v = view?.findViewById<View>(id)
                if(v is androidx.appcompat.widget.Toolbar){
                    v.showOverflowMenu()
                }
            }
        }
    }
    val workspace = Workspace("workspace_id","workspace_name")
    val board = Board("board_id","board_name","workspace_id")
    val memberWorkspaceRel = MemberWorkspaceRel(member.email,workspace.workspaceId)
    val list = List("list_id","meo meo","board_id",List.STATUS_ACTIVE,0)
    val card = Card("card_id","cardName_1",0,"list_id")

    val boardArgs = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId,board.boardName).build().toBundle()
    val cardArgs = CardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, list.listId, card.cardId).build().toBundle()
    fun setupData() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.boardDao.insertOne(board)
        dataSource.memberWorkspaceDao.insertOne(memberWorkspaceRel)
        dataSource.listDao.insertOne(list)
    }

    @Test
    fun runTest(){
        setupData()
        check_add_card_success_in_board_detail_fragment(boardArgs)
        check_add_card_with_empty_name_in_board_detail_fragment(boardArgs)
        check_change_name_card_success_in_board_detail_fragment(boardArgs)
        check_change_name_card_fail_in_board_detail_fragment(cardArgs)
        check_open_archived_card_with_no_card_in_board_detail_fragment(boardArgs)
        check_open_archived_card_in_board_detail_fragment(boardArgs)
        check_open_archived_card_success_in_board_detail_fragment(cardArgs)
        check_archive_card_in_board_detail_fragment(cardArgs)
    }


    fun check_add_card_success_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        //man hinh can doi so thi phai lam the nay
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withText("Add New Card")).perform(click())
            onView(withHint("Card name")).perform(typeText(card.cardName))
            onView(withText("OK")).perform(click())
            dataSource.cardDao.insertOne(card)
            sleep(200)
            onView(withText(card.cardName)).check(matches(isDisplayed()))
        }
    }

    fun check_add_card_with_empty_name_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withText("Add New Card")).perform(click())
            onView(withText("OK")).perform(click())
            onView(withText("Card name can't be empty")).check(matches(isDisplayed()))
        }
    }

    fun check_change_name_card_success_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withText("cardName_1")).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Change name")).perform(click())
            onView(withHint("New name")).perform(typeText("meo"))
            onView(withText("OK")).perform(click())
            val card = Card("card_id","meo",0,"list_id")
            dataSource.cardDao.insertOne(card)
            onView(withText("meo")).check(matches(isDisplayed()))
        }
    }

    fun check_change_name_card_fail_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Change name")).perform(click())
            onView(withText("OK")).perform(click())
            onView(withText("Card name can't be empty")).check(matches(isDisplayed()))
        }
    }

    fun check_open_archived_card_with_no_card_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Archived cards")).perform(click())
            onView(withText("There is no archived card")).check(matches(isDisplayed()))
        }
    }

    fun check_open_archived_card_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.cardDao.insertOne(card.copy(cardStatus = Card.STATUS_ARCHIVED))
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Archived cards")).perform(click())
            onView(withText(card.cardName)).check(matches(isDisplayed()))
        }
    }


    fun check_open_archived_card_success_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Activate card")).perform(click())
            dataSource.cardDao.insertOne(card)
            sleep(500)
            onView(withText("Card activated")).check(matches(isDisplayed()))
        }
    }

    fun check_archive_card_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Archive card")).perform(click())
            dataSource.cardDao.insertOne(card.copy(cardStatus = Card.STATUS_ARCHIVED))
            sleep(100)
            onView(withText("Card archived")).check(matches(isDisplayed()))
        }
    }


}