package com.example.auction.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.auction.databinding.PublicRoomViewHolderBinding
import com.example.auction.databinding.RoomMemberBinding

class public_rooms_adapter(private val join_room:(String)->Unit): ListAdapter<String, public_rooms_adapter.Public_rooms_ViewHolder>(DiffCallback) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): public_rooms_adapter.Public_rooms_ViewHolder {
        return Public_rooms_ViewHolder(
            PublicRoomViewHolderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: Public_rooms_ViewHolder, position: Int) {
        val details = getItem(position)
        val params = split_to_list(details)
        holder.bind(params[0],params[1])
        holder.join_but.setOnClickListener {
            join_room(params[0])
        }

    }

    class Public_rooms_ViewHolder(private var binding: PublicRoomViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val join_but = binding.joinButton

        fun bind(room_no: String,members:String) {
            binding.roomNoTv.text = room_no
            binding.membersTv.text = "Members: $members"
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem.length === newItem.length
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
    fun split_to_list(text:String):List<String> = text.trim().split(":")

}