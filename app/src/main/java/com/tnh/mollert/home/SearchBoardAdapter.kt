package com.tnh.mollert.home

import com.tnh.mollert.R
import com.tnh.mollert.databinding.SearchBoardItemBinding
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleClickableDataBindingListAdapter

class SearchBoardAdapter: SimpleClickableDataBindingListAdapter<Board, SearchBoardItemBinding>(R.layout.search_board_item) {
    override fun bindViewHolder(
        holder: DataBindingViewHolder<SearchBoardItemBinding>,
        itemData: Board,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            searchBoardItemBackground.bindImageUri(item.background)
            searchBoardItemName.text = item.boardName
        }
    }
}