package com.tnh.mollert.boardDetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BoardDetailFragmentBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.logE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoardDetailFragment: DataBindingFragment<BoardDetailFragmentBinding>(R.layout.board_detail_fragment) {
    val viewModel by viewModels<BoardDetailFragmentViewModel>()
    private lateinit var boardDetailAdapter: BoardDetailAdapter
    override fun doOnCreateView() {
        boardDetailAdapter = BoardDetailAdapter (getCardList, addNewList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boardDetailAdapter.submitList(viewModel.getListTest())
        binding.boardDetailFragmentRecyclerview.adapter = boardDetailAdapter
    }
    private val getCardList: (String) -> ArrayList<Card> = {
        viewModel.getCardTest()
    }

    private val addNewList: () -> Unit = {

    }
}