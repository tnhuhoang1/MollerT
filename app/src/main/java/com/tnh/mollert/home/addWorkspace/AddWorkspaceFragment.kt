package com.tnh.mollert.home.addWorkspace

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AddWorkspaceFragmentBinding
import com.tnh.mollert.utils.LoadingModal
import com.tnh.mollert.utils.SpecialCharFilter
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddWorkspaceFragment: DataBindingFragment<AddWorkspaceFragmentBinding>(R.layout.add_workspace_fragment) {
    private val viewModel by viewModels<AddWorkspaceViewModel>()
    private val loading by lazy {
        LoadingModal(requireContext())
    }

    override fun doOnCreateView() {
        populateData()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun populateData(){
        binding.addWorkspaceFragmentToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.isVisible = true
            twoActionToolbarStartIcon.setOnClickListener {
                viewModel.dispatchClickEvent(AddWorkspaceViewModel.EVENT_BACK_CLICKED)
            }
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.isVisible = true
            twoActionToolbarEndIcon.setOnClickListener {
                viewModel.dispatchClickEvent(AddWorkspaceViewModel.EVENT_ADD_CLICKED)
            }
            twoActionToolbarTitle.text = "New Workspace"
        }
        binding.addWorkspaceFragmentName.filters = arrayOf(SpecialCharFilter())
        viewModel.setupListType {
            createTypeItem(it)
        }
        viewModel.listTv.forEachIndexed { index, materialTextView ->
            materialTextView.setOnClickListener {
                onTypeItemClicked(index)
            }
            binding.addWorkspaceFragmentTypeContainer.addView(materialTextView)
        }
    }

    private fun onTypeItemClicked(pos: Int){
        viewModel.changeSelectItem(pos)
    }

    private fun createTypeItem(typeName: String): MaterialTextView{
        val tv = MaterialTextView(requireContext())
        tv.setPadding(resources.getDimensionPixelSize(R.dimen.dimen_12))
        tv.text = typeName
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ResourcesCompat.getDrawable(resources, R.drawable.vd_circle_default, null),
            null,null,null
        )
        tv.isFocusable = true
        tv.isClickable = true
        val typeValue = TypedValue()
        if(requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, typeValue, true)){
            tv.setBackgroundResource(typeValue.resourceId)
        }
        tv.compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.dimen_8)
        tv.gone()
        return tv
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        safeObserve(viewModel.isExpanded){
            if(it){
                viewModel.listTv.forEach { tv->
                    tv.show()
                    val objectAnimator = ObjectAnimator.ofFloat(binding.addWorkspaceFragmentArrayDown, View.ROTATION_X, 0f, 180f)
                    objectAnimator.duration = 100
                    objectAnimator.start()
                }
            }else{
                viewModel.listTv.forEach { tv->
                    tv.gone()
                    val objectAnimator = ObjectAnimator.ofFloat(binding.addWorkspaceFragmentArrayDown, View.ROTATION_X, 180f, 0f)
                    objectAnimator.duration = 100
                    objectAnimator.start()
                }
            }
        }

        eventObserve(viewModel.clickEvent){
            when(it){
                AddWorkspaceViewModel.EVENT_ADD_CLICKED->{
                    createWorkspace()
                }
                AddWorkspaceViewModel.EVENT_BACK_CLICKED->{
                    findNavController().navigateUp()
                }
                AddWorkspaceViewModel.EVENT_SUCCESS->{
                    clearInput()
                    viewModel.dispatchClickEvent(AddWorkspaceViewModel.EVENT_BACK_CLICKED)
                }
            }
        }

        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }

        safeObserve(viewModel.selectedPosition){
            binding.addWorkspaceFragmentType.setText(viewModel.listTv[it].text)
            viewModel.changeIcon(it)
        }

        safeObserve(viewModel.isLoading){
            if(it){
                loading.show()
            }else{
                loading.dismiss()
            }
        }

        binding.addWorkspaceFragmentType.setOnClickListener {
            viewModel.toggleType()
        }
    }

    private fun createWorkspace(){
        viewModel.addWorkspace(
            binding.addWorkspaceFragmentName.text.toString().trim(),
            binding.addWorkspaceFragmentType.text.toString(),
            binding.addWorkspaceFragmentDesc.text.toString()
        )
    }

    fun clearInput(){
        binding.addWorkspaceFragmentName.setText("")
        binding.addWorkspaceFragmentType.setText("")
        binding.addWorkspaceFragmentDesc.setText("")
    }

}