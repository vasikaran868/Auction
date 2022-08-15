package com.example.auction.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.auction.*
import com.example.auction.databinding.FragmentRoomPageBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


class room_page : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }

    lateinit var start_msg_time :OffsetDateTime

    private var _binding: FragmentRoomPageBinding?= null
    val binding get() = _binding!!

    lateinit var room_type:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentRoomPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adapter = Room_members_adapter(viewModel.is_room_admin){
            viewModel.send_to_Server("7 ${viewModel.current_user.username} ${viewModel.room_no} $it")
        }
        binding.roomNoTextView.text = viewModel.room_no
        binding.roomMembersRec.layoutManager= GridLayoutManager(this.context,2)
        binding.roomMembersRec.adapter= adapter
        adapter.submitList(viewModel.room_members.toList())
        arguments.let {
            room_type = it?.getString("room_type")!!
        }
        binding.privPublButton.text = room_type
        binding.leaveRoom.setOnClickListener {
            val msg = "7 ${viewModel.current_user.username} ${viewModel.room_no} ${viewModel.current_user.username}"
            viewModel.send_to_Server(msg)
        }
        binding.privPublButton.setOnClickListener {
            if (room_type=="private"){
                val msg = "5 ${viewModel.room_no}"
                viewModel.send_to_Server(msg)
            }
            else if (room_type=="public"){
                val msg = "6 ${viewModel.room_no}"
                viewModel.send_to_Server(msg)
            }
        }
        binding.startGame.setOnClickListener {
            Log.v("MyActivity","but press on ${OffsetTime.now(ZoneOffset.UTC)}")
            binding.loadingLayout.visibility = View.VISIBLE
            binding.privPublButton.isEnabled = false
            binding.leaveRoom.isEnabled = false
            binding.startGame.isEnabled = false
            val msg = "8 ${viewModel.room_no}"
            viewModel.send_to_Server(msg)
            /*if (viewModel.room_members.size<4){
                Toast.makeText(requireContext(), "need minimum 4 members to start the game", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.v("MyActivity","but press on ${OffsetTime.now(ZoneOffset.UTC)}")
                val msg = "8 ${viewModel.room_no}"
                viewModel.send_to_Server(msg)
            }*/
        }
        if ((room_type=="private") and viewModel.is_room_admin){
            binding.privPublButton.visibility= View.VISIBLE
            binding.startGame.visibility = View.VISIBLE
        }
        if (viewModel.is_collector_initialised["room"]==false){
            lifecycleScope.launch {
                viewModel.is_collector_initialised["room"]= true
                viewModel.shared_msg.collect(){
                    Log.v("MyActivity","from room: $it")
                    val params = split_to_list(it)
                    if (params[0]=="adding_user:"){
                        viewModel.room_members.add(params[1])
                        adapter.submitList(viewModel.room_members.toList())
                    }
                    else if (it == "admin=true"){
                        viewModel.is_room_admin = true
                        adapter = Room_members_adapter(viewModel.is_room_admin){
                            viewModel.send_to_Server("7 $it ${viewModel.room_no} 0")
                        }
                        binding.roomMembersRec.adapter= adapter
                        adapter.submitList(viewModel.room_members.toList())
                    }
                    else if (params[0] =="left_room"){
                        //val action = room_pageDirections.actionRoomPageToDashboard()
                        Toast.makeText(context, "left the room", Toast.LENGTH_SHORT).show()
                        activity?.findNavController(R.id.room_page)?.navigateUp()
                        viewModel.room_no="0"
                        viewModel.room_members.clear()
                        viewModel.is_room_admin=false
                    }
                    else if(params[0] =="deleting_user:"){
                        viewModel.room_members.remove(params[1])
                        adapter.submitList(viewModel.room_members.toList())
                    }
                    else if (params[0] =="removed_from_room"){
                        //val action = room_pageDirections.actionRoomPageToDashboard()
                        Toast.makeText(context, "kicked out of room", Toast.LENGTH_SHORT).show()
                        activity?.findNavController(R.id.room_page)?.navigateUp()
                        viewModel.room_no="0"
                        viewModel.room_members.clear()
                        viewModel.is_room_admin=false
                    }
                    else if (params[0] =="changed_to_public_room"){
                        room_type="public"
                        binding.privPublButton.text="public"
                    }
                    else if (params[0] =="changed_to_private_room"){
                        room_type="private"
                        binding.privPublButton.text="private"
                    }
                    else if (params[0] =="players:"){
                        viewModel.temp_players_list = it.removePrefix("players: [").removeSuffix("]").split(",").toMutableList()
                    }
                    else if(params[0] =="players_image_list:"){
                        for (i in 1..(params.size -1) step 2){
                            viewModel.players_image_uri_map[params[i]] = params[i+1]
                        }
                    }
                    else if(params[0] =="game_start:"){
                        val time_diff_msg = "11"
                        start_msg_time= OffsetDateTime.now(ZoneOffset.UTC)
                        viewModel.send_to_Server(time_diff_msg)
                        Log.v("MyActivity","start msg $start_msg_time")
                        viewModel.wallet_money = params[1].toFloat()
                        viewModel.game_start_sec = params[2]
                    }
                    else if (params[0] =="time"){
                        val receive_time = OffsetDateTime.now(ZoneOffset.UTC)
                        Log.v("MyActivity","receive msg $receive_time")
                        val send_mili = (start_msg_time.until(receive_time, ChronoUnit.MILLIS))/2
                        Log.v("MyActivity","sent mili $send_mili")
                        val server_time = OffsetDateTime.parse(params[1])
                        viewModel.server_client_mili_diff = (start_msg_time.until(server_time,ChronoUnit.MILLIS)) - send_mili
                        Log.v("MyActivity","diff ${viewModel.server_client_mili_diff}")
                        viewModel.split_players_details()
                        Log.v("MyActivity","${viewModel.players_list}")
                    }
                    else if (params[0] == "connect_to_game_server"){
                        val req = mapOf("REQUEST" to "join_game_room","USERNAME" to viewModel.current_user.username,"ROOM_NO" to viewModel.room_no)
                        val enc_req = Json.encodeToString(req)
                        viewModel.send_udp_server(enc_req)
                    }
                    else if (params[0]=="joined_game_room"){
                        val action = room_pageDirections.actionRoomPageToGamePage()
                        activity?.findNavController(R.id.room_page)?.navigate(action)
                        binding.loadingLayout.visibility = View.INVISIBLE
                    }
                }
            }
        }
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val back_dialog_frag = back_pressed_dialog("Do you want to leave the room?","LEAVE"){
                    val msg = "7 ${viewModel.current_user.username} ${viewModel.room_no} ${viewModel.current_user.username}"
                    viewModel.send_to_Server(msg)
                    findNavController().navigateUp()
                }
                back_dialog_frag.show(childFragmentManager,"back_dialog_fragment")
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["room"]=false
        Log.v("MyActivity","room fragment destroyed")
    }



    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())


}