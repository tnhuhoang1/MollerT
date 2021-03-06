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
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.home.CreateBoardDialog
import com.tnh.mollert.home.SearchDialog
import com.tnh.mollert.utils.LoadingModal
import com.tnh.mollert.utils.SpecialCharFilter
import com.tnh.mollert.utils.bindImageUriOrHide
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.toast.showToast
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoardDetailFragment: DataBindingFragment<BoardDetailFragmentBinding>(R.layout.board_detail_fragment) {
    val viewModel by viewModels<BoardDetailFragmentViewModel>()
    private lateinit var boardDetailAdapter: BoardDetailAdapter
    private val archivedCardDialog by lazy {
        ArchivedCardDialog(requireContext(), viewGroup)
    }
    private val args: BoardDetailFragmentArgs by navArgs()
    private var viewGroup: ViewGroup? = null
    private val descriptionDialog by lazy {
        DescriptionDialog(requireContext(), viewGroup)
    }
    private val changeBackgroundDialog by lazy {
        CreateBoardDialog(requireContext(), viewGroup)
    }
    private val searchDialog by lazy {
        SearchDialog(requireContext(), viewGroup)
    }

    private val loadingModal by lazy {
        LoadingModal(requireContext())
    }

    private val searchCardAdapter by lazy {
        SearchCardAdapter()
    }
    @Inject lateinit var prefManager: PrefManager

    private lateinit var popupMenu: BoardPopupMenu

    private val activityDialog by lazy {
        ActivityDialog(requireContext(),viewGroup)
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
        uri?.let {
            changeBackgroundDialog.setCustomImage(uri.toString())
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
        popupMenu = BoardPopupMenu(requireContext(), binding.boardDetailFragmentToolbar.twoActionToolbarEndIcon)
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
                    showChangeBackgroundDialog()
                }
                R.id.board_detail_menu_activity->{
                    activityDialog.setTitle("Board activities")
                    activityDialog.show()
                }
                R.id.board_detail_menu_archived_cards->{
                    showArchivedDialog()
                }
                R.id.board_detail_menu_leave->{
                    viewModel.leaveBoard(args.workspaceId, args.boardId, prefManager){
                        findNavController().navigateUp()
                    }
                }
                R.id.board_detail_menu_close->{
                    viewModel.closeBoard(args.workspaceId, args.boardId, prefManager)
                }

                R.id.board_detail_menu_invite->{
                    showInviteDialog()
                }

                R.id.board_detail_menu_private->{
                    popupMenu.setNewVisibility(R.id.board_detail_menu_private)
                    viewModel.changeVisibility(args.boardId, Board.VISIBILITY_PRIVATE)
                }

                R.id.board_detail_menu_public->{
                    popupMenu.setNewVisibility(R.id.board_detail_menu_public)
                    viewModel.changeVisibility(args.boardId, Board.VISIBILITY_PUBLIC)
                }
                R.id.board_detail_menu_board_name->{
                    showChangeBoardNameDialog()
                }
                R.id.board_detail_menu_dashboard ->{
                    navigateToDashboard()
                }
            }
            true
        }
    }
    private fun navigateToDashboard(){
        findNavController().navigate(BoardDetailFragmentDirections.actionBoardDetailFragmentToDashboardFragment(
            args.workspaceId,
            args.boardId
        ))
    }

    private fun showChangeBackgroundDialog() {
        changeBackgroundDialog.setTitle("Change background")
        changeBackgroundDialog.hideNameAndVisibility()
        changeBackgroundDialog.refresh()
        viewModel.boardWithLists.value?.board?.background?.let {
            changeBackgroundDialog.setSelectedDefaultBackground(it)
        }
        changeBackgroundDialog.onSelectImageClicked = {
            imageLauncher.launch(arrayOf("image/*"))
        }
        changeBackgroundDialog.onConfirmClicked = { name, vis, url ->
            url?.let {
                viewModel.changeBoardBackground(args.workspaceId, args.boardId, requireContext().contentResolver, url, changeBackgroundDialog.backgroundMode){
                    changeBackgroundDialog.dismiss()
                }
            }?: viewModel.postMessage("Please select background")
        }
        changeBackgroundDialog.show()
    }

    private fun showInviteDialog() {
        showAlertDialog("Invite to board"){ builder, createBoardLayoutBinding ->
            createBoardLayoutBinding.createBoardLayoutName.hint = "Email"
            createBoardLayoutBinding.createBoardLayoutName.filters = arrayOf(SpecialCharFilter())
            builder.setPositiveButton("Invite") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Email address can't be empty")
                }else{
                    viewModel.inviteMemberToBoard(createBoardLayoutBinding.createBoardLayoutName.text.toString().trim(), args.workspaceId)
                }
            }
        }
    }

    private fun showChangeListNameDialog(list: List) {
        showAlertDialog("Change list name"){ builder, createBoardLayoutBinding ->
            createBoardLayoutBinding.createBoardLayoutName.filters = arrayOf(SpecialCharFilter())
            createBoardLayoutBinding.createBoardLayoutName.hint = "List name"
            builder.setPositiveButton("OK") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("List name can't be empty")
                }else{
                    viewModel.changeListName(createBoardLayoutBinding.createBoardLayoutName.text.toString().trim(), args.workspaceId, args.boardId, list)
                }
            }
        }
    }

    private fun showChangeBoardNameDialog() {
        showAlertDialog("Change board name"){ builder, createBoardLayoutBinding ->
            createBoardLayoutBinding.createBoardLayoutName.filters = arrayOf(SpecialCharFilter())
            createBoardLayoutBinding.createBoardLayoutName.hint = "Board name"
            builder.setPositiveButton("OK") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Board name can't be empty")
                }else{
                    viewModel.changeBoardName(createBoardLayoutBinding.createBoardLayoutName.text.toString().trim(), args.workspaceId, args.boardId)
                }
            }
        }
    }

    private fun showArchivedDialog(){
        archivedCardDialog.setOnCardClicked(){ listId, cardId ->
            try {
                archivedCardDialog.dismiss()
                navigateToCard(args.workspaceId, args.boardId, listId, cardId)
            }catch (e: Exception){
                trace(e)
            }
        }
        archivedCardDialog.showFullscreen()
    }



    private fun showOptionMenu(){
        popupMenu.setLeaveOrClose(viewModel.isOwner)
        viewModel.boardWithLists.value?.board?.boardVisibility?.let {
            popupMenu.setVisibility(it)
        }
        popupMenu.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boardDetailAdapter = BoardDetailAdapter (DataSource.getInstance(requireContext()).cardDao){
            showCreateListDialog()
        }
        boardDetailAdapter.onNewCardClicked = { list ->
            showCreateCardDialog(list.listId)
        }
        boardDetailAdapter.onCardClicked = {listId, cardId ->
            navigateToCard(args.workspaceId, args.boardId, listId, cardId)
        }

        boardDetailAdapter.onArchiveListClicked = { list->
            AlertDialog.Builder(requireContext()).apply {
                setTitle("This will make all cards in this list archived")
                setPositiveButton("OK"){_, _->
                    viewModel.archiveList(list.listId)
                }
                setNegativeButton("CANCEL"){_, _->

                }
            }.show()
        }

        boardDetailAdapter.onDeleteListClicked = { list->
            AlertDialog.Builder(requireContext()).apply {
                setTitle("This operation is undone and will delete all cards in list. Proceed any way?")
                setPositiveButton("DO IT"){_, _->
                    viewModel.deleteList(list)
                }
                setNegativeButton("CANCEL"){_, _->

                }
            }.show()
        }

        boardDetailAdapter.onChangeListNameClicked = { list->
            showChangeListNameDialog(list)
        }

        binding.boardDetailFragmentRecyclerview.adapter = boardDetailAdapter
        binding.boardDetailFragmentSearchBox.setEndIconOnClickListener {
            if(binding.boardDetailFragmentSearchInput.text.isNullOrEmpty().not()){
                lifecycleScope.launchWhenResumed {
                    viewModel.searchCard(binding.boardDetailFragmentSearchInput.text.toString(), args.boardId).let { listCard ->
                        searchDialog.setCardAdapter(searchCardAdapter)
                        if(listCard.isEmpty()){
                            searchDialog.showNoResult()
                        }else{
                            searchDialog.hideNoResult()
                        }
                        searchCardAdapter.submitList(listCard)
                        searchCardAdapter.setRootClickListener{ card, _, _ ->
                            navigateToCard(args.workspaceId, args.boardId, card.listIdPar, card.cardId)
                            searchDialog.dismiss()
                        }
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
                    viewModel.setMessage("List name can't be empty")
                }else{
                    viewModel.createNewList(args.workspaceId, args.boardId, dialogBinding.createBoardLayoutName.text.toString())
                }
            }
        }
    }

    private fun showCreateCardDialog(listId: String){
        showAlertDialog("Create new card"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "Card name"
            dialogBinding.createBoardLayoutName.requestFocus()
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Card name can't be empty")
                }else{
                    viewModel.createNewCard(args.workspaceId, args.boardId, listId, dialogBinding.createBoardLayoutName.text.toString().trim())
                }
            }
        }
    }



    private fun setupObserver() {
        viewModel.boardWithLists.observe(viewLifecycleOwner){boardWithLists->
            requireActivity().runOnUiThread {
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
        }
        safeObserve(viewModel.boardWithLists){
            requireActivity().runOnUiThread {
                binding.boardDetailFragmentToolbar.twoActionToolbarTitle.text = it.board.boardName
                if(it.board.boardStatus == Board.STATUS_CLOSED){
                    viewModel.setMessage("The board was closed")
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
                binding.boardDetailFragmentBackground.bindImageUriOrHide(it.board.background)
            }
        }
        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }
        safeObserve(viewModel.memberAndActivity){
            activityDialog.submitList(it)
        }

        safeObserve(viewModel.isShowProgress){
            if(it){
                loadingModal.show()
            }else{
                loadingModal.dismiss()
            }
        }

        safeObserve(viewModel.cardArchived){
            archivedCardDialog.submitList(it)
        }
    }
}