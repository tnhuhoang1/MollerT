package com.tnh.mollert.boardDetail.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.databinding.DashboardFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment: DataBindingFragment<DashboardFragmentBinding>(R.layout.dashboard_fragment) {
    private val viewModel by viewModels<DashboardViewModel>()
    private val args by navArgs<DashboardFragmentArgs>()
    private val adapter by lazy {
        DashboardAdapter()
    }
    private lateinit var dashboardMenu: DashboardMenu

    override fun doOnCreateView() {
        super.doOnCreateView()
        setupToolbar()
    }

    private fun setupToolbar(){
        dashboardMenu = DashboardMenu(requireContext(), binding.dashboardFragmentToolbar.twoActionToolbarEndIcon)
        binding.dashboardFragmentToolbar.apply {
            twoActionToolbarTitle.text = "Dashboard"
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
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

        dashboardMenu.setOnMenuItemClickListener { menuItem->
            when(menuItem.itemId){
                R.id.dashboard_menu_card_per_list->{
                    lifecycleScope.launchWhenResumed {
                        setupDashboard(
                            viewModel.getCardPerList(args.boardId)
                        )
                    }
                }
                R.id.dashboard_menu_card_per_due->{
                    lifecycleScope.launchWhenResumed {
                        setupDashboard(
                            viewModel.getCardPerDueDate(args.boardId)
                        )
                    }
                }
                R.id.dashboard_menu_card_per_member->{
                    lifecycleScope.launchWhenResumed {
                        setupDashboard(
                            viewModel.getCardPerMember(args.boardId)
                        )
                    }
                }
                R.id.dashboard_menu_card_per_label->{
                    lifecycleScope.launchWhenResumed {
                        setupDashboard(
                            viewModel.getCardPerLabel(args.boardId)
                        )
                    }
                }
            }
            true
        }
    }

    private fun showOptionMenu() {
        dashboardMenu.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObserver()
        binding.dashboardFragmentRecycler.doOnPreDraw {
            adapter.setMaxHeightPixels(it.height)
            lifecycleScope.launchWhenResumed {
                setupDashboard(viewModel.getCardPerList(args.boardId))
            }
        }
    }


    private fun setupView(){
        binding.dashboardFragmentRecycler.adapter = adapter

    }

    private fun setupDashboard(dashboard: Dashboard){
        adapter.max = dashboard.max
        binding.dashboardFragmentTitle.text = dashboard.title
        binding.dashboardFragmentStone100.text = dashboard.max.toString()
        binding.dashboardFragmentStone75.text = (dashboard.max * 0.75f).toInt().toString()
        binding.dashboardFragmentStone50.text = (dashboard.max * 0.5f).toInt().toString()
        binding.dashboardFragmentStone25.text = (dashboard.max * 0.25f).toInt().toString()
        binding.dashboardFragmentStone0.text = dashboard.min.toString()
        adapter.submitList(dashboard.listItem)
    }


    private fun setupObserver(){

    }

}