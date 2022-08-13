package com.example.auction.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.auction.dashboard.Room_members_adapter
import com.example.auction.databinding.PlayerViewholderBinding
import com.example.auction.databinding.RoomMemberBinding

class Game_all_players_rec_adapter():ListAdapter<String,Game_all_players_rec_adapter.PlayerViewHolder>(
    DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        return PlayerViewHolder(
            PlayerViewholderBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player_name = getItem(position)
        holder.bind(player_name)
    }

    class PlayerViewHolder(private var binding: PlayerViewholderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(name:String){
            binding.playerNameVh.text = name
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