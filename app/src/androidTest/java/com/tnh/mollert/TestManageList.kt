package com.tnh.mollert

import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.boardDetail.BoardDetailFragmentArgs
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
class TestManageList : ActivityTestWithDataBindingIdlingResources() {
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

    val list = List("list_id","meo meo","board_id",List.STATUS_ACTIVE,0)
    val workspace = Workspace("workspace_id","workspace_name")
    val board = Board("board_id","board_name","workspace_id")
    val memberWorkspaceRel = MemberWorkspaceRel(member.email,workspace.workspaceId)
    val card = Card("card_id","cardName_1",0,"list_id")
    // tao du lieu phai them cai nay vao


    //man hinh can doi so thi phai lam the nay
    val boardArgs = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId,board.boardName).build().toBundle()


    fun setupData() = mainCoroutine.runBlockingTest{
        dataSource.listDao.insertOne(list)
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.boardDao.insertOne(board)
        dataSource.memberWorkspaceDao.insertOne(memberWorkspaceRel)
    }

    @Test
    fun runTest(){
        setupData()
        check_add_List_success_in_board_detail_fragment(boardArgs)
        check_add_List_with_empty_name_in_board_detail_fragment(boardArgs)
        check_change_list_with_empty_name_in_board_detail_fragment(boardArgs)
        check_change_list_name_success_in_board_detail_fragment(boardArgs)
        check_archive_list_in_board_detail_fragment(boardArgs)
        check_open_archived_card_with_no_card_in_board_detail_fragment(boardArgs)
        check_open_archived_card_success_in_board_detail_fragment(boardArgs)
        check_delete_list_in_board_detail_fragment(boardArgs)
    }

    fun check_add_List_success_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.listDao.deleteOne(list)
        launchTestFragmentWithContainer(R.id.boardDetailFragment, args){
            onView(withId(R.id.board_detail_fragment_new_list_button)).perform(click())
            onView(withHint("List name")).perform(typeText("meo meo"))
            onView(withText("OK")).perform(click())
            dataSource.listDao.insertOne(list)
            sleep(100)
            onView(withText("meo meo")).check(matches(isDisplayed()))
            dataSource.listDao.deleteOne(list)
        }
    }

    fun check_add_List_with_empty_name_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.boardDetailFragment, args){
            onView(withId(R.id.board_detail_fragment_new_list_button)).perform(click())
            onView(withText("OK")).perform(click())
            onView(withText("List name can't be empty")).check(matches(isDisplayed()))
        }
    }

    fun check_change_list_name_success_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            dataSource.listDao.insertOne(list)
            onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickOptionMenuInToolbar(R.id.board_detail_list_item_toolbar)
                )
            )
            onView(withText("Change name")).perform(click())
            onView(withHint("List name")).perform(typeText("gau gau"))
            onView(withText("OK")).perform(click())
            dataSource.listDao.insertOne(list.copy(listName = "gau gau"))
            sleep(100)
            onView(withText("Change list name successfully")).check(matches(isDisplayed()))
            dataSource.listDao.insertOne(list)
        }
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

    fun check_change_list_with_empty_name_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.listDao.insertOne(list)
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickOptionMenuInToolbar(R.id.board_detail_list_item_toolbar)
                )
            )
            onView(withText("Change name")).perform(click())
            onView(withText("OK")).perform(click())
            sleep(100)
            onView(withText("List name can't be empty")).check(matches(isDisplayed()))
        }
    }

    fun check_archive_list_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.cardDao.insertOne(card)
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickOptionMenuInToolbar(R.id.board_detail_list_item_toolbar)
                )
            )
            sleep(100)
            onView(withText("Archive list")).perform(click())
            sleep(100)
            onView(withText("OK")).perform(click())
            dataSource.cardDao.insertOne(card.copy(cardStatus = Card.STATUS_ARCHIVED))
            sleep(100)
            onView(withText(card.cardName)).check(doesNotExist())
            onView(withText("Cards are archived")).check(matches(isDisplayed()))
        }
    }

    fun check_delete_list_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.cardDao.insertOne(card)
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickOptionMenuInToolbar(R.id.board_detail_list_item_toolbar)
                )
            )
            onView(withText("Delete list card")).perform(click())
            sleep(100)
            onView(withText("DO IT")).perform(click())
            sleep(100)
            onView(withText("Delete successfully")).check(matches(isDisplayed()))
        }
    }

    fun check_open_archived_card_with_no_card_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.cardDao.insertOne(card)
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            sleep(100)
            onView(withText("Archived cards")).perform(click())
            onView(withText("There is no archived card")).check(matches(isDisplayed()))
        }
    }

    fun check_open_archived_card_success_in_board_detail_fragment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.cardDao.insertOne(card.copy(cardStatus = Card.STATUS_ARCHIVED))
        launchTestFragmentWithContainer(R.id.boardDetailFragment,args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Archived cards")).perform(click())
            sleep(300)
            onView(withText(card.cardName)).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Activate card")).perform(click())
            sleep(100)
            onView(withText("Card activated")).check(matches(isDisplayed()))
        }
    }
}