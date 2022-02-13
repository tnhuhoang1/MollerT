package com.tnh.mollert.cardDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.cardDetail.label.LabelPickerDialog
import com.tnh.mollert.databinding.CardDetailFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.tnhlibrary.dataBinding.utils.initBinding

@AndroidEntryPoint
class CardDetailFragment: DataBindingFragment<CardDetailFragmentBinding>(R.layout.card_detail_fragment) {
    val viewModel by viewModels<CardDetailFragmentViewModel>()
    private var container: ViewGroup? = null
    private val dialog by lazy(){
        LabelPickerDialog(requireContext(), container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initBinding(layoutRes,container, false)
        this.container = container

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog.showFullscreen()
    }


}

