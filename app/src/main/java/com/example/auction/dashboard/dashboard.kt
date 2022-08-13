package com.example.auction.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auction.*
import com.example.auction.databinding.FragmentDashboardBinding
import com.example.auction.login.login_fragmentDirections
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.zip.Inflater


class dashboard : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }

    private var _binding:FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    val acc_det = account_details()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("MyActivity","dashboard frag created")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentDashboardBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.is_collector_initialised["dashboard"]==false){
            lifecycleScope.launch {
                viewModel.is_collector_initialised["dashboard"]= true
                viewModel.shared_msg.collect(){
                    Log.v("MyActivity","from dashboard: $it")
                    val params = split_to_list(it)
                    if (params[0]=="admin=true"){
                        viewModel.is_room_admin= true
                    }
                    else if (params[0]=="createdroom"){
                        viewModel.room_no= params[1]
                        viewModel.room_members.add(viewModel.current_user.username)
                        val action = dashboardDirections.actionDashboardToRoomPage("private")
                        activity?.findNavController(R.id.dashboard_page)?.navigate(action)
                    }
                    else if(params[0] == "joined_public_room"){
                        viewModel.room_no= params[1]
                        val action=dashboardDirections.actionDashboardToRoomPage("public")
                        activity?.findNavController(R.id.dashboard_page)?.navigate(action)
                    }
                    else if(params[0] =="users:"){
                        viewModel.room_no= params[1]
                        Log.v("MyActivity","from dashboard: $it")
                        for (i in 2..(params.size-1)){
                            if(!(viewModel.room_members.contains(params[i]))){
                                viewModel.room_members.add(params[i])
                            }
                        }
                    }
                    else if(params[0] =="joined_room"){
                        if (viewModel.is_collector_initialised["public_rooms"]== false){
                            viewModel.room_no= params[1]
                            val pub_or_pri = params[2]
                            val action=dashboardDirections.actionDashboardToRoomPage(pub_or_pri)
                            activity?.findNavController(R.id.dashboard_page)?.navigate(action)
                        }
                    }
                    else if (params[0] =="changed_username"){
                        acc_det.username_changed(params[1])
                        viewModel.current_user.username = params[1]
                        viewModel.change_datastore_values(params[1],viewModel.current_user.password,requireContext())
                    }
                    else if (params[0] =="changed_password"){
                        acc_det.password_changed(params[1])
                        viewModel.current_user.password = params[1]
                        viewModel.change_datastore_values(viewModel.current_user.username,params[1],requireContext())
                    }
                    else if (params[0] =="changed_email"){
                        acc_det.email_changed(params[1])
                        viewModel.current_user.email = params[1]
                    }
                    else if (params[0]=="pub_room"){
                        val action = dashboardDirections.actionDashboardToPublicRooms(it)
                        activity?.findNavController(R.id.dashboard_page)?.navigate(action)
                    }
                }
            }
        }
        binding.apply {
            matchPlayedTv.text = viewModel.current_user.match_played.toString()
            matchWonTv.text = viewModel.current_user.match_won.toString()
            avgPointsTv.text = viewModel.current_user.avg_points.toString()
            dashUsername.text = viewModel.current_user.username
            dashXpLvlTv.text = "lvl.${(viewModel.current_user.xp/100).toInt()+1}"
            dashXpPb.max = 100
            dashXpPb.setProgress(viewModel.current_user.xp%100)
        }

        binding.createRoomButton.setOnClickListener {
            val msg = "3 ${viewModel.current_user.username}"
            viewModel.send_to_Server(msg)

        }
        binding.accountButton.setOnClickListener {
            acc_det.show(childFragmentManager,"acc_det")
        }
        binding.settingsButton.setOnClickListener {
            Toast.makeText(context, "No Settings Available...Will be updated soon", Toast.LENGTH_LONG).show()
        }
        binding.playWithFrndsButton.setOnClickListener {
            //val action = dashboardDirections.actionDashboardToPlayWithFriends()
            //this.findNavController().navigate(action)
            val dialog_obj = play_with_friends()
            dialog_obj.show(childFragmentManager,"dialog_obj")
        }
        binding.playOnlineButton.setOnClickListener {
            viewModel.send_to_Server("18")
        }
        binding.signoutButton.setOnClickListener {
            viewModel.change_datastore_values("","",requireContext())
            val action = dashboardDirections.actionDashboardToLoginFragment()
            findNavController().navigate(action)
        }
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val back_dialog_frag = back_pressed_dialog("Do you want to exit the game?","EXIT"){
                    findNavController().navigateUp()
                }
                back_dialog_frag.show(childFragmentManager,"back_dialog_fragment")
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["dashboard"]= false
        Log.v("MyActivity","dashboard destroyed")
    }

    fun send_to_server(message:String){
        viewModel.send_to_Server(message)
    }

    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())
}