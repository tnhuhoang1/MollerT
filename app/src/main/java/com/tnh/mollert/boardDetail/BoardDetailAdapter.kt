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
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

class BoardDetailAdapter(
    private val cardDao: CardDao,
    private val addNewList:() -> Unit
) : ListAdapter<List, BoardDetailAdapter.BoardDetailViewHolder>(ListDiffUtil()) {
    var onNewCardClicked: ((list: List) -> Unit)? = null
    var onDeleteListClicked: ((list: List) -> Unit)? = null
    var onArchiveListClicked: ((list: List) -> Unit)? = null
    var onChangeListNameClicked: ((list: List) -> Unit)? = null
    var onCardClicked: ((l: String, c: String) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    var onSortSelected: (sortType:String) -> Unit = {}

    inner class BoardDetailViewHolder(
        private val binding: BoardDetailListItemBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        private val boardCardAdapter = BoardDetailCardAdapter()
        private var dataFlow: Flow<kotlin.collections.List<Card>>? = null
        private var job: Job? = null
        private var sorted: String? = null

        fun beginObserveData(){
            job = scope.launch {
                dataFlow?.collectLatest { listCard->
                    sorted?.let{
                        val l1 = mutableListOf<Card>()
                        val l2 = mutableListOf<Card>()
                        when(sorted){
                            SORT_BY_DUE_DATE->{
                                val current =  System.currentTimeMillis()
                                listCard.forEach { card ->
                                    if (card.dueDate == 0L || card.dueDate < current){
                                        l1.add(card)
                                    }else{
                                        l2.add(card)
                                    }
                                    withContext(Dispatchers.Main){
                                        submitCardList(l2 + l1)
                                    }
                                }
                            }
                            else->{
                                withContext(Dispatchers.Main){
                                    submitCardList(listCard)
                                }
                            }
                        }
                    } ?: withContext(Dispatchers.Main){
                        submitCardList(listCard)
                    }
                }
            }
        }

        fun changeSortType(sortType: String, listId: String){
            when(sortType){
                SORT_BY_DUE_DATE ->{
                    sorted = SORT_BY_DUE_DATE
                    dataFlow = cardDao.getCardsWithListIdSortedByDueDate(listId)
                }
                SORT_BY_NAME ->{
                    sorted = SORT_BY_NAME
                    dataFlow = cardDao.getCardsWithListIdSortedByName(listId)
                }
                SORT_BY_DATE_ADDED ->{
                    sorted = SORT_BY_DATE_ADDED
                    dataFlow = cardDao.getCardsWithListIdSortedByDateAdded(listId)
                }
            }
            beginObserveData()
        }

        private fun submitCardList(list: kotlin.collections.List<Card>){
            if(list.isNotEmpty()){
                binding.root.layoutParams.height = RecyclerView.LayoutParams.MATCH_PARENT
                boardCardAdapter.submitList(list)
            }else{
                boardCardAdapter.submitList(list)
            }
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
                onNewCardClicked?.invoke(list)
            }
            binding.boardDetailListItemToolbar.setOnMenuItemClickListener { menuItem->
                when(menuItem.itemId){
                    R.id.board_detail_item_menu_add->{
                        onNewCardClicked?.invoke(list)
                    }
                    R.id.board_detail_item_menu_archived->{
                        onArchiveListClicked?.invoke(list)
                    }
                    R.id.board_detail_item_menu_sort_by_date->{
                        changeSortType(SORT_BY_DATE_ADDED, list.listId)
                        onSortSelected(SORT_BY_DATE_ADDED)
                    }
                    R.id.board_detail_item_menu_sort_by_name->{
                        changeSortType(SORT_BY_NAME, list.listId)
                        onSortSelected(SORT_BY_NAME)
                    }
                    R.id.board_detail_item_menu_sort_by_due_date->{
                        changeSortType(SORT_BY_DUE_DATE, list.listId)
                        onSortSelected(SORT_BY_DUE_DATE)
                    }
                    R.id.board_detail_item_menu_delete->{
                        onDeleteListClicked?.invoke(list)
                    }
                    R.id.board_detail_item_menu_list_name->{
                        onChangeListNameClicked?.invoke(list)
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
                binding.root.setOnClickListener {
                    onCardClicked?.invoke(card.listIdPar, card.cardId)
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
    companion object{
        const val SORT_BY_NAME = "name"
        const val SORT_BY_DATE_ADDED = "date_added"
        const val SORT_BY_DUE_DATE = "due_date"
    }
}