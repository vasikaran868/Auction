package com.example.auction.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.auction.AuctionApplication
import com.example.auction.AuctionViewModel
import com.example.auction.AuctionViewModelFactory
import com.example.auction.R
import com.example.auction.databinding.FragmentPlayWithFriendsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class play_with_friends : DialogFragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }
    private var _binding: FragmentPlayWithFriendsBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentPlayWithFriendsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.joinRoomButton.setOnClickListener {
            if(binding.RoomNoInput.text.isNullOrBlank()){
                binding.RoomNoInput.setError("Cannot Be Blank")
            }
            else {
                try {
                    val code= binding.RoomNoInput.text.toString().toInt()
                    val msg = "4 ${viewModel.current_user.username} ${code}"
                    viewModel.send_to_Server(msg)
                }catch (e:Exception){
                    binding.RoomNoInput.setError("Enter Valid Input")
                }
            }

        }
    }
    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())
}