package com.tnh.mollert.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tnh.mollert.MainActivity
import com.tnh.mollert.R
import com.tnh.mollert.databinding.HomeFragmentBinding
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.log
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logVar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : DataBindingFragment<HomeFragmentBinding>(R.layout.home_fragment){
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var homeAdapter: HomeWorkSpaceAdapter

    override fun doOnCreateView() {
        (activity as MainActivity?)?.showBottomNav()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.registerWorkspace()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreset()
        initControl()
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

        homeAdapter = HomeWorkSpaceAdapter(onClick,getBoardList)
        homeAdapter.submitList(viewModel.getWorkSpaceTest())

        binding.homeFragmentRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }

    }

    private val onClick: (String) -> Unit = {
        navigateToBoardDetail(it)
    }

    private val getBoardList: (String) -> List<Board> = {
        viewModel.getBoardTest()
    }
}