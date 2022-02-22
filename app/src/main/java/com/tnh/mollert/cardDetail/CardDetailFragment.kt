package com.tnh.mollert.cardDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.boardDetail.DescriptionDialog
import com.tnh.mollert.cardDetail.label.LabelPickerDialog
import com.tnh.mollert.databinding.CardDetailFragmentBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar

@AndroidEntryPoint
class CardDetailFragment: DataBindingFragment<CardDetailFragmentBinding>(R.layout.card_detail_fragment) {
    val viewModel by viewModels<CardDetailFragmentViewModel>()
    private val args by navArgs<CardDetailFragmentArgs>()
    private var container: ViewGroup? = null
    private val labelPickerDialog by lazy(){
        LabelPickerDialog(requireContext(), container)
    }
    private val descriptionDialog by lazy {
        DescriptionDialog(requireContext(), container)
    }

    private val optionMenu by lazy {
        CardPopupMenu(requireContext(), binding.cardDetailFragmentToolbar.twoActionToolbarEndIcon)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun doOnCreateView() {
        setupToolbar()
        viewModel.setCardDoc(args.workspaceId, args.boardId, args.listId, args.cardId)
        viewModel.getCardById(args.cardId)
        viewModel.getLabelById(args.boardId)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
    }

    private fun setupToolbar(){
        binding.cardDetailFragmentToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_more)
            twoActionToolbarEndIcon.show()

            twoActionToolbarStartIcon.setOnClickListener {
                findNavController().navigateUp()
            }

            twoActionToolbarEndIcon.setOnClickListener {
                showOptionMenu()
            }
        }
    }

    private fun showOptionMenu(){
        optionMenu.setOnMenuItemClickListener { item->
            when(item.itemId){

            }
            true
        }
        optionMenu.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupObserver()
    }

    private fun setupListener(){
        binding.cardDetailFragmentLabel.setOnClickListener {
            labelPickerDialog.onCreateClick = {
                navigateToCreateLabel()
            }
            labelPickerDialog.setEditLabelListener { labelId, labelName ->
                labelPickerDialog.dismiss()
                navigateToEditLabel(labelId, labelName)
            }
            labelPickerDialog.showFullscreen()
        }

        binding.cardDetailFragmentDescription.setOnClickListener {
            descriptionDialog.onCreateClick = { newDesc->
                viewModel.saveCardDescription(newDesc)
            }
            descriptionDialog.setHint("Write card description")
            descriptionDialog.showFullscreen(viewModel.card.value?.cardDesc)
        }
    }

    private fun navigateToCreateLabel(){
        findNavController().navigate(CardDetailFragmentDirections.actionCardDetailFragmentToAddEditLabelFragment(
            args.workspaceId,
            args.boardId,
            args.listId,
            args.cardId,
            "",
            ""
        ))
    }
    private fun navigateToEditLabel(labelId: String, labelName: String){
        findNavController().navigate(CardDetailFragmentDirections.actionCardDetailFragmentToAddEditLabelFragment(
            args.workspaceId,
            args.boardId,
            args.listId,
            args.cardId,
            labelId,
            labelName
        ))
    }



    private fun setupObserver(){
        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }

        safeObserve(viewModel.labels){
            labelPickerDialog.submitList(it)
        }

        safeObserve(viewModel.card){
            bindData(it)
        }
    }

    private fun bindData(card: Card){
        binding.cardDetailTextviewNameCard.text = card.cardName ?: ""
        binding.cardDetailFragmentDescription.text = card.cardDesc ?: ""
    }

}

