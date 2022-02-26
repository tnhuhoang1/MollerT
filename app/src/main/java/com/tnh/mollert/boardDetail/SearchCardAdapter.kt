package com.tnh.mollert.boardDetail

import com.tnh.mollert.R
import com.tnh.mollert.databinding.BoardDetailCardItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleClickableDataBindingListAdapter
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class SearchCardAdapter: SimpleClickableDataBindingListAdapter<Card, BoardDetailCardItemBinding>(R.layout.board_detail_card_item) {
    override fun bindViewHolder(
        holder: DataBindingViewHolder<BoardDetailCardItemBinding>,
        itemData: Card,
        position: Int
    ) {
        val card = getItem(position)
        holder.apply {
            binding.card = card
            if(card.cover.isEmpty().not()){
                binding.boardDetailCardItemCover.show()
            } else {
                binding.boardDetailCardItemCover.gone()
            }

            if(card.startDate != 0L && card.dueDate != 0L){
                binding.boardDetailCardItemDueData.text = "${card.startDate.getDate()} - ${card.dueDate.getDate()}"
                binding.boardDetailCardItemDueData.show()
            }else if(card.dueDate != 0L){
                binding.boardDetailCardItemDueData.text = card.dueDate.getDate()
                binding.boardDetailCardItemDueData.show()
            }else{
                binding.boardDetailCardItemDueData.gone()
            }
            binding.boardDetailCardItemAttachmentCount.gone()
            binding.executePendingBindings()
        }
    }
}