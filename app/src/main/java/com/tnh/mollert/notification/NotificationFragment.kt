package com.tnh.mollert.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.mollert.databinding.NotificationFragmentBinding
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class NotificationFragment: DataBindingFragment<NotificationFragmentBinding>(R.layout.notification_fragment) {
    private val viewModel by viewModels<NotificationViewModel>()
    private val adapter by lazy {
        NotificationAdapter()
    }
    private val notificationMenu by lazy {
        NotificationMenu(requireContext(), binding.notificationFragmentToolbar.twoActionToolbarEndIcon)
    }
    override fun doOnCreateView() {
        binding.notificationFragmentToolbar.apply {
            twoActionToolbarTitle.text = "Notifications"
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_more)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                showNotificationType()
            }
        }

    }

    private fun showNotificationType() {
        notificationMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.notification_menu_your->{
                    viewModel.changeToYourNotification()
                    collectData()
                }
                R.id.notification_menu_all->{
                    viewModel.changeToAllNotification()
                    collectData()
                }
            }
            true
        }
        notificationMenu.show()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObserver()
    }

    private fun setupObserver() {
        collectData()
        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }
    }

    private fun collectData(){
        lifecycleScope.launchWhenResumed {
            viewModel.memberAndActivity.collectLatest {
                adapter.submitList(it)
            }
        }
    }

    private fun setupView(){
        binding.notificationFragmentRecycler.adapter = adapter
        adapter.onItemClicked = { memberAndActivity ->
            when(memberAndActivity.activity.activityType){
                Activity.TYPE_INVITATION_WORKSPACE->{
                    onInvitationClicked(memberAndActivity.activity)
                }
                Activity.TYPE_INVITATION_BOARD->{
                    onBoardInvitationClicked(memberAndActivity.activity)
                }
                Activity.TYPE_ACTION->{
                    val cardId = MessageMaker.getEncodedRef(MessageMaker.HEADER_CARD, memberAndActivity.activity.message)
                    if(cardId.isEmpty().not()){
                        navigateToCard(cardId)
                    }
                }
            }
        }
    }

    fun navigateToCard(cardId: String){
        lifecycleScope.launchWhenResumed {
            viewModel.getCardById(cardId)?.let { boardAndCard ->
                findNavController().navigate(
                    NotificationFragmentDirections.actionNotificationFragmentToCardDetailFragment(
                        boardAndCard.board.workspaceId,
                        boardAndCard.board.boardId,
                        boardAndCard.card.listId,
                        boardAndCard.card.cardId
                    )
                )
            }
        }
    }

    private fun onInvitationClicked(activity: Activity){
        UserWrapper.getInstance()?.currentUserEmail?.let { currentEmail->
            val pair = MessageMaker.getWorkspaceInvitationParams(activity.message)
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

    private fun onBoardInvitationClicked(activity: Activity){
        UserWrapper.getInstance()?.currentUserEmail?.let { currentEmail->
            val pair = MessageMaker.getBoardInvitationParams(activity.message)
            if(pair.first.isNotEmpty() && pair.first != currentEmail){
                if(pair.second.isNotEmpty()){
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("Join this board?")
                        setPositiveButton("Join"){_,_->
                            viewModel.joinBoard(pair.second, pair.first, currentEmail)
                        }
                    }.show()
                }
            }
        }
    }

}