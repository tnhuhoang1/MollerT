package com.tnh.mollert.cardDetail.label

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AddLabelBinding
import com.tnh.mollert.utils.LabelPreset
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditLabelFragment: DataBindingFragment<AddLabelBinding>(R.layout.add_label){
    private val viewModel: AddEditLabelViewModel by viewModels<AddEditLabelViewModel>()
    private val adapter by lazy {
        LabelColorAdapter(){ name, _ ->
            binding.addLabelName.setText(name)
        }
    }
    override fun doOnCreateView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
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
            twoActionToolbarTitle.text = "Add Label"
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
                    addLabel()
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
            viewModel.addLabel(LabelPreset.colorDataSet[it])
        } ?: viewModel.setMessage("Please select color")
    }
}