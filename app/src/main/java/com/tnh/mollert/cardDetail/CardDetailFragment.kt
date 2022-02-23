package com.tnh.mollert.cardDetail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tnh.mollert.R
import com.tnh.mollert.boardDetail.DescriptionDialog
import com.tnh.mollert.cardDetail.label.LabelChipAdapter
import com.tnh.mollert.cardDetail.label.LabelPickerDialog
import com.tnh.mollert.databinding.CardDetailFragmentBinding
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.datasource.local.model.Attachment
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.bindImageUri
import com.tnh.mollert.utils.dpToPx
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.toast.showToast
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import com.tnh.tnhlibrary.view.snackbar.showSnackbar

@AndroidEntryPoint
class CardDetailFragment: DataBindingFragment<CardDetailFragmentBinding>(R.layout.card_detail_fragment) {
    val viewModel by viewModels<CardDetailFragmentViewModel>()
    private val args by navArgs<CardDetailFragmentArgs>()
    private var container: ViewGroup? = null
    private val labelPickerDialog by lazy(){
        LabelPickerDialog(requireContext(), container)
    }
    private val attachmentDialog by lazy {
        AddAttachmentDialog(requireContext(), container)
    }
    private val descriptionDialog by lazy {
        DescriptionDialog(requireContext(), container)
    }
    private val chipAdapter by lazy {
        LabelChipAdapter()
    }
    private val attachmentAdapter by lazy {
        AttachmentAdapter()
    }

    private val optionMenu by lazy {
        CardPopupMenu(requireContext(), binding.cardDetailFragmentToolbar.twoActionToolbarEndIcon)
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
        if(uri != null){
            viewModel.changeCardCover(requireContext().contentResolver, uri, args.cardId)
        }
    }

    private val imageAttachmentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
        if(uri != null){
            viewModel.addImageAttachment(requireContext().contentResolver, uri, args.boardId, args.cardId)
        }
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

    private fun setupToolbar(hasCover: Boolean = false){
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
            if(hasCover){
                twoActionToolbarStartIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                twoActionToolbarEndIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                root.background = ColorDrawable(Color.parseColor("#33000000"))
            }else{
                twoActionToolbarStartIcon.imageTintList = ColorStateList.valueOf(Color.BLACK)
                twoActionToolbarEndIcon.imageTintList = ColorStateList.valueOf(Color.BLACK)
                root.background = ColorDrawable(Color.WHITE)
            }
        }
    }

    private fun showOptionMenu(){
        optionMenu.setOnMenuItemClickListener { item->
            when(item.itemId){
                R.id.card_detail_menu_change_name->{
                    showChangeNameDialog()
                }
                R.id.card_detail_menu_change_cover->{
                    imageLauncher.launch(arrayOf("image/*"))
                }
                R.id.card_detail_menu_add_attachemnt->{
                    showAttachmentDialog()
                }
            }
            true
        }
        optionMenu.show()
    }

    private fun showAlertDialog(title: String, builder: (AlertDialog.Builder, CreateBoardLayoutBinding)-> Unit){
        AlertDialog.Builder(requireContext()).apply {
            val binding = CreateBoardLayoutBinding.inflate(layoutInflater)
            setTitle(title)
            setView(binding.root)
            builder(this, binding)
        }.show()
    }

    private fun showChangeNameDialog(){
        showAlertDialog("Change card name"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "New name"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("Card name cannot be empty")
                }else{
                    viewModel.changeCardName(dialogBinding.createBoardLayoutName.text.toString())
                }
            }
        }
    }

    private fun showLinkAttachmentDialog(){
        showAlertDialog("Add link"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "Link"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("Link cannot be empty")
                }else{
                    if(Patterns.WEB_URL.matcher(dialogBinding.createBoardLayoutName.text.toString()).matches()){
                        viewModel.addLinkAttachment(args.cardId, dialogBinding.createBoardLayoutName.text.toString())
                    }else{
                        viewModel.postMessage("Not an url")
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardDetailFragmentAttachmentRecycler.adapter = attachmentAdapter
        attachmentAdapter.onItemClicked = { attachment->
            when(attachment.type){
                Attachment.TYPE_LINK->{
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(attachment.linkRemote)
                        startActivity(intent)
                    }catch (e: Exception){
                        trace(e)
                        viewModel.postMessage("Unable to open link")
                    }
                }
            }
        }

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
            labelPickerDialog.onApplyLabelClicked = {
                viewModel.applyLabelsToCard(args.workspaceId, args.boardId, it)
            }
            lifecycleScope.launchWhenResumed {
                labelPickerDialog.setSelectedList(viewModel.getCardWithLabels(args.cardId).labels)
                labelPickerDialog.showFullscreen()
            }
        }

        binding.cardDetailFragmentDescription.setOnClickListener {
            descriptionDialog.onCreateClick = { newDesc->
                viewModel.saveCardDescription(newDesc)
            }
            descriptionDialog.setHint("Write card description")
            descriptionDialog.showFullscreen(viewModel.card.value?.cardDesc)
        }

        binding.cardDetailFragmentAttachment.setOnClickListener {
            showAttachmentDialog()
        }
    }

    private fun showAttachmentDialog(){
        attachmentDialog.onCloseClicked = {
            attachmentDialog.dismiss()
        }
        attachmentDialog.onImageClicked = {
            attachmentDialog.dismiss()
            imageAttachmentLauncher.launch(arrayOf("image/*"))
        }
        attachmentDialog.onLinkClicked = {
            attachmentDialog.dismiss()
            showLinkAttachmentDialog()
        }
        attachmentDialog.show()
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
            requireActivity().showSnackbar(it)
        }

        safeObserve(viewModel.labels){
            labelPickerDialog.submitList(it)
        }

        safeObserve(viewModel.card){
            bindData(it)
        }

        safeObserve(viewModel.cardWithLabels){
            if(it.labels.isEmpty()){
                binding.cardDetailFragmentLabelRecycler.gone()
            }else{
                binding.cardDetailFragmentLabelRecycler.show()
                binding.cardDetailFragmentLabelRecycler.adapter = chipAdapter
                chipAdapter.submitList(it.labels)
            }
        }

        safeObserve(viewModel.attachments){
            if(it.isEmpty()){
                binding.cardDetailFragmentAttachmentRecycler.gone()
            }else{
                binding.cardDetailFragmentAttachmentRecycler.show()
                attachmentAdapter.submitList(it)
            }
        }
    }

    private fun bindData(card: Card){
        binding.cardDetailTextviewNameCard.text = card.cardName
        binding.cardDetailFragmentDescription.text = card.cardDesc ?: ""
        if(card.cover.isNotEmpty()){
            setupToolbar(true)
            binding.cardDetailTextviewNameCard.setTextColor(Color.WHITE)
            binding.cardDetailTextviewNameCard.background = ColorDrawable(Color.parseColor("#33000000"))
            binding.cardDetailFragmentCover.show()
            binding.cardDetailFragmentCover.bindImageUri(card.cover)
            binding.cardDetailTextviewNameCard.height = 200.dpToPx
        }else{
            setupToolbar(false)
            binding.cardDetailTextviewNameCard.setTextColor(Color.BLACK)
            binding.cardDetailFragmentCover.gone()
            binding.cardDetailTextviewNameCard.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.cardDetailTextviewNameCard.background = ColorDrawable(Color.WHITE)
        }
    }

}

