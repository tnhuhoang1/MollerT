package com.tnh.mollert.home.addWorkspace

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AddWorkspaceFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.logAny
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddWorkspaceFragment: DataBindingFragment<AddWorkspaceFragmentBinding>(R.layout.add_workspace_fragment) {
    private val viewModel by viewModels<AddWorkspaceViewModel>()

    override fun doOnCreateView() {
        populateData()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun populateData(){
        binding.addWorkspaceFragmentToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.isVisible = true
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.isVisible = true
            twoActionToolbarTitle.text = "New Workspace"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addWorkspaceFragmentType.setOnClickListener {
            binding.viewView.isVisible = true
        }
    }

}