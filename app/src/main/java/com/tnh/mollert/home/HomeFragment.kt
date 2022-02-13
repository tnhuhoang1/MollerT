package com.tnh.mollert.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tnh.mollert.databinding.HomeFragmentBinding


class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var homeAdapter: HomeWorkSpaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        initControl()
        return binding.root
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