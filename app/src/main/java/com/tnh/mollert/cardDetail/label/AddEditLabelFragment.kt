package com.tnh.mollert.cardDetail.label

import android.app.AlertDialog
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AddLabelBinding
import com.tnh.mollert.utils.LabelPreset
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.view.hide
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditLabelFragment: DataBindingFragment<AddLabelBinding>(R.layout.add_label){
    private val viewModel: AddEditLabelViewModel by viewModels<AddEditLabelViewModel>()
    private val adapter by lazy {
        LabelColorAdapter(){ _, _ -> }
    }
    private val args by navArgs<AddEditLabelFragmentArgs>()
    override fun doOnCreateView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        if(args.labelId.isEmpty()){
            viewModel.mode = AddEditLabelViewModel.MODE_CREATE
        }else{
            viewModel.mode = AddEditLabelViewModel.MODE_EDIT
        }
        setupToolbar()
    }

    private fun setupToolbar(){
        binding.addLabelToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarStartIcon.setOnClickListener {
                viewModel.dispatchClickEvent(AddEditLabelViewModel.EVENT_BACK)
            }
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                viewModel.dispatchClickEvent(AddEditLabelViewModel.EVENT_OK)
            }
            if(args.labelId.isEmpty()){
                twoActionToolbarTitle.text = "Add Label"
                binding.labelItemDelete.hide()
            }else{
                twoActionToolbarTitle.text = "Edit Label"
                binding.labelItemDelete.show()
                binding.addLabelName.setText(args.labelName)
                binding.labelItemDelete.setOnClickListener {
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("Do you really want to delete this label?")
                        setPositiveButton("DELETE"){ _, _ ->
                            viewModel.deleteLabel(args.workspaceId, args.boardId, args.labelId)
                        }
                    }.show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventObserve(viewModel.clickEvent){
            when(it){
                AddEditLabelViewModel.EVENT_BACK->{
                    findNavController().navigateUp()
                }
                AddEditLabelViewModel.EVENT_OK->{
                    if(viewModel.mode == AddEditLabelViewModel.MODE_CREATE){
                        addLabel()
                    }else{
                        editLabel()
                    }
                }
                AddEditLabelViewModel.EVENT_DELETE_OK, AddEditLabelViewModel.EVENT_ADD_OK ->{
                    findNavController().navigateUp()
                }
            }
        }

        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }

        binding.labelItemRecycler.adapter = adapter
        adapter.submitList(LabelPreset.colorDataSet)
    }

    private fun addLabel(){
        adapter.selectedPosition?.let {
            viewModel.addLabel(args.workspaceId, args.boardId, LabelPreset.colorDataSet[it], binding.addLabelName.text.toString().trim())
        } ?: viewModel.setMessage("Please select color")
    }

    private fun editLabel(){
        adapter.selectedPosition?.let {
            viewModel.editLabel(args.workspaceId, args.boardId, args.labelId, LabelPreset.colorDataSet[it], binding.addLabelName.text.toString().trim())
        } ?: viewModel.setMessage("Please select color")
    }
}