package com.tnh.mollert.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tnh.mollert.databinding.WorkspaceItemBinding
import com.tnh.mollert.datasource.local.model.Board

class HomeWorkSpaceAdapter :
    ListAdapter<Board, HomeWorkSpaceAdapter.HomeWorkSpaceViewHolder>(HomeWorkSpaceDiffUtil()) {
    class HomeWorkSpaceViewHolder(private var binding: WorkspaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HomeWorkSpaceViewHolder {
                return HomeWorkSpaceViewHolder(
                    WorkspaceItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        fun bind(board: Board) {
            binding.title = board.boardName
            Glide.with(binding.root).load(board.background).into(binding.workspaceItemImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeWorkSpaceViewHolder {
        return HomeWorkSpaceViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HomeWorkSpaceViewHolder, position: Int) {
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