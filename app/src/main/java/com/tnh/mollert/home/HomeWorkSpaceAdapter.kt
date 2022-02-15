package com.tnh.mollert.home

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

class HomeWorkSpaceAdapter(
    private val onClick: (String) -> Unit,
    private val getBoardList: (String) -> List<Board>
) : ListAdapter<Workspace, HomeWorkSpaceAdapter.HomeWorkSpaceViewHolder>(HomeWorkSpaceDiffUtil()) {

    inner class HomeWorkSpaceViewHolder(private var binding: WorkspaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val workspaceBoardAdapter = WorkspaceBoardAdapter()

        fun bind(workspace: Workspace) {
            workspaceBoardAdapter.submitList(getBoardList(workspace.workspaceId))
            binding.workspaceName = workspace.workspaceName
            binding.workspaceBoardItemBoardList.adapter = workspaceBoardAdapter
            binding.workspaceItemCard.setOnClickListener { onClick(workspace.workspaceId) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeWorkSpaceViewHolder {
        return HomeWorkSpaceViewHolder(
            WorkspaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HomeWorkSpaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HomeWorkSpaceDiffUtil : DiffUtil.ItemCallback<Workspace>() {
        override fun areItemsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
            return oldItem.workspaceId == newItem.workspaceId
        }
    }


    class WorkspaceBoardAdapter :
        ListAdapter<Board, WorkspaceBoardAdapter.WorkspaceBoardViewHolder>(HomeWorkSpaceDiffUtil()) {
        class WorkspaceBoardViewHolder(private var binding: WorkspaceBoardItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            companion object {
                fun from(parent: ViewGroup): WorkspaceBoardViewHolder {
                    return WorkspaceBoardViewHolder(
                        WorkspaceBoardItemBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
            }

            fun bind(board: Board) {
                binding.title = board.boardName
                Glide.with(binding.root).load(board.background)
                    .into(binding.workspaceBoardItemImage)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): WorkspaceBoardViewHolder {
            return WorkspaceBoardViewHolder.from(parent)
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