package com.tnh.mollert.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tnh.mollert.R
import com.tnh.mollert.cardDetail.label.LabelPickerDialog
import com.tnh.mollert.databinding.TimelineFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment

class TimelineFragment: DataBindingFragment<TimelineFragmentBinding>(R.layout.timeline_fragment) {
    private val labelDialog by lazy{
        LabelPickerDialog(requireContext(), container)
    }
    private var container: ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        labelDialog.showFullscreen()
    }

}