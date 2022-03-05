package com.tnh.mollert.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tnh.mollert.R
import com.tnh.mollert.databinding.WorkspaceBoardItemBinding
import com.tnh.mollert.databinding.WorkspaceItemBinding
import com.tnh.mollert.datasource.local.dao.MemberWorkspaceDao
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import kotlinx.coroutines.*

class HomeWorkSpaceAdapter(
    private val onClick: (workspaceId: String, boardId: String, boardName: String) -> Unit,
    private val memberWorkspaceDao: MemberWorkspaceDao
) : ListAdapter<Workspace, HomeWorkSpaceAdapter.HomeWorkSpaceViewHolder>(HomeWorkSpaceDiffUtil()) {
    var email = UserWrapper.getInstance()?.currentUserEmail ?: ""
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    var onNewClicked: ((workspace: Workspace)-> Unit)? = null
    var onSettingClicked: ((workspace: Workspace)-> Unit)? = null
    var onInviteClicked: ((workspace: Workspace)-> Unit)? = null

    private val boardList: MutableList<List<Board>> = mutableListOf()


    inner class HomeWorkSpaceViewHolder(private var binding: WorkspaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val workspaceBoardAdapter = WorkspaceBoardAdapter()
        var memberWorkspaceRel: MemberWorkspaceRel? = null

        fun bindSetting(){
            if(email == memberWorkspaceRel!!.email && (memberWorkspaceRel!!.role == MemberWorkspaceRel.ROLE_LEADER || memberWorkspaceRel!!.role == MemberBoardRel.ROLE_OWNER)){
                binding.workspaceItemSetting.show()
                binding.workspaceItemInvite.gone()
            }else{
                binding.workspaceItemSetting.gone()
                binding.workspaceItemInvite.show()
            }
        }

        fun bind(workspace: Workspace, list: List<Board>?) {
            workspaceBoardAdapter.submitList(list)
            workspaceBoardAdapter.workspaceId = workspace.workspaceId
            workspaceBoardAdapter.onClick = onClick
            binding.workspaceName = workspace.workspaceName
            binding.workspaceBoardItemBoardList.adapter = workspaceBoardAdapter
            binding.workspaceItemNew.setOnClickListener {
                onNewClicked?.invoke(workspace)
            }
            binding.workspaceItemSetting.setOnClickListener {
                onSettingClicked?.invoke(workspace)
            }
            if(memberWorkspaceRel == null){
                scope.launch {
                    withContext(Dispatchers.IO){
                        memberWorkspaceRel = memberWorkspaceDao.getWorkspaceLeader(workspace.workspaceId)
                    }
                    bindSetting()
                }
            }else{
                bindSetting()
            }

            binding.workspaceItemInvite.setOnClickListener {
                onInviteClicked?.invoke(workspace)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeWorkSpaceViewHolder {
        return HomeWorkSpaceViewHolder(
            WorkspaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HomeWorkSpaceViewHolder, position: Int) {
        holder.bind(getItem(position), boardList.getOrNull(position))
    }

    fun submitBoardList(list: List<List<Board>>){
        boardList.clear()
        boardList.addAll(list)
        this.notifyDataSetChanged()
    }

    class HomeWorkSpaceDiffUtil : DiffUtil.ItemCallback<Workspace>() {
        override fun areItemsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
            return oldItem.workspaceId == newItem.workspaceId
        }
    }


    class WorkspaceBoardAdapter() :
        ListAdapter<Board, WorkspaceBoardAdapter.WorkspaceBoardViewHolder>(HomeWorkSpaceDiffUtil()) {
        var workspaceId: String = ""
        var onClick: ((String, String, boardName: String) -> Unit)? = null

        inner class WorkspaceBoardViewHolder(private var binding: WorkspaceBoardItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(board: Board) {
                binding.root.setOnClickListener {
                    onClick?.invoke(workspaceId, board.boardId, board.boardName)
                }
                binding.title = board.boardName
                Glide.with(binding.root).load(board.background)
                    .placeholder(R.drawable.asset_3)
                    .into(binding.workspaceBoardItemImage)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): WorkspaceBoardViewHolder {
            return WorkspaceBoardViewHolder(
                WorkspaceBoardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: WorkspaceBoardViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class HomeWorkSpaceDiffUtil : DiffUtil.ItemCallback<Board>() {
            override fun areItemsTheSame(oldItem: Board, newItem: Board): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Board, newItem: Board): Boolean {
                return oldItem.boardId == newItem.boardId
            }
        }

    }
}