package com.example.auction.game

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auction.*
import com.example.auction.databinding.FragmentGamePageBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*


class game_page : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }
    private val gameviewmodel = GameViewmodel()
    private var _binding:FragmentGamePageBinding? = null
    val binding get() = _binding!!
    lateinit var current_player :Map<String,String>
    var points_table_name_views = mutableListOf<TextView>()
    var points_table_points_views = mutableListOf<TextView>()
    var points_table_balance_views = mutableListOf<TextView>()
    var game_over_points_table_name_views = mutableListOf<TextView>()
    var game_over_points_table_points_views = mutableListOf<TextView>()
    var game_over_points_table_balance_views = mutableListOf<TextView>()
    var cbidder_image_views = mutableMapOf<String,ImageView>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGamePageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameviewmodel.initialise_role_req()
        gameviewmodel.initialise_balance_and_points(viewModel.room_members)
        gameviewmodel.update_players_list(viewModel.players_list)
        gameviewmodel.update_start_time(OffsetDateTime.parse(viewModel.game_start_sec))
        gameviewmodel.server_client_mili_difference = viewModel.server_client_mili_diff
        var current_image_view = "1"
        val bid_timer_tv = binding.timer
        val starts_in_timer_tv = binding.startsInTimer
        val batsmen_list= mutableListOf<String>()
        val bowlers_list = mutableListOf<String>()
        val allrounder_list = mutableListOf<String>()
        val keeper_list = mutableListOf<String>()
        val bat_adapter = Game_all_players_rec_adapter()
        val ball_adapter = Game_all_players_rec_adapter()
        val all_adapter = Game_all_players_rec_adapter()
        val wk_adapter = Game_all_players_rec_adapter()
        binding.recBatsmen.layoutManager= LinearLayoutManager(this.context)
        binding.recBowler.layoutManager= LinearLayoutManager(this.context)
        binding.recAllrounder.layoutManager= LinearLayoutManager(this.context)
        binding.recWicketKeeper.layoutManager= LinearLayoutManager(this.context)
        for (player in viewModel.players_list){
            if (player["role"] == "BAT"){
                batsmen_list.add(player["name"]!!)
            }
            else if (player["role"]=="BOWL"){
                bowlers_list.add(player["name"]!!)
            }
            else if (player["role"]=="ALL"){
                allrounder_list.add(player["name"]!!)
            }
            else if (player["role"] =="WK"){
                keeper_list.add(player["name"]!!)
            }
        }
        binding.recBatsmen.adapter= bat_adapter
        binding.recBowler.adapter = ball_adapter
        binding.recAllrounder.adapter = all_adapter
        binding.recWicketKeeper.adapter = wk_adapter
        bat_adapter.submitList(batsmen_list)
        ball_adapter.submitList(bowlers_list)
        all_adapter.submitList(allrounder_list)
        wk_adapter.submitList(keeper_list)
        Picasso.get().load(viewModel.players_image_uri_map[gameviewmodel.players_list[gameviewmodel.player_no]["image_uri_key"]]).into(binding.playerImage)
        //Picasso.get().load(gameviewmodel.players_list[gameviewmodel.player_no + 1]["image_uri_key"]).into(binding.playerImage)
        gameviewmodel.game_start_timer_milis.observe(viewLifecycleOwner,{
            starts_in_timer_tv.text = it
        })
        gameviewmodel.game_starts_layout_invisibility.observe(viewLifecycleOwner,{
            if (it == true){
                binding.startingTimerLayout.visibility = View.INVISIBLE
                binding.darkBgLayout.visibility = View.GONE
                start_game()
                viewModel.current_user.match_played += 1
                binding.playerImage.visibility = View.VISIBLE
            }
        })
        gameviewmodel.bid_timer_milis_left.observe(viewLifecycleOwner,{
            bid_timer_tv.text = it
            if (it == "0.0" && gameviewmodel.bid_timer_over_msg_received == false){
                binding.bidButton.isEnabled = false
            }
        })
        gameviewmodel.current_player.observe(viewLifecycleOwner,{
            current_player = it
            binding.apply {
                playerNo.text = "Player number: ${gameviewmodel.player_no + 1}"
                playerName.text = current_player["name"]
                playerCategory.text = current_player["role"]
                playerBasePrice.text = current_player["base"]
                playerPoints.text = current_player["points"]
                currentBid.text = "-"
                bidButton.isEnabled = true
            }
            if (current_image_view == "1"){
                Log.v("MyActivity","loading uri to image view2 ...${viewModel.players_image_uri_map[gameviewmodel.players_list[gameviewmodel.player_no + 1]["image_uri_key"]]}")
                if (gameviewmodel.player_no +1 < gameviewmodel.players_list.size){
                    Picasso.get().load(viewModel.players_image_uri_map[gameviewmodel.players_list[gameviewmodel.player_no + 1]["image_uri_key"]]).into(binding.playerImage2)

                }
                binding.playerImage.visibility = View.VISIBLE
                binding.playerImage2.visibility = View.INVISIBLE
                current_image_view = "2"
            }
            else if (current_image_view == "2"){
                if (gameviewmodel.player_no +1 < gameviewmodel.players_list.size){
                    Picasso.get().load(viewModel.players_image_uri_map[gameviewmodel.players_list[gameviewmodel.player_no + 1]["image_uri_key"]]).into(binding.playerImage)
                }
                binding.playerImage2.visibility = View.VISIBLE
                binding.playerImage.visibility = View.INVISIBLE
                current_image_view = "1"
            }
        })
        gameviewmodel.game_start_timer_page()


        lifecycleScope.launch {
            viewModel.shared_msg.collect(){
                val response = Json.decodeFromString<Map<String,String>>(it)
                Log.v("MyActivity","from game page: $response")
                if(response["REQUEST"] == "bid_time_over"){
                    gameviewmodel.bid_timer_over_msg_received = true
                    if (response["STATUS"]=="SOLD"){
                        gameviewmodel.update_balance_points(response["TEAM"]!!,response["CURRENT_BID"]!!.toFloat())
                        if (response["TEAM"]==viewModel.current_user.username){
                            binding.points.text = gameviewmodel.each_members_points[response["TEAM"]].toString()
                            binding.balance.text = gameviewmodel.each_members_balance[response["TEAM"]].toString()
                            gameviewmodel.update_role_req(current_player["role"]!!)
                            gameviewmodel.update_role_req(current_player["ind/for"]!!)
                            update_minimum_req_view()
                        }
                    }
                    if(gameviewmodel.player_no < (gameviewmodel.total_players-1)){
                        gameviewmodel.next_player()
                        gameviewmodel.update_bid_timer(OffsetDateTime.parse(response["NEXT_BID_END_TIME"]))
                    }
                    else{
                        binding.bidButton.isEnabled = false
                    }
                    cbidder_image_views[gameviewmodel.current_bidder]?.visibility = View.INVISIBLE
                    gameviewmodel.current_bidder = null
                    gameviewmodel.update_points_table_list()
                    update_points_table_view()
                }
                if (response["REQUEST"] == "suc_bidded"){
                    gameviewmodel.update_bid_timer(OffsetDateTime.parse(response["BID_END_TIME"]))
                    val bid = response["CURRENT_BID"]!!.toFloat()
                    gameviewmodel.update_current_bid(bid)
                    if (bid < 1F){
                        binding.currentBid.text = "${bid * 100}L"
                    }
                    else{
                        binding.currentBid.text = "${bid}C"
                    }
                    if (gameviewmodel.current_bidder != null){
                        cbidder_image_views[gameviewmodel.current_bidder]?.visibility = View.INVISIBLE
                    }
                    gameviewmodel.current_bidder = response["USERNAME"]
                    cbidder_image_views[gameviewmodel.current_bidder]?.visibility = View.VISIBLE

                }
                if (response["REQUEST"] == "game_over"){
                    Log.v("MyActivity","game_over msg detected")
                    val role_req_list = comma_split_to_list(response["TEAM_STATUS"]!!).dropLast(1)
                    Log.v("MyActivity","${role_req_list}")
                    for (i in role_req_list){
                        val temp_params = split_to_list(i)
                        Log.v("MyActivity","${temp_params}")
                        val is_player_qualified = gameviewmodel.check_if_qualified(temp_params[1].toInt(),temp_params[2].toInt(),temp_params[3].toInt(),temp_params[4].toInt(),temp_params[5].toInt(),temp_params[6].toInt())
                        Log.v("MyActivity","${is_player_qualified}")
                        gameviewmodel.each_members_is_qualified[temp_params[0]] = is_player_qualified
                        Log.v("MyActivity","each_member ...${gameviewmodel.each_members_is_qualified[temp_params[0]]}")
                    }
                    Log.v("MyActivity","each_member ...${gameviewmodel.each_members_is_qualified}")
                    gameviewmodel.rearrange_disqualified()
                    show_game_over_points_table()
                }
            }
        }
        binding.bidButton.setOnClickListener {
            val bid_amt = gameviewmodel.get_bid_amount()
            val req = mapOf("REQUEST" to "bidded","USERNAME" to viewModel.current_user.username,"ROOM_NO" to viewModel.room_no,"PLAYER_NAME" to current_player["name"],"BID" to bid_amt.toString())
            val enc_req = Json.encodeToString(req)
            viewModel.send_udp_server(enc_req)
        }
        binding.gameOverExit.setOnClickListener {
            val action = game_pageDirections.actionGamePageToDashboard()
            findNavController().navigate(action)
            viewModel.reset_room_details()
        }
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val back_dialog_frag = back_pressed_dialog("Do you want to exit and go to dashboard?","EXIT"){
                    val mess = mapOf("REQUEST" to "LEAVE_GAME","USERNAME" to viewModel.current_user.username,"ROOM_NO" to viewModel.room_no)
                    val en_mess = Json.encodeToString(mess)
                    viewModel.send_udp_server(en_mess)
                    val msg = "19 ${viewModel.current_user.username} ${viewModel.current_user.xp} ${viewModel.current_user.match_played} ${viewModel.current_user.match_won} ${viewModel.current_user.avg_points}"
                    viewModel.send_to_Server(msg)
                    val action = game_pageDirections.actionGamePageToDashboard()
                    findNavController().navigate(action)
                    viewModel.reset_room_details()
                }
                back_dialog_frag.show(childFragmentManager,"back_dialog_fragment")
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
    }
    fun start_game(){
        binding.balance.text = gameviewmodel.each_members_balance[viewModel.current_user.username].toString()
        val room_mems = viewModel.room_members
        val room_mems_size = viewModel.room_members.size
        if (room_mems_size >= 1){
            binding.apply {
                player1BidHeading.visibility = View.VISIBLE
                player1BidHeading.text = room_mems[0]
            }
            points_table_name_views.add(binding.player1PointsTable)
            points_table_points_views.add(binding.player1Points)
            points_table_balance_views.add(binding.player1Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer1PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer1Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer1Balance)
            cbidder_image_views[room_mems[0]] = binding.player1Bid
        }
        if (room_mems_size >= 2){
            binding.apply {
                player2BidHeading.visibility = View.VISIBLE
                player2BidHeading.text = room_mems[1]
            }
            points_table_name_views.add(binding.player2PointsTable)
            points_table_points_views.add(binding.player2Points)
            points_table_balance_views.add(binding.player2Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer2PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer2Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer2Balance)
            cbidder_image_views[room_mems[1]] = binding.player2Bid
        }
        if (room_mems_size >= 3){
            binding.apply {
                player3BidHeading.visibility = View.VISIBLE
                player3BidHeading.text = room_mems[2]
            }
            points_table_name_views.add(binding.player3PointsTable)
            points_table_points_views.add(binding.player3Points)
            points_table_balance_views.add(binding.player3Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer3PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer3Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer3Balance)
            cbidder_image_views[room_mems[2]] = binding.player3Bid
        }
        if (room_mems_size >= 4){
            binding.apply {
                player4BidHeading.visibility = View.VISIBLE
                player4BidHeading.text = room_mems[3]
            }
            points_table_name_views.add(binding.player4PointsTable)
            points_table_points_views.add(binding.player4Points)
            points_table_balance_views.add(binding.player4Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer4PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer4Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer4Balance)
            cbidder_image_views[room_mems[3]] = binding.player4Bid
        }
        if (room_mems_size >= 5){
            binding.apply {
                player5BidHeading.visibility = View.VISIBLE
                player5BidHeading.text = room_mems[4]
            }
            points_table_name_views.add(binding.player5PointsTable)
            points_table_points_views.add(binding.player5Points)
            points_table_balance_views.add(binding.player5Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer5PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer5Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer5Balance)
            cbidder_image_views[room_mems[4]] = binding.player5Bid
        }
        if (room_mems_size >= 6){
            binding.apply {
                player6BidHeading.visibility = View.VISIBLE
                player6BidHeading.text = room_mems[5]
            }
            points_table_name_views.add(binding.player6PointsTable)
            points_table_points_views.add(binding.player6Points)
            points_table_balance_views.add(binding.player6Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer6PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer6Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer6Balance)
            cbidder_image_views[room_mems[5]] = binding.player6Bid
        }
        if (room_mems_size >= 7){
            binding.apply {
                player7BidHeading.visibility = View.VISIBLE
                player7BidHeading.text = room_mems[6]
            }
            points_table_name_views.add(binding.player7PointsTable)
            points_table_points_views.add(binding.player7Points)
            points_table_balance_views.add(binding.player7Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer7PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer7Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer7Balance)
            cbidder_image_views[room_mems[6]] = binding.player7Bid
        }
        if (room_mems_size >= 8){
            binding.apply {
                player8BidHeading.visibility = View.VISIBLE
                player8BidHeading.text = room_mems[7]
            }
            points_table_name_views.add(binding.player8PointsTable)
            points_table_points_views.add(binding.player8Points)
            points_table_balance_views.add(binding.player8Balance)
            game_over_points_table_name_views.add(binding.gamOvPlayer8PointsTable)
            game_over_points_table_points_views.add(binding.gamOvPlayer8Points)
            game_over_points_table_balance_views.add(binding.gamOvPlayer8Balance)
            cbidder_image_views[room_mems[7]] = binding.player8Bid
        }
        for (i in 0..(gameviewmodel.game_members.size-1)){
            val member_name = gameviewmodel.game_members[i]
            points_table_name_views[i].text = member_name
            points_table_points_views[i].text = "0"
            points_table_balance_views[i].text = "80.0c"
        }
    }

    fun update_minimum_req_view(){
        binding.apply {
            batsmanCount.text = "${gameviewmodel.role_req["BAT"]}/${gameviewmodel.minimum_req["BAT"]}"
            bowlerCount.text = "${gameviewmodel.role_req["BOWL"]}/${gameviewmodel.minimum_req["BOWL"]}"
            allrounderCount.text = "${gameviewmodel.role_req["ALL"]}/${gameviewmodel.minimum_req["ALL"]}"
            wicketKeeperCount.text = "${gameviewmodel.role_req["WK"]}/${gameviewmodel.minimum_req["WK"]}"
            foreignersCount.text = "${gameviewmodel.role_req["F"]}/${gameviewmodel.minimum_req["F"]}"
            indiansCount.text = "${gameviewmodel.role_req["I"]}/${gameviewmodel.minimum_req["I"]}"
        }
    }

    fun update_points_table_view(){
        for (i in 0..(gameviewmodel.points_table_list.size-1)){
            val member_name = gameviewmodel.points_table_list[i]
            points_table_name_views[i].text = member_name
            points_table_points_views[i].text = gameviewmodel.each_members_points[member_name].toString()
            points_table_balance_views[i].text = "${gameviewmodel.each_members_balance[member_name]}C"
        }
    }

    fun show_game_over_points_table(){
        for (i in 0..(gameviewmodel.points_table_list.size-1)){
            val member_name = gameviewmodel.points_table_list[i]
            game_over_points_table_name_views[i].text = member_name
            game_over_points_table_points_views[i].text = gameviewmodel.each_members_points[member_name].toString()
            game_over_points_table_balance_views[i].text = "${gameviewmodel.each_members_balance[member_name]}C"
            if (gameviewmodel.each_members_is_qualified[member_name] == true){
                game_over_points_table_name_views[i].background = resources.getDrawable(R.drawable.qualified_textview)
                game_over_points_table_points_views[i].background = resources.getDrawable(R.drawable.qualified_textview)
                game_over_points_table_balance_views[i].background = resources.getDrawable(R.drawable.qualified_textview)
            }
            else{
                game_over_points_table_name_views[i].background = resources.getDrawable(R.drawable.disqualified_textview)
                game_over_points_table_points_views[i].background = resources.getDrawable(R.drawable.disqualified_textview)
                game_over_points_table_balance_views[i].background = resources.getDrawable(R.drawable.disqualified_textview)
            }
        }
        game_over_points_table_name_views[0].background = resources.getDrawable(R.drawable.winner_textview)
        game_over_points_table_points_views[0].background = resources.getDrawable(R.drawable.winner_textview)
        game_over_points_table_balance_views[0].background = resources.getDrawable(R.drawable.winner_textview)
        binding.gameOverRoomNo.text = "Room no. ${viewModel.room_no}"
        binding.darkBgLayout.visibility = View.VISIBLE
        binding.gameOverLayout.visibility = View.VISIBLE
        viewModel.current_user.xp += 10
        val cur_tot_ponts = viewModel.current_user.avg_points * (viewModel.current_user.match_played-1) + gameviewmodel.each_members_points[viewModel.current_user.username]!!
        viewModel.current_user.avg_points = cur_tot_ponts / viewModel.current_user.match_played
        if (gameviewmodel.points_table_list[0] == viewModel.current_user.username && gameviewmodel.each_members_is_qualified[viewModel.current_user.username]==true){
            viewModel.current_user.match_won +=1
            viewModel.current_user.xp +=10
        }
        val msg = "19 ${viewModel.current_user.username} ${viewModel.current_user.xp} ${viewModel.current_user.match_played} ${viewModel.current_user.match_won} ${viewModel.current_user.avg_points}"
        viewModel.send_to_Server(msg)
    }





    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())

    fun comma_split_to_list(text:String):List<String> = text.trim().split(",")

}