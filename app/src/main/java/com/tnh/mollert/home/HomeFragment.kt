package com.tnh.mollert.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tnh.mollert.R
import com.tnh.mollert.databinding.HomeFragmentBinding
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : DataBindingFragment<HomeFragmentBinding>(R.layout.home_fragment){
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var homeAdapter: HomeWorkSpaceAdapter

    override fun doOnCreateView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreset()
        initControl()
    }

    private fun setupPreset(){
        binding.homeFragmentToolbar.apply {
            twoActionToolbarTitle.text = "MollerT"
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