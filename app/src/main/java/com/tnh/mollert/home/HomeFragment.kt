package com.tnh.mollert.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tnh.mollert.MainActivity
import com.tnh.mollert.R
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.databinding.HomeFragmentBinding
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.utils.LoadingModal
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.toast.showToast
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.hideKeyboard
import com.tnh.tnhlibrary.view.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : DataBindingFragment<HomeFragmentBinding>(R.layout.home_fragment){
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var homeAdapter: HomeWorkSpaceAdapter
    @Inject lateinit var prefManager: PrefManager
    private var container: ViewGroup? = null
    private val loading by lazy {
        LoadingModal(requireContext())
    }
    private val createBoardDialog by lazy {
        CreateBoardDialog(requireContext(), container)
    }
    private val searchBoardAdapter by lazy {
        SearchBoardAdapter()
    }
    private val searchDialog by lazy {
        SearchDialog(requireContext(), container)
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
        uri?.let {
            createBoardDialog.setCustomImage(uri.toString())
        }
    }
    override fun doOnCreateView() {
        (activity as MainActivity?)?.showBottomNav()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.syncWorkspacesAndBoardsDataFirstTime(prefManager)
        viewModel.loadMemberWithWorkspaces()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.hideKeyboard()
        setupPreset()
        initControl()
        observeData()
    }

    private fun submitBoardList(workspaces: List<Workspace>){
        lifecycleScope.launchWhenResumed {
            // TODO: Need to improve performance
            homeAdapter.submitBoardList(viewModel.getAllBoardOfUser(workspaces))
        }
    }

    private fun observeData() {
        safeObserve(viewModel.memberWithWorkspaces){
            if(it.workspaces.isEmpty()){
                binding.homeFragmentNoWorkspace.show()
            }else{
                binding.homeFragmentNoWorkspace.gone()
            }
            lifecycleScope.launchWhenStarted {
                withContext(Dispatchers.Default){
                    it.workspaces.sortedBy { workspace->
                        workspace.workspaceName
                    }
                }.let { l->
                    homeAdapter.submitList(l)
                    submitBoardList(l)
                }
            }
        }

        safeObserve(viewModel.boards){
            viewModel.memberWithWorkspaces.value?.workspaces?.let {
                submitBoardList(it)
            }
        }

        eventObserve(viewModel.message){
            showToast(it)
        }

        safeObserve(viewModel.loading){
            if(it){
                loading.show()
            }else{
                loading.dismiss()
            }
        }
    }

    private fun setupPreset(){
        binding.homeFragmentToolbar.apply {
            twoActionToolbarTitle.text = getString(R.string.app_name)
            twoActionToolbarEndIcon.setImageResource(R.drawable.ic_baseline_add_24)
            twoActionToolbarEndIcon.visibility = View.VISIBLE
            twoActionToolbarEndIcon.setOnClickListener {
                navigateToAddFragment()
            }
        }


    }

    private fun navigateToBoardDetail(workspaceId: String, boardId: String, boardName: String) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBoardDetailFragment(workspaceId, boardId, boardName))
    }

    private fun navigateToAddFragment(){
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddWorkspaceFragment())
    }

    private fun navigateToManageWorkspace(workspaceId: String){
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToManageWorkspaceFragment(workspaceId))
    }

    private fun initControl() {
        homeAdapter = HomeWorkSpaceAdapter(onClick, viewModel.getMemberWorkspaceDao())
        homeAdapter.onNewClicked = { ws->
            showCreateDialog(ws)
        }

        homeAdapter.onSettingClicked = { ws->
            navigateToManageWorkspace(ws.workspaceId)
        }

        homeAdapter.onInviteClicked = {ws->
            showInviteDialog(ws)
        }


        binding.homeFragmentRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }

        binding.homeFragmentSearchBox.setEndIconOnClickListener {
            val search = binding.homeFragmentSearchInput.text.toString()
            if(search.isEmpty().not()){
                searchBoardAdapter.setRootClickListener { data, _, _ ->
                    try {
                        searchDialog.dismiss()
                        onClick(data.workspaceId, data.boardId, data.boardName)
                    }catch (e:Exception){
                        trace(e)
                    }
                }
                lifecycleScope.launchWhenResumed {
                    searchBoardAdapter.submitList(viewModel.searchBoard(search))
                    searchDialog.setBoardAdapter(searchBoardAdapter)
                    searchDialog.show()
                }
            }
        }
    }

    private fun showCreateDialog(ws: Workspace){
        createBoardDialog.refresh()
        createBoardDialog.onSelectImageClicked = {
            imageLauncher.launch(arrayOf("image/*"))
        }
        createBoardDialog.onConfirmClicked = { name, vis, url ->
            if(name.isBlank()){
                viewModel.setMessage("Board name cannot be empty")
            }else{
                if(vis == "null"){
                    viewModel.setMessage("Please select visibility")
                }else{
                    url?.let {
                        viewModel.createBoard(ws, name, vis!!, url, createBoardDialog.backgroundMode, requireContext().contentResolver){
                            createBoardDialog.dismiss()
                        }
                    }?: viewModel.postMessage("Please select background")
                }
            }
        }
        createBoardDialog.show()
    }

    private fun showInviteDialog(ws: Workspace){
        showAlertDialog("Invite to workspace"){ builder, createBoardLayoutBinding ->
            createBoardLayoutBinding.createBoardLayoutName.hint = "Email"
            builder.setPositiveButton("Invite") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Email address cannot be empty")
                }else{
                    viewModel.inviteMember(ws, createBoardLayoutBinding.createBoardLayoutName.text.toString().trim())
                }
            }
        }
    }

    private fun showAlertDialog(title: String, builder: (AlertDialog.Builder, CreateBoardLayoutBinding)-> Unit){
        AlertDialog.Builder(requireContext()).apply {
            val binding = CreateBoardLayoutBinding.inflate(layoutInflater)
            setTitle(title)
            setView(binding.root)

            setNegativeButton("Cancel"){_, _ -> }
            builder(this, binding)
        }.show()
    }

    private val onClick: (workspaceId: String, boardId: String, boardName: String) -> Unit = { workspaceId, boardId, boardName->
        lifecycleScope.launchWhenResumed {
            if(viewModel.isJoinedThisBoard(boardId)){
                viewModel.getBoardById(boardId)?.let {
                    "Navigate to $it".logAny()
                    if(it.boardStatus == Board.STATUS_CLOSED){
                        if(viewModel.isOwnerOfThisBoard(boardId)){
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle("This board was closed!")
                                setPositiveButton("OK"){_,_->
                                }
                                setNegativeButton("REOPEN"){_,_->
                                    viewModel.reopenBoard(workspaceId, boardId)
                                }
                            }.show()
                        }else{
                            viewModel.postMessage("This board was closed")
                        }
                    }else{
                        navigateToBoardDetail(workspaceId, boardId, boardName)
                    }
                }
            }else{
                lifecycleScope.launchWhenResumed {
                    viewModel.getBoardById(boardId)?.let { board->
                        if(board.boardVisibility == Board.VISIBILITY_PRIVATE){
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle("This board is private")
                                setPositiveButton("OK"){_,_->
                                }
                            }.show()
                        }else{
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle("Join this board?")
                                setPositiveButton("Join"){_,_->
                                    viewModel.joinBoard(workspaceId, boardId){
                                        navigateToBoardDetail(workspaceId, boardId, boardName)
                                    }
                                }
                            }.show()
                        }
                    }
                }
            }
        }

    }

}