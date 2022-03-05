package com.tnh.mollert.home.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.databinding.ManageWorkspaceFragmentBinding
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.hideKeyboard
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageWorkspaceFragment: DataBindingFragment<ManageWorkspaceFragmentBinding>(R.layout.manage_workspace_fragment) {
    private val viewModel by viewModels<ManageWorkspaceViewModel>()
    private val args by navArgs<ManageWorkspaceFragmentArgs>()
    private val adapter by lazy {
        MemberLargeAdapter()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadWorkspace(args.workspaceId)
    }
    private var container: ViewGroup? = null
    private val memberDialog by lazy {
        MemberDialog(requireContext(), container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun doOnCreateView() {
        setupToolbar()
    }

    private fun setupToolbar(){
        binding.manageWorkspaceFragmentToolbar.apply {
            twoActionToolbarTitle.text = "Manage workspace"
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarStartIcon.setOnClickListener {
                findNavController().navigateUp()
            }
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_user_add)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                showInviteDialog()
            }
        }
    }
    private fun showInviteDialog(){
        showAlertDialog("Invite to workspace"){ builder, createBoardLayoutBinding ->
            createBoardLayoutBinding.createBoardLayoutName.hint = "Email"
            builder.setPositiveButton("Invite") { _, _ ->
                if(createBoardLayoutBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Email address cannot be empty")
                }else{
                    viewModel.homeWorkspace.value?.let {
                        viewModel.inviteMember(it.workspace, createBoardLayoutBinding.createBoardLayoutName.text.toString().trim())
                    }
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupObserver()
    }

    private fun showMemberProfile(member: Member){
        memberDialog.showMember(member)
    }

    private fun setupListener() {
        binding.manageWorkspaceFragmentRecycler.adapter = adapter
        adapter.onClicked = { member ->
            showMemberProfile(member)
        }

        adapter.onLongClicked = { member ->

        }

        binding.apply {
            manageWorkspaceFragmentName.setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    manageWorkspaceFragmentNameApply.show()
                }else{
                    manageWorkspaceFragmentNameApply.gone()
                }
            }



            manageWorkspaceFragmentNameApply.setOnClickListener {
                manageWorkspaceFragmentName.clearFocus()
                it.hideKeyboard()
                changeWorkspaceName(manageWorkspaceFragmentName.text.toString())
            }

            manageWorkspaceFragmentDesc.setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    manageWorkspaceFragmentDescApply.show()
                }else{
                    manageWorkspaceFragmentDescApply.gone()
                }
            }

            manageWorkspaceFragmentDescApply.setOnClickListener {
                manageWorkspaceFragmentDesc.clearFocus()
                it.hideKeyboard()
                changeWorkspaceDesc(manageWorkspaceFragmentDesc.text.toString())
            }
        }
    }


    private fun changeWorkspaceName(name: String?){
        if(name.isNullOrBlank()){
            viewModel.postMessage("Name can not be empty")
        }else{
            viewModel.changeName(name.trim(), args.workspaceId)
        }
    }

    private fun changeWorkspaceDesc(name: String?){
        if(name.isNullOrBlank()){
            viewModel.postMessage("Name can not be empty")
        }else{
            viewModel.changeDesc(name.trim(), args.workspaceId)
        }
    }

    private fun setupObserver(){
        safeObserve(viewModel.homeWorkspace){ workspaceWithMembers->
            bindWorkspaceData(workspaceWithMembers.workspace)
            lifecycleScope.launchWhenResumed {
                viewModel.getWorkspaceOwner(args.workspaceId)?.let {
                    adapter.leader = it.email
                    adapter.submitList(workspaceWithMembers.members)
                }
            }
        }

        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }
    }

    private fun bindWorkspaceData(workspace: Workspace){
        binding.apply {
            manageWorkspaceFragmentName.setText(workspace.workspaceName)
            manageWorkspaceFragmentDesc.setText(workspace.workspaceDesc)
            manageWorkspaceFragmentType.setText(workspace.workspaceType)
        }
    }
}