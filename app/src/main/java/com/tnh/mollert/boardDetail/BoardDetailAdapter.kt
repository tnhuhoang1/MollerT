package com.tnh.mollert.boardDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tnh.mollert.databinding.BoardDetailCardItemBinding
import com.tnh.mollert.databinding.BoardDetailListEndItemBinding
import com.tnh.mollert.databinding.BoardDetailListItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logE

class BoardDetailAdapter(
    private val getCardList: (String) -> ArrayList<Card>,
    private val addNewList:() -> Unit
) : ListAdapter<List, BoardDetailAdapter.BoardDetailViewHolder>(ListDiffUtil()) {
    inner class BoardDetailViewHolder(
        private val binding: BoardDetailListItemBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val boardCardAdapter = BoardDetailCardAdapter()
        fun bind(list: List) {
            boardCardAdapter.submitList(getCardList(list.listId))
            binding.boardDetailListItemRecyclerview.adapter = boardCardAdapter
            binding.boardDetailListItemToolbar.title = list.listName
        }
        fun bind(boardId: String) {
            (binding.boardDetailCardItem.layoutParams as RecyclerView.LayoutParams).height = RecyclerView.LayoutParams.WRAP_CONTENT
            binding.boardDetailListItemRecyclerview.visibility = View.GONE
            binding.boardDetailListItemToolbar.visibility = View.GONE
            binding.boardDetailFragmentNewListButton.text = "Add New List"
            binding.boardDetailFragmentNewListButton.setOnClickListener {
                addNewList()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardDetailViewHolder {
        return BoardDetailViewHolder(
            BoardDetailListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BoardDetailViewHolder, position: Int) {
        if (getItemViewType(position) == 1) holder.bind(getItem(position))
        else holder.bind(getItem(position).boardId)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).listId == "null") 0 else 1
    }

    class ListDiffUtil : DiffUtil.ItemCallback<List>() {
        override fun areItemsTheSame(oldItem: List, newItem: List) =
            (oldItem == newItem)

        override fun areContentsTheSame(oldItem: List, newItem: List) =
            (oldItem.listId == newItem.listId)
    }

    class BoardDetailCardAdapter(

    ) : ListAdapter<Card, BoardDetailCardAdapter.BoardDetailCardViewHolder>(ListDiffUtil()) {
        inner class BoardDetailCardViewHolder(private val binding: BoardDetailCardItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(card: Card) {
                Glide.with(binding.root).load(card.cover).into(binding.boardDetailCardItemImage)
                binding.card = card
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            BoardDetailCardViewHolder(
                BoardDetailCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: BoardDetailCardViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class ListDiffUtil : DiffUtil.ItemCallback<Card>() {
            override fun areItemsTheSame(oldItem: Card, newItem: Card) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: Card, newItem: Card) =
                oldItem.cardId == newItem.cardId

        }
    }
}