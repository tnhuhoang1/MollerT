package com.tnh.mollert.boardDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BoardDetailCardItemBinding
import com.tnh.mollert.databinding.BoardDetailListItemBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class BoardDetailAdapter(
    private val addNewList:() -> Unit
) : ListAdapter<List, BoardDetailAdapter.BoardDetailViewHolder>(ListDiffUtil()) {
    var listCards: kotlin.collections.List<kotlin.collections.List<Card>> = listOf()
    var onNewCardClicked: ((listId: String) -> Unit)? = null
    var onDeleteListClicked: ((listId: String) -> Unit)? = null
    var onCardClicked: ((l: String, c: String) -> Unit)? = null

    fun resubmitCardList(viewHolder: BoardDetailViewHolder, position: Int){
        listCards.getOrNull(position)?.let {
            viewHolder.boardCardAdapter.submitList(it)
        }
    }

    inner class BoardDetailViewHolder(
        private val binding: BoardDetailListItemBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        val boardCardAdapter = BoardDetailCardAdapter()


        fun bind(list: List, position: Int) {
            binding.boardDetailListItemRecyclerview.adapter = boardCardAdapter
            listCards.getOrNull(position)?.let {
                boardCardAdapter.submitList(it)
            }
            boardCardAdapter.onCardClicked = onCardClicked
            binding.boardDetailListItemToolbar.title = list.listName
            binding.boardDetailFragmentNewListButton.setOnClickListener {
                onNewCardClicked?.invoke(list.listId)
            }
            binding.boardDetailListItemToolbar.setOnMenuItemClickListener { menuItem->
                when(menuItem.itemId){
                    R.id.board_detail_item_menu_add->{
                        onNewCardClicked?.invoke(list.listId)
                    }
                    R.id.board_detail_item_menu_delete->{
                        onDeleteListClicked?.invoke(list.listId)
                    }
                }
                true
            }
        }
        fun bind() {
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
        if (getItemViewType(position) == 1) holder.bind(getItem(position), position)
        else holder.bind()
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

    class BoardDetailCardAdapter() : ListAdapter<Card, BoardDetailCardAdapter.BoardDetailCardViewHolder>(ListDiffUtil()) {
        var onCardClicked: ((l: String, c: String) -> Unit)? = null

        inner class BoardDetailCardViewHolder(
            private val binding: BoardDetailCardItemBinding
        ): RecyclerView.ViewHolder(binding.root) {

            fun bind(card: Card) {
                binding.card = card
                if(card.startDate != 0L && card.dueDate != 0L){
                    binding.boardDetailCardItemDueData.text = "${card.startDate.getDate()} - ${card.dueDate.getDate()}"
                    binding.boardDetailCardItemDueData.show()
                }else if(card.dueDate != 0L){
                    binding.boardDetailCardItemDueData.text = card.dueDate.getDate()
                    binding.boardDetailCardItemDueData.show()
                }else{
                    binding.boardDetailCardItemDueData.gone()
                }
                binding.root.setOnClickListener {
                    onCardClicked?.invoke(card.listId, card.cardId)
                }
                binding.boardDetailCardItemAttachmentCount.gone()
                binding.executePendingBindings()
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