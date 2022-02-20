package com.tnh.mollert.boardDetail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BoardDetailFragmentBinding
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.log
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.view.show
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoardDetailFragment: DataBindingFragment<BoardDetailFragmentBinding>(R.layout.board_detail_fragment) {
    val viewModel by viewModels<BoardDetailFragmentViewModel>()
    private lateinit var boardDetailAdapter: BoardDetailAdapter
    private val args: BoardDetailFragmentArgs by navArgs()
    @Inject lateinit var prefManager: PrefManager

    private val popupMenu by lazy {
        BoardPopupMenu(requireContext(), binding.boardDetailFragmentToolbar.twoActionToolbarEndIcon)
    }

    override fun doOnCreateView() {
        setupToolbar()
        binding.lifecycleOwner = this
        viewModel.getAllList(args.boardId)
    }

    private fun setupToolbar(){
        binding.boardDetailFragmentToolbar.apply {
            twoActionToolbarTitle.text = args.boardName
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_arrow_left)
            twoActionToolbarStartIcon.show()
            twoActionToolbarStartIcon.setOnClickListener {
                findNavController().navigateUp()
            }
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_more)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                showOptionMenu()
            }
        }
    }



    private fun showOptionMenu(){
        popupMenu.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boardDetailAdapter = BoardDetailAdapter (){
            showCreateListDialog()
        }
        boardDetailAdapter.onNewCardClicked = { listId ->
            showCreateCardDialog(listId)
        }
        boardDetailAdapter.onCardClicked = {listId, cardId ->
            listId.logAny()
        }
        binding.boardDetailFragmentRecyclerview.adapter = boardDetailAdapter
        setupObserver()
    }

    private fun showAlertDialog(title: String, builder: (AlertDialog.Builder, CreateBoardLayoutBinding)-> Unit){
        AlertDialog.Builder(requireContext()).apply {
            val binding = CreateBoardLayoutBinding.inflate(layoutInflater)
            setTitle(title)
            setView(binding.root)
            builder(this, binding)
        }.show()
    }

    private fun showCreateListDialog(){
        showAlertDialog("Create new list"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "List name"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("List name cannot be empty")
                }else{
                    viewModel.createNewList(args.workspaceId, args.boardId, dialogBinding.createBoardLayoutName.text.toString())
                }
            }
        }
    }

    private fun showCreateCardDialog(listId: String){
        showAlertDialog("Create new card"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "Card name"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("Card name cannot be empty")
                }else{
                    viewModel.createNewCard(args.workspaceId, args.boardId, listId, dialogBinding.createBoardLayoutName.text.toString())
                }
            }
        }
    }



    private fun setupObserver() {
        viewModel.boardWithLists.observe(viewLifecycleOwner){boardWithLists->
            if(boardWithLists == null){
                boardDetailAdapter.submitList(viewModel.getConcatList(listOf()))
            }else{
                if(boardWithLists.lists.isEmpty()){
                    viewModel.checkAndFetchList(prefManager, args.workspaceId, args.boardId)
                    boardDetailAdapter.submitList(viewModel.getConcatList(boardWithLists.lists))
                }else{
                    boardDetailAdapter.submitList(viewModel.getConcatList(boardWithLists.lists))
                }
            }
        }
    }

}