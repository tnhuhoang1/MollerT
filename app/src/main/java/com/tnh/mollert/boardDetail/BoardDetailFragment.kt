package com.tnh.mollert.boardDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.cardDetail.ActivityDialog
import com.tnh.mollert.databinding.BoardDetailFragmentBinding
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.home.SearchDialog
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.bindImageUriOrHide
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.toast.showToast
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoardDetailFragment: DataBindingFragment<BoardDetailFragmentBinding>(R.layout.board_detail_fragment) {
    val viewModel by viewModels<BoardDetailFragmentViewModel>()
    private lateinit var boardDetailAdapter: BoardDetailAdapter
    private val achievedCardDialog by lazy {
        AchievedCardDialog(requireContext(), viewGroup)
    }
    private val args: BoardDetailFragmentArgs by navArgs()
    private var viewGroup: ViewGroup? = null
    private val descriptionDialog by lazy {
        DescriptionDialog(requireContext(), viewGroup)
    }
    private val searchDialog by lazy {
        SearchDialog(requireContext(), viewGroup)
    }

    private val searchCardAdapter by lazy {
        SearchCardAdapter()
    }
    @Inject lateinit var prefManager: PrefManager

    private val popupMenu by lazy {
        BoardPopupMenu(requireContext(), binding.boardDetailFragmentToolbar.twoActionToolbarEndIcon)
    }

    private val activityDialog by lazy {
        ActivityDialog(requireContext(),viewGroup)
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
        uri?.let {
            viewModel.changeBoardBackground(args.workspaceId, args.boardId, requireContext().contentResolver, it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewGroup = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setBoardDoc(args.workspaceId, args.boardId)
        viewModel.checkAndFetchList(prefManager, args.workspaceId, args.boardId)
        viewModel.getAllList(args.boardId)
    }

    override fun doOnCreateView() {
        setupToolbar()
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

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
        popupMenu.setOnMenuItemClickListener { menuItem->
            when(menuItem.itemId){
                R.id.board_detail_menu_desc->{
                    viewModel.boardWithLists.value?.let { boardWithLists ->
                        descriptionDialog.onCreateClick = { content->
                            viewModel.changeDescription(args.workspaceId, args.boardId, content)
                        }
                        descriptionDialog.setHint("Write your board description...")
                        descriptionDialog.showFullscreen(boardWithLists.board.boardDesc)
                    }
                }
                R.id.board_detail_menu_background->{
                    imageLauncher.launch(arrayOf("image/*"))
                }
                R.id.board_detail_menu_activity->{
                    activityDialog.setTitle("Board activities")
                    activityDialog.show()
                }
                R.id.board_detail_menu_achieved_cards->{
                    showAchievedDialog()
                }
                R.id.board_detail_menu_leave->{
                    viewModel.leaveBoard(args.workspaceId, args.boardId){
                        findNavController().navigateUp()
                    }
                }
                R.id.board_detail_menu_close->{
                    viewModel.closeBoard(args.workspaceId, args.boardId)
                }
            }
            true
        }
    }

    private fun showAchievedDialog(){
        achievedCardDialog.showFullscreen()
    }



    private fun showOptionMenu(){
        popupMenu.setLeaveOrClose(viewModel.isOwner)
        popupMenu.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boardDetailAdapter = BoardDetailAdapter (AppRepository.getInstance(requireContext()).cardDao){
            showCreateListDialog()
        }
        boardDetailAdapter.onNewCardClicked = { listId ->
            showCreateCardDialog(listId)
        }
        boardDetailAdapter.onCardClicked = {listId, cardId ->
            navigateToCard(args.workspaceId, args.boardId, listId, cardId)
        }
        binding.boardDetailFragmentRecyclerview.adapter = boardDetailAdapter
        binding.boardDetailFragmentSearchBox.setEndIconOnClickListener {
            if(binding.boardDetailFragmentSearchInput.text.isNullOrEmpty().not()){
                lifecycleScope.launchWhenResumed {
                    viewModel.searchCard(binding.boardDetailFragmentSearchInput.text.toString()).let { listCard ->
                        searchDialog.setCardAdapter(searchCardAdapter)
                        searchCardAdapter.submitList(listCard)
                        searchDialog.show()
                    }
                }
            }
        }

        setupObserver()
    }

    private fun navigateToCard(workspaceId: String, boardId: String, listId: String, cardId: String){
        findNavController().navigate(BoardDetailFragmentDirections.actionBoardDetailFragmentToCardDetailFragment(workspaceId, boardId, listId, cardId))
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
                    boardDetailAdapter.submitList(viewModel.getConcatList(boardWithLists.lists))
                }else{
                    boardDetailAdapter.submitList(viewModel.getConcatList(boardWithLists.lists))
                }
            }
        }
        safeObserve(viewModel.boardWithLists){
            if(it.board.status == Board.STATUS_CLOSED){
                showToast("The board was closed")
                findNavController().popBackStack(R.id.homeFragment, false)
            }
            binding.boardDetailFragmentBackground.bindImageUriOrHide(it.board.background)
        }
        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }
        safeObserve(viewModel.memberAndActivity){
            activityDialog.submitList(it)
        }

        safeObserve(viewModel.cardAchieved){
            achievedCardDialog.submitList(it)
        }
    }
}