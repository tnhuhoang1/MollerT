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
import com.tnh.mollert.datasource.local.dao.CardDao
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

class BoardDetailAdapter(
    private val cardDao: CardDao,
    private val addNewList:() -> Unit
) : ListAdapter<List, BoardDetailAdapter.BoardDetailViewHolder>(ListDiffUtil()) {
    var onNewCardClicked: ((listId: String) -> Unit)? = null
    var onDeleteListClicked: ((listId: String) -> Unit)? = null
    var onCardClicked: ((l: String, c: String) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO)


    inner class BoardDetailViewHolder(
        private val binding: BoardDetailListItemBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        private val boardCardAdapter = BoardDetailCardAdapter()
        private var dataFlow: Flow<kotlin.collections.List<Card>>? = null
        private var job: Job? = null

        fun beginObserveData(){
            job = scope.launch {
                dataFlow?.collectLatest {
                    withContext(Dispatchers.Main){
                        submitCardList(it)
                    }
                }
            }
        }

        private fun submitCardList(list: kotlin.collections.List<Card>){
            binding.root.layoutParams.height = RecyclerView.LayoutParams.MATCH_PARENT
            boardCardAdapter.submitList(list)
        }

        fun pauseObserveData(){
            job?.cancel()
            job = null
        }

        fun bind(list: List) {
            dataFlow = cardDao.getCardsWithListId(list.listId)
            beginObserveData()
            binding.boardDetailListItemRecyclerview.adapter = boardCardAdapter
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

//    override fun onViewAttachedToWindow(holder: BoardDetailViewHolder) {
//        holder.beginObserveData()
//    }
//
//    override fun onViewDetachedFromWindow(holder: BoardDetailViewHolder) {
//        holder.pauseObserveData()
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardDetailViewHolder {
        return BoardDetailViewHolder(
            BoardDetailListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BoardDetailViewHolder, position: Int) {
        if (getItemViewType(position) == 1) holder.bind(getItem(position))
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