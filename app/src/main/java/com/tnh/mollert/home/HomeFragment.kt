package com.tnh.mollert.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tnh.mollert.MainActivity
import com.tnh.mollert.R
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.databinding.HomeFragmentBinding
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.utils.LoadingModal
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logVar
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : DataBindingFragment<HomeFragmentBinding>(R.layout.home_fragment){
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var homeAdapter: HomeWorkSpaceAdapter
    private val loading by lazy {
        LoadingModal(requireContext())
    }
    override fun doOnCreateView() {
        (activity as MainActivity?)?.showBottomNav()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.loadMemberWithWorkspaces()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            homeAdapter.submitList(it.workspaces)
            submitBoardList(it.workspaces)
        }

        safeObserve(viewModel.boards){
            viewModel.memberWithWorkspaces.value?.workspaces?.let {
                submitBoardList(it)
            }
        }

        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
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

    private fun navigateToBoardDetail(boardId: String) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBoardDetailFragment())
    }

    private fun navigateToAddFragment(){
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddWorkspaceFragment())
    }

    private fun initControl() {
        homeAdapter = HomeWorkSpaceAdapter(onClick)
        homeAdapter.onNewClicked = { ws->
            showCreateDialog(ws)
        }

        homeAdapter.onNewMemberClicked = {ws->
            showInviteDialog(ws)
        }


        binding.homeFragmentRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }
    }

    private fun showCreateDialog(ws: Workspace){
        showAlertDialog("Add new board"){ builder, createBoardLayoutBinding ->
            builder.setPositiveButton("Create") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("Board name cannot be empty")
                }else{
                    viewModel.createBoard(ws, createBoardLayoutBinding.createBoardLayoutName.text.toString())
                }
            }
        }
    }

    private fun showInviteDialog(ws: Workspace){
        showAlertDialog("Invite to workspace"){ builder, createBoardLayoutBinding ->
            createBoardLayoutBinding.createBoardLayoutName.hint = "Email"
            builder.setPositiveButton("Invite") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("Email address cannot be empty")
                }else{
                    viewModel.inviteMember(ws, createBoardLayoutBinding.createBoardLayoutName.text.toString())
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

    private val onClick: (String) -> Unit = {
        navigateToBoardDetail(it)
    }

}