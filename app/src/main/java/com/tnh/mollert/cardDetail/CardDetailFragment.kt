package com.tnh.mollert.cardDetail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.tnh.mollert.R
import com.tnh.mollert.boardDetail.DescriptionDialog
import com.tnh.mollert.cardDetail.label.LabelChipAdapter
import com.tnh.mollert.cardDetail.label.LabelPickerDialog
import com.tnh.mollert.databinding.CardDetailFragmentBinding
import com.tnh.mollert.databinding.CreateBoardLayoutBinding
import com.tnh.mollert.databinding.CustomMenuItemBinding
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.Attachment
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.utils.LoadingModal
import com.tnh.mollert.utils.bindImageUri
import com.tnh.mollert.utils.dpToPx
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardDetailFragment: DataBindingFragment<CardDetailFragmentBinding>(R.layout.card_detail_fragment) {
    val viewModel by viewModels<CardDetailFragmentViewModel>()
    private val args by navArgs<CardDetailFragmentArgs>()
    private var container: ViewGroup? = null
    private val loadingModal by lazy {
        LoadingModal(requireContext())
    }
    private val labelPickerDialog by lazy(){
        LabelPickerDialog(requireContext(), container)
    }
    private val attachmentDialog by lazy {
        AddAttachmentDialog(requireContext(), container)
    }
    private val activityDialog by lazy {
        ActivityDialog(requireContext(), container)
    }
    private val descriptionDialog by lazy {
        DescriptionDialog(requireContext(), container)
    }
    private val workAdapter by lazy {
        WorkAdapter(AppRepository.getInstance(requireContext()).taskDao)
    }
    private val chipAdapter by lazy {
        LabelChipAdapter()
    }
    private val memberAdapter by lazy {
        MemberAdapter()
    }
    private val commentAdapter by lazy {
        CommentAdapter()
    }
    private val attachmentAdapter by lazy {
        AttachmentAdapter()
    }

    private lateinit var optionMenu: CardPopupMenu
    private val imageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
        if(uri != null){
            viewModel.changeCardCover(args.boardId, requireContext().contentResolver, uri, args.cardId)
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
        optionMenu = CardPopupMenu(requireContext(), binding.cardDetailFragmentToolbar.twoActionToolbarEndIcon)
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
                R.id.card_detail_menu_add_attachment->{
                    showAttachmentDialog()
                }
                R.id.card_detail_menu_add_work->{
                    showCreateWorkDialog()
                }
                R.id.card_detail_menu_join_card->{
                    if(optionMenu.isMemberInCard(viewModel.email)){
                        viewModel.leaveCard(args.boardId, args.cardId)
                    }else{
                        viewModel.joinCard(args.boardId)
                    }
                }
                R.id.card_detail_menu_activity->{
                    activityDialog.setTitle("Card activities")
                    activityDialog.showFullscreen()
                }
                R.id.card_detail_menu_achieved->{
                    if(optionMenu.getCardStatus() == Card.STATUS_ACTIVE){
                        viewModel.achieveCard(args.boardId)
                    }else{
                        viewModel.activateCard(args.boardId)
                    }
                }
                R.id.card_detail_menu_delete->{
                    showDeleteCardDialog()
                }
            }
            true
        }
        optionMenu.showWithMember(viewModel.email)
    }

    private fun showDeleteCardDialog(){
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Do you really want to delete this card?")
            setPositiveButton("DELETE"){_, _->
                loadingModal.show()
                viewModel.deleteThisCard(args.workspaceId, args.boardId, {
                    loadingModal.dismiss()
                }){
                    loadingModal.dismiss()
                    findNavController().navigateUp()
                }
            }
        }.show()
    }

    private fun showAlertDialog(title: String, builder: (AlertDialog.Builder, CreateBoardLayoutBinding)-> Unit){
        AlertDialog.Builder(requireContext()).apply {
            val binding = CreateBoardLayoutBinding.inflate(layoutInflater)
            setTitle(title)
            setView(binding.root)
            builder(this, binding)
        }.show()
    }

    private fun showCreateWorkDialog(){
        showAlertDialog("Add work"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "Work name"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Work name cannot be empty")
                }else{
                    viewModel.addWork(args.boardId, args.cardId, dialogBinding.createBoardLayoutName.text.toString().trim())
                }
            }
        }
    }

    private fun showCreateTaskDialog(workId: String){
        showAlertDialog("Add task"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "Task name"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrBlank()){
                    viewModel.setMessage("Task name cannot be empty")
                }else{
                    viewModel.addTask(args.boardId, workId, dialogBinding.createBoardLayoutName.text.toString().trim())
                }
            }
        }
    }


    private fun showChangeNameDialog(){
        showAlertDialog("Change card name"){ builder, dialogBinding ->
            dialogBinding.createBoardLayoutName.hint = "New name"
            builder.setPositiveButton("OK") { _, _ ->
                if(dialogBinding.createBoardLayoutName.text.isNullOrEmpty()){
                    viewModel.setMessage("Card name cannot be empty")
                }else{
                    viewModel.changeCardName(args.boardId, dialogBinding.createBoardLayoutName.text.toString())
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
                        viewModel.addLinkAttachment(args.boardId, args.cardId, dialogBinding.createBoardLayoutName.text.toString())
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
        attachmentAdapter.onLongClicked = { attachment->
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Do you want to delete this attachment?")
                setPositiveButton("DELETE"){_, _->
                    viewModel.deleteAttachment(args.boardId, attachment)
                }
            }.show()
        }

        binding.cardDetailFragmentCommentRecycler.adapter = commentAdapter

        commentAdapter.onLongClicked = { activity ->
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Do you want to delete this comment?")
                setPositiveButton("DELETE"){_, _->
                    viewModel.deleteActivity(args.workspaceId, args.boardId, activity)
                }
            }.show()
        }

        binding.cardDetailFragmentMemberRecycler.adapter = memberAdapter
        binding.cardDetailFragmentWorkRecycler.adapter = workAdapter

        workAdapter.onAddItemClicked = { workId ->  
            showCreateTaskDialog(workId)
        }  
        
        workAdapter.onDeleteTaskClicked = { task ->
            viewModel.deleteTask(args.boardId, task)
        }

        workAdapter.onDeleteWorkClicked = { work ->
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Do you want to delete this work?")
                setPositiveButton("DELETE"){_, _->
                    viewModel.deleteWork(args.boardId, work)
                }
            }.show()
        }

        workAdapter.onTaskChecked = { task, isChecked ->
            viewModel.onTaskChecked(task, isChecked)
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
                viewModel.saveCardDescription(args.boardId, newDesc)
            }
            descriptionDialog.setHint("Write card description")
            descriptionDialog.showFullscreen(viewModel.card.value?.cardDesc)
        }

        binding.cardDetailFragmentAttachment.setOnClickListener {
            showAttachmentDialog()
        }

        binding.cardDetailFragmentSend.setOnClickListener {
            if(binding.cardDetailFragmentCommentInput.text.isNullOrEmpty().not()){
                viewModel.addComment(args.workspaceId, args.boardId, args.cardId, binding.cardDetailFragmentCommentInput.text.toString())
                binding.cardDetailFragmentCommentInput.setText("")
            }
        }

        binding.cardDetailFragmentDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker().apply {
                setTitleText("Select date")
            }.build()

            datePicker.addOnPositiveButtonClickListener {
                datePicker.selection?.let { pair->
                    viewModel.saveDate(args.boardId, pair.first, pair.second)
                }
            }
            datePicker.show(requireActivity().supportFragmentManager, "datePicker")
        }
        
        binding.cardDetailFragmentDateCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveDateChecked(args.boardId, isChecked)
        }
        binding.cardDetailFragmentCheckedList.setOnClickListener {
            showCreateWorkDialog()
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

        safeObserve(viewModel.memberAndActivity){
            activityDialog.submitList(it)
            if(it.isEmpty()){
                binding.cardDetailFragmentCommentRecycler.gone()
            }else{
                binding.cardDetailFragmentCommentRecycler.show()
                commentAdapter.submitList(it.filter { memberAndActivity->
                    memberAndActivity.activity.activityType == Activity.TYPE_COMMENT
                })
            }
        }

        safeObserve(viewModel.cardWithMembers){
            optionMenu.setCardWithMembers(it)
            if(it.members.isEmpty()){
                binding.cardDetailFragmentMemberRecycler.gone()
            }else{
                binding.cardDetailFragmentMemberRecycler.show()
                memberAdapter.submitList(it.members)
            }
        }

        safeObserve(viewModel.works){
            if(it.isEmpty()){
                binding.cardDetailFragmentWorkRecycler.gone()
            }else{
                binding.cardDetailFragmentWorkRecycler.show()
                workAdapter.submitList(it)
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
        if(card.startDate != 0L && card.dueDate != 0L){
            val spanString = SpannableString("${card.startDate.getDate()} - ${card.dueDate.getDate()}")
            if(card.checked){
                spanString.setSpan(ForegroundColorSpan(Color.GREEN), 0, spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanString.setSpan(StrikethroughSpan(), 0, spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if(System.currentTimeMillis() > card.dueDate){
                spanString.setSpan(ForegroundColorSpan(Color.RED), 0, spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if(card.dueDate - System.currentTimeMillis() <= 86400000){
                spanString.setSpan(ForegroundColorSpan(Color.parseColor("#FFA500")), 0, spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            binding.cardDetailFragmentDateCheckbox.isChecked = card.checked
            binding.cardDetailFragmentDateCheckbox.text = spanString
            binding.cardDetailFragmentDateCheckbox.show()
        }else{
            binding.cardDetailFragmentDateCheckbox.gone()
        }
    }

}

