package com.tnh.mollert.test

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.mollert.databinding.TestFragmentBinding
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef
import com.tnh.mollert.datasource.remote.model.RemoteWorkspaceRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logVar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestFragment: DataBindingFragment<TestFragmentBinding>(R.layout.test_fragment) {
    private val viewModel by viewModels<TestFragmentViewModel>()
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        FirestoreHelper.getInstance().run {

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testFragmentButton.setOnClickListener {

            fileChooserLauncher.launch(arrayOf("image/*"))

        }
    }

}