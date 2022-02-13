package com.tnh.mollert.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tnh.mollert.R
import com.tnh.mollert.databinding.HomeFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.toast.showToast
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
                navigateToBoardDetail("lol")
            }
        }
    }

    private fun navigateToBoardDetail(boardId: String) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBoardDetailFragment())
    }

    private fun initControl() {
        homeAdapter = HomeWorkSpaceAdapter()
        homeAdapter.submitList(viewModel.getBoardTest())

        binding.homeFragmentRecycleView.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = homeAdapter
        }

    }


}