package com.tnh.mollert.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tnh.mollert.databinding.WorkspaceBoardItemBinding
import com.tnh.mollert.databinding.WorkspaceItemBinding
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.tnhlibrary.logAny

class HomeWorkSpaceAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<Workspace, HomeWorkSpaceAdapter.HomeWorkSpaceViewHolder>(HomeWorkSpaceDiffUtil()) {
    var onNewClicked: ((workspace: Workspace)-> Unit)? = null
    private val boardList: MutableList<List<Board>> = mutableListOf()


    inner class HomeWorkSpaceViewHolder(private var binding: WorkspaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val workspaceBoardAdapter = WorkspaceBoardAdapter()

        fun bind(workspace: Workspace, list: List<Board>?) {
            workspaceBoardAdapter.submitList(list)
            workspaceBoardAdapter.onClick = onClick
            binding.workspaceName = workspace.workspaceName
            binding.workspaceBoardItemBoardList.adapter = workspaceBoardAdapter
            binding.workspaceItemNew.setOnClickListener {
                onNewClicked?.invoke(workspace)
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
        var onClick: ((String) -> Unit)? = null

        inner class WorkspaceBoardViewHolder(private var binding: WorkspaceBoardItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(board: Board) {
                binding.root.setOnClickListener {
                    onClick?.invoke(board.boardId)
                }
                binding.title = board.boardName
                Glide.with(binding.root).load(board.background)
                    .placeholder(ColorDrawable(Color.parseColor("#048ce4")))
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