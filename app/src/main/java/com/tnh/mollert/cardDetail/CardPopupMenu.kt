package com.tnh.mollert.cardDetail

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.tnh.mollert.R
import com.tnh.mollert.datasource.local.compound.CardWithMembers
import com.tnh.mollert.datasource.local.model.Card

class CardPopupMenu(
    context: Context,
    anchor: View
): PopupMenu(context,anchor) {

    private var cardWithMembers: CardWithMembers? = null

    fun setCardWithMembers(cardWithMembers: CardWithMembers){
        this.cardWithMembers = cardWithMembers
        this.cardWithMembers?.let { c ->
            menu.findItem(R.id.card_detail_menu_archived)?.let { menuItem ->
                if(c.card.cardStatus == Card.STATUS_ACTIVE){
                    menuItem.title = "Archive card"
                    menu.findItem(R.id.card_detail_menu_delete)?.let {
                        it.isVisible = false
                    }
                }else{
                    menuItem.title = "Activate card"
                    menu.findItem(R.id.card_detail_menu_delete)?.let {
                        it.isVisible = true
                    }
                }
            }
        }
    }

    init {
        inflate(R.menu.card_detail_menu)
    }

    fun getCardStatus(): String{
        return cardWithMembers?.card?.cardStatus ?: Card.STATUS_ACTIVE
    }

    fun isMemberInCard(email: String): Boolean{
        cardWithMembers?.let { cardWithMembers ->
            cardWithMembers.members.forEach {
                if(it.email == email){
                    return true
                }
            }
        }
        return false
    }

    fun showWithMember(email: String){
        cardWithMembers?.let { cardWithMembers ->
            val item = menu.findItem(R.id.card_detail_menu_join_card)
            item.title = "Join card"
            item.isVisible = true
            cardWithMembers.members.forEach {
                if(it.email == email){
                    item?.let { menuItem->
                        menuItem.title = "Leave card"
                        menuItem.isVisible = true
                        return@forEach
                    }
                }
            }
        }
        show()
    }

}