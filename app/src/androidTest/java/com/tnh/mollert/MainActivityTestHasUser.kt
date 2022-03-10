package com.tnh.mollert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.mollert.utils.LabelPreset
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTestHasUser : ActivityTestWithDataBindingIdlingResources() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTask = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    var mainCoroutine = MainCoroutineRule()

    private lateinit var dataSource: DataSource
    private val member = Member(
        "h@1.1","h@1.1", "", ""
    )

    override fun setUp() {
        hiltRule.inject()
        super.setUp()
        runBlocking {
            coroutineScope {
                dataSource = DataSource.enableTest(ApplicationProvider.getApplicationContext())
                dataSource.memberDao.insertOne(
                    member
                )
                suspendCancellableCoroutine<Unit> { cont->
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        "h@1.1",
                        "1234567"
                    ).addOnSuccessListener {
                        cont.resume(Unit)
                    }.addOnFailureListener {
                        cont.resume(Unit)
                    }
                }
            }
        }
    }

    override fun tearDown() {
        super.tearDown()
        dataSource.close()
        FirebaseAuth.getInstance().signOut()
    }

    private val workspaceName = "My new workspace"
    private val workspaceId = "${member.email}_${workspaceName}"

    private val boardName = "New board"
    var board = Board(
        "new_board_id",
        boardName,
        workspaceId,
        "",
        "https://baoquocte.vn/stores/news_dataimages/dieulinh/012020/29/15/nhung-buc-anh-dep-tuyet-voi-ve-tinh-ban.jpg"
    )

    private val list1 = List("list_id_1", "list1", board.boardId, List.STATUS_ACTIVE, 0)
    private val list2 = List("list_id_2", "list2", board.boardId, List.STATUS_ACTIVE, 0)
    private val list3 = List("list_id_3", "list3", board.boardId, List.STATUS_ACTIVE, 0)
    private val list4 = List("list_id_4", "list4", board.boardId, List.STATUS_ACTIVE, 0)
    private val list5 = List("list_id_5", "list5", board.boardId, List.STATUS_ACTIVE, 0)

    private val card11 = Card("card_1_1", "card_1_1", 0, list1.listId)
    private val card12 = Card("card_1_2", "card_1_2", 0, list1.listId)
    private val card13 = Card("card_1_3", "card_1_3", 0, list1.listId)
    private val card14 = Card("card_1_4", "card_1_4", 0, list1.listId)
    private val card15 = Card("card_1_5", "card_1_5", 0, list1.listId)
    private val card21 = Card("card_2_1", "card_2_1", 0, list2.listId)
    private val card22 = Card("card_2_2", "card_2_2", 0, list2.listId)
    private val card23 = Card("card_2_3", "card_2_3", 0, list2.listId)

    @Test
    fun testAll() = mainCoroutine.runBlockingTest{
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        monitorActivityScenario(activityScenario)

        //go to profile
        onView(withId(R.id.profileFragment)).perform(click())
        onView(allOf(withId(R.id.profile_fragment_email), withText(member.email))).check(matches(isDisplayed()))

        // go to edit profile
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())

        // change profile
        val newName = "Nhu Hoang"
        val newBio = "This is my bio"
        onView(allOf(withId(R.id.edit_profile_fragment_name))).perform(clearText())
        onView(allOf(withId(R.id.edit_profile_fragment_name))).perform(typeText(newName))
        onView(allOf(withId(R.id.edit_profile_fragment_bio))).perform(scrollTo())
        onView(allOf(withId(R.id.edit_profile_fragment_bio))).perform(clearText())

        onView(allOf(withId(R.id.edit_profile_fragment_bio))).perform(typeText(newBio))
        Espresso.closeSoftKeyboard()

        onView(allOf(withId(R.id.two_action_toolbar_title))).perform(scrollTo())
        dataSource.memberDao.updateOne(member.copy(name = newName, biography = newBio))
        onView(allOf(withId(R.id.two_action_toolbar_end_icon))).perform(click())
        sleep(3000)

        // go to home
        onView(withId(R.id.homeFragment)).perform(click())

        // create workspace


        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withId(R.id.add_workspace_fragment_name)).perform(typeText(workspaceName))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.add_workspace_fragment_type_container)).perform(click())
        onView(withText("Education")).perform(click())
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(allOf(withId(R.id.workspace_item_name), withText(workspaceName))).check(matches(isDisplayed()))
        sleep(200)

        // create board
        onView(withId(R.id.workspace_item_new)).perform(click())
        onView(withId(R.id.create_board_dialog_name)).perform(typeText(boardName))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_board_dialog_public)).perform(click())
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())

        val memberBoardRel = MemberBoardRel(member.email, board.boardId, MemberBoardRel.ROLE_OWNER)
        val labels = LabelPreset.getPresetLabelList(board.boardId).map { it.toLabel()!! }
        dataSource.labelDao.insertAll(*labels.toTypedArray())
        dataSource.boardDao.insertOne(board)
        dataSource.memberBoardDao.insertOne(memberBoardRel)
        sleep(300)
        // go to board
        onView(allOf(withText(board.boardName))).perform(click())

        // change board name
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        val newBoardName = "New board name"
//        onView(withId(R.id.board_detail_menu_board_name)).perform(click())
        onView(withText("Board name")).perform(click())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(newBoardName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        board = board.copy(boardName = newBoardName)
        dataSource.boardDao.insertOne(board)
        sleep(300)

        // change board description
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        val boardDesc = "Welcome to this board!!!"
        onView(withText("Board description")).perform(click())
        onView(withId(R.id.board_desc_desc)).perform(typeText(boardDesc))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        board = board.copy(boardDesc = boardDesc)
        dataSource.boardDao.insertOne(board)
        sleep(300)

        // change background
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        val boardBackground = "https://img4.thuthuatphanmem.vn/uploads/2020/12/26/background-gradient-tim_052413985.jpg"
        onView(withText("Change background")).perform(click())
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        board = board.copy(background = boardBackground)
        dataSource.boardDao.insertOne(board)
        sleep(300)

        // insert lists
        addLists()
        sleep(500)
        // add cards
        addCards()
        sleep(500)
        // select card 1
        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickItemWithIdAndChildTextInRecyclerView(
                    R.id.board_detail_list_item_recyclerview,
                    card11.cardName
                )
            )
        )

        //Join card
        val memberCardRel = MemberCardRel(member.email, card11.cardId, MemberBoardRel.ROLE_MEMBER)
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Join card")).perform(click())
        dataSource.memberCarDao.insertOne(memberCardRel)
        sleep(200)

        // change card name
        val newCardName11 = "New_Card_1_1"
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Change name")).perform(click())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(newCardName11))
        onView(withText("OK")).perform(click())
        dataSource.cardDao.insertOne(card11.copy(cardName = newCardName11))
        sleep(100)
        // change card cover
        val card11Cover = "https://scr.vn/wp-content/uploads/2020/08/H%C3%ACnh-n%E1%BB%81n-background-vector-scaled.jpg"
//        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        dataSource.cardDao.insertOne(card11.copy(cardName = newCardName11, cover = card11Cover))
        sleep(100)

        // change description
        val card11Desc = "Hello world"
        onView(withId(R.id.card_detail_fragment_description)).perform(click())
        onView(withId(R.id.board_desc_desc)).perform(typeText(card11Desc))
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        dataSource.cardDao.insertOne(card11.copy(cardName = newCardName11, cover = card11Cover, cardDesc = card11Desc))

        // add attachment
        val attachment11 = Attachment(
            "attachment_1_1",
            "Link",
            Attachment.TYPE_LINK,
            "http://facebook.com",
            card11.cardId
        )
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Add attachment")).perform(click())
        onView(withText("Link")).perform(click())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(attachment11.linkRemote))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.attachmentDao.insertOne(attachment11)

        // add labels
        val label = Label(
            "label_11_id",
            LabelPreset.colorDataSet[0].color,
            "Custom label",
            board.boardId
        )
        val cardLabelRel1 = CardLabelRel(
            card11.cardId,
            labels[0].labelId
        )
        val cardLabelRel2 = CardLabelRel(
            card11.cardId,
            labels[1].labelId
        )
        val cardLabelRel3 = CardLabelRel(
            card11.cardId,
            labels[2].labelId
        )
        onView(withId((R.id.card_detail_fragment_label))).perform(click())
        onView(withText(labels[0].labelName)).perform(click())
        onView(withText(labels[1].labelName)).perform(click())
        onView(withText(labels[2].labelName)).perform(click())
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        dataSource.cardLabelDao.insertAll(cardLabelRel1, cardLabelRel2, cardLabelRel3)
        sleep(4000)

        // comment
        val commentContent1 = "Comment here 1"
        val comment1 = Activity(
            "comment_11",
            member.email,
            board.boardId,
            card11.cardId,
            MessageMaker.getEncodedComment(commentContent1),
            activityType = Activity.TYPE_COMMENT
        )
        val commentContent2 = "Comment here 2"
        val comment2 = Activity(
            "comment_12",
            member.email,
            board.boardId,
            card11.cardId,
            MessageMaker.getEncodedComment(commentContent2),
            activityType = Activity.TYPE_COMMENT
        )
        onView(withId(R.id.card_detail_fragment_comment_input)).perform(click(), typeText(commentContent1))
        onView(withId(R.id.card_detail_fragment_send)).perform(click())
        dataSource.activityDao.insertOne(comment1)
        sleep(300)
        onView(withId(R.id.card_detail_fragment_comment_input)).perform(click(), typeText(commentContent2))
        onView(withId(R.id.card_detail_fragment_send)).perform(click())
        dataSource.activityDao.insertOne(comment2)
        Espresso.closeSoftKeyboard()
        sleep(100)
        Espresso.pressBack()
        sleep(2500)

//        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
//            actionOnItemAtPosition<RecyclerView.ViewHolder>(
//                1,
//                clickItemWithIdAndChildTextInRecyclerView(
//                    R.id.board_detail_list_item_recyclerview,
//                    card21.cardName
//                )
//            )
//        )
//        val cardLabelRel21 = CardLabelRel(
//            card21.cardId,
//            labels[1].labelId
//        )
//        val cardLabelRel22 = CardLabelRel(
//            card21.cardId,
//            labels[2].labelId
//        )
//        val cardLabelRel23 = CardLabelRel(
//            card21.cardId,
//            labels[3].labelId
//        )
//
//        onView(withId((R.id.card_detail_fragment_label))).perform(scrollTo(), click())
//        onView(withText(labels[1].labelName)).perform(click())
//        onView(withText(labels[2].labelName)).perform(click())
//        onView(withText(labels[3].labelName)).perform(click())
//        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
//        dataSource.cardLabelDao.insertAll(cardLabelRel21, cardLabelRel22, cardLabelRel23)
//        sleep(100)
//        Espresso.pressBack()
//
        // navigate to dashboard
        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Dashboard")).perform(click())

        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Cards per due date")).perform(click())
        sleep(500)

        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Cards per member")).perform(click())
        sleep(500)


        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Cards per label")).perform(click())
        sleep(300)
        Espresso.pressBack()
        sleep(300)


        onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
        onView(withText("Close board")).perform(click())
        board = board.copy(boardStatus = Board.STATUS_CLOSED)
        dataSource.boardDao.insertOne(board)


//        // add work
//        val work1 = Work("work1_1", "Design", card11.cardId)
//        val work2 = Work("work1_2", "Work2", card11.cardId)
//        onView(withId(R.id.card_detail_fragment_checked_list)).perform(scrollTo(), click())
//        onView(withId(R.id.create_board_layout_name)).perform(typeText(work1.workName))
//        Espresso.closeSoftKeyboard()
//        onView(withText("OK")).perform(click())
//        dataSource.workDao.insertOne(work1)
//
//        onView(withId(R.id.card_detail_fragment_checked_list)).perform(click())
//        onView(withId(R.id.create_board_layout_name)).perform(typeText(work2.workName))
//        Espresso.closeSoftKeyboard()
//        onView(withText("OK")).perform(click())
//        dataSource.workDao.insertOne(work2)

        sleep(3000)
        activityScenario.close()
    }

    private suspend fun addLists(){
        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withId(R.id.create_board_layout_name)).perform(typeText(list1.listName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.listDao.insertOne(list1)
        sleep(600)

        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )

        onView(withId(R.id.create_board_layout_name)).perform(typeText(list2.listName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.listDao.insertOne(list2)
        sleep(600)

        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2, click()
            )
        )
        onView(withId(R.id.create_board_layout_name)).perform(typeText(list3.listName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.listDao.insertOne(list3)
        sleep(600)
    }

    private suspend fun addCards(){
        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickItemWithId(R.id.board_detail_fragment_new_list_button)
            )
        )
        sleep(100)
        onView(withId(R.id.create_board_layout_name)).perform(requestFocus())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(card11.cardName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.cardDao.insertOne(card11)
        sleep(300)

        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickItemWithId(R.id.board_detail_fragment_new_list_button)
            )
        )
        sleep(100)
        onView(withId(R.id.create_board_layout_name)).perform(requestFocus())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(card12.cardName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.cardDao.insertOne(card12)
        sleep(300)

        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickItemWithId(R.id.board_detail_fragment_new_list_button)
            )
        )
        sleep(100)
        onView(withId(R.id.create_board_layout_name)).perform(requestFocus())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(card13.cardName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.cardDao.insertOne(card13)
        sleep(300)

        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1,
                clickItemWithId(R.id.board_detail_fragment_new_list_button)
            )
        )
        sleep(100)
        onView(withId(R.id.create_board_layout_name)).perform(requestFocus())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(card21.cardName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.cardDao.insertOne(card21)
        sleep(300)

        onView(withId(R.id.board_detail_fragment_recyclerview)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1,
                clickItemWithId(R.id.board_detail_fragment_new_list_button)
            )
        )
        sleep(100)
        onView(withId(R.id.create_board_layout_name)).perform(requestFocus())
        onView(withId(R.id.create_board_layout_name)).perform(typeText(card22.cardName))
        Espresso.closeSoftKeyboard()
        onView(withText("OK")).perform(click())
        dataSource.cardDao.insertOne(card22)
        sleep(300)
    }
}

//inline fun <reified T : Fragment> launchFragmentInHiltContainer(
//    fragmentArgs: Bundle? = null,
//    @StyleRes themeResId: Int = androidx.fragment.testing.R.style.FragmentScenarioEmptyFragmentActivityTheme,
//    crossinline action: Fragment.() -> Unit = {}
//) {
//    val startActivityIntent = Intent.makeMainActivity(
//        ComponentName(
//            ApplicationProvider.getApplicationContext(),
//            Hilt_MainActivity::class.java
//        )
//    ).putExtra(
//        "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
//        themeResId
//    )
//
//    ActivityScenario.launch<Hilt_MainActivity>(startActivityIntent).onActivity { activity ->
//        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
//            Preconditions.checkNotNull(T::class.java.classLoader),
//            T::class.java.name
//        )
//        fragment.arguments = fragmentArgs
//        activity.supportFragmentManager
//            .beginTransaction()
//            .add(android.R.id.content, fragment, "")
//            .commitNow()
//
//        fragment.action()
//    }
//}