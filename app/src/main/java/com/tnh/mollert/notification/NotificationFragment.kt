package com.tnh.mollert.notification

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.NotificationFragmentBinding
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment: DataBindingFragment<NotificationFragmentBinding>(R.layout.notification_fragment) {
    private val viewModel by viewModels<NotificationViewModel>()
    private val adapter by lazy {
        NotificationAdapter()
    }
    override fun doOnCreateView() {
        binding.notificationFragmentToolbar.twoActionToolbarTitle.text = "Notifications"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObserver()
    }

    private fun setupObserver() {
        safeObserve(viewModel.memberAndActivity){
            adapter.submitList(it)
        }
        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }
    }

    private fun setupView(){
        binding.notificationFragmentRecycler.adapter = adapter
        adapter.onItemClicked = { memberAndActivity ->
            when(memberAndActivity.activity.activityType){
                Activity.TYPE_INVITATION->{
                    onInvitationClicked(memberAndActivity.activity)
                }
            }
        }
    }

    private fun onInvitationClicked(activity: Activity){
        UserWrapper.getInstance()?.currentUserEmail?.let { currentEmail->
            val pair = MessageMaker.getInvitationParams(activity.message)
            if(pair.first.isNotEmpty() && pair.first != currentEmail){
                if(pair.second.isNotEmpty()){
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("Join this workspace?")
                        setPositiveButton("Join"){_,_->
                            viewModel.joinWorkspace(pair.first, pair.second, currentEmail)
                        }
                    }.show()

                }
            }
        }
    }

}