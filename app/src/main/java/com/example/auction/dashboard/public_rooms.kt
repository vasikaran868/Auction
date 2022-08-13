package com.example.auction.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.auction.AuctionApplication
import com.example.auction.AuctionViewModel
import com.example.auction.AuctionViewModelFactory
import com.example.auction.R
import com.example.auction.databinding.FragmentPublicRoomsBinding
import kotlinx.coroutines.launch

class public_rooms : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }

    private var _binding: FragmentPublicRoomsBinding? = null
    private val binding get() = _binding!!

    var public_rooms = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentPublicRoomsBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.is_collector_initialised["public_rooms"]==false){
            lifecycleScope.launch {
                viewModel.is_collector_initialised["public_rooms"]= true
                viewModel.shared_msg.collect(){
                    Log.v("MyActivity","from public_rooms: $it")
                    val params = split_to_list(it)
                    if(params[0] =="joined_room"){
                        viewModel.room_no= params[1]
                        val pub_or_pri = params[2]
                        val action=public_roomsDirections.actionPublicRoomsToRoomPage(pub_or_pri)
                        activity?.findNavController(R.id.public_rooms_frag)?.navigate(action)
                    }
                }
            }
        }

        val details = split_to_list(arguments?.get("room_det").toString())
        val adapter = public_rooms_adapter(){
            viewModel.send_to_Server("4 ${viewModel.current_user.username} ${it}")
        }
        for (i in (1..details.size-1)){
            public_rooms .add(details[i])
        }
        binding.pubRoomsRec.layoutManager = GridLayoutManager(context,2)
        binding.pubRoomsRec.adapter = adapter
        adapter.submitList(public_rooms)

        binding.backToDash.setOnClickListener {
            findNavController().navigateUp()
        }



    }
    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())

    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["public_rooms"] = false
    }
}