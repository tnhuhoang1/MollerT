package com.tnh.mollert.boardDetail.dashboard

import android.graphics.Color
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DataSource,
) : BaseViewModel(){


    suspend fun getCardPerList(boardId: String): Dashboard{
        return withContext(Dispatchers.Default){
            val db = Dashboard()
            val listItem = mutableListOf<DashboardItem>()
            db.title = "Cards per list"
            repository.appDao.getListWithCardsByBoardId(boardId).filter { it.cards.isNotEmpty() }.forEach{ listWithCards ->
                if(listWithCards.cards.size > db.max){
                    db.max = listWithCards.cards.size
                }
                listItem.add(
                    DashboardItem(
                        listWithCards.list.listName,
                        Color.parseColor("#48546c"),
                        listWithCards.cards.size
                    )
                )
            }
            db.listItem = listItem
            db.max = (db.max / 4 + 1) * 4
            db
        }
    }

    suspend fun getCardPerMember(boardId: String): Dashboard{
        return withContext(Dispatchers.Default){
            val db = Dashboard()
            val listItem = mutableListOf<DashboardItem>()
            db.title = "Cards per member"
            val members = repository.memberBoardDao.getRelsByBoardId(boardId)
            members.forEach { memberBoardRel ->
                val rels = repository.memberCarDao.getRelByEmailInBoard(memberBoardRel.email, memberBoardRel.boardId)
                if(rels.size > db.max){
                    db.max = rels.size
                }
                val member = repository.memberDao.getByEmail(memberBoardRel.email)
                member?.let {
                    listItem.add(
                        DashboardItem(
                            member.name,
                            Color.parseColor("#48546c"),
                            rels.size
                        )
                    )
                }
            }
            db.listItem = listItem
            db.max = (db.max / 4 + 1) * 4
            db
        }
    }

    suspend fun getCardPerLabel(boardId: String): Dashboard{
        return withContext(Dispatchers.Default){
            val db = Dashboard()
            val listItem = mutableListOf<DashboardItem>()
            db.title = "Cards per label"
            val labels = repository.labelDao.getLabelsWithBoardIdNoFlow(boardId)
            val noNameItem = DashboardItem(
                "No name",
                Color.parseColor("#e0e4e4"),
                0
            )
            labels.forEach { label->
                val rels = repository.cardLabelDao.getRelByLabelId(label.labelId)
                rels.logAny()
                if(rels.size > db.max){
                    db.max = rels.size
                }
                if(label.labelName.isNotEmpty()){
                    listItem.add(
                        DashboardItem(
                            label.labelName,
                            Color.parseColor(label.labelColor),
                            rels.size
                        )
                    )
                }else{
                    noNameItem.value++
                }
            }
            listItem.add(noNameItem)
            db.listItem = listItem
            db.max = (db.max / 4 + 1) * 4
            db
        }
    }

    suspend fun getCardPerDueDate(boardId: String): Dashboard{
        return withContext(Dispatchers.Default){
            val db = Dashboard()
            db.title = "Cards per due date"
            val listItem = mutableListOf<DashboardItem>()
            val noDueItem = DashboardItem(
                "No due date",
                Color.parseColor("#e0e4e4"),
                0
            )
            val overDueItem = DashboardItem(
                "Overdue",
                Color.parseColor("#f05c44"),
                0
            )
            val dueSoonItem = DashboardItem(
                "Due soon",
                Color.parseColor("#f8cc04"),
                0
            )
            val completeItem = DashboardItem(
                "Complete",
                Color.parseColor("#68bc4c"),
                0
            )
            val currentTime = System.currentTimeMillis()
            repository.cardDao.getCardsInBoardSortedByDueDate(boardId).forEach { card->
                if(card.checked){
                    completeItem.value++
                }else{
                    if(card.dueDate == 0L){
                        noDueItem.value++
                    }else if(currentTime > card.dueDate){
                        overDueItem.value++
                    }else{
                        dueSoonItem.value++
                    }
                }
            }
            listItem.add(completeItem)
            listItem.add(dueSoonItem)
            listItem.add(overDueItem)
            listItem.add(noDueItem)
            db.max = listItem.maxOf { it.value }
            db.max = (db.max / 4 + 1) * 4
            db.listItem = listItem
            db
        }
    }
}