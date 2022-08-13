package com.example.auction.dashboard

import android.content.ClipData
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.auction.databinding.RoomMemberBinding

class Room_members_adapter (val is_admin:Boolean,private val remove_but_clicked:(String)->Unit): ListAdapter<String, Room_members_adapter.Room_memberViewHolder>(DiffCallback){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Room_members_adapter.Room_memberViewHolder {
        return Room_memberViewHolder(
            RoomMemberBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: Room_members_adapter.Room_memberViewHolder, position: Int) {
        val name = getItem(position)
        holder.bind(name = name)
        if (is_admin){
            holder.show_remove_button()
        }
        holder.remove_button.setOnClickListener {
            remove_but_clicked(name)
        }

    }

    class Room_memberViewHolder(private var binding: RoomMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        val remove_button = binding.removeButton

        fun bind(name:String){
            binding.usernameTextView.text = name
        }
        fun show_remove_button(){
            binding.removeButton.visibility= View.VISIBLE
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem.length === newItem.length
            }

            override fun areContentsTheSame(oldItem:String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}