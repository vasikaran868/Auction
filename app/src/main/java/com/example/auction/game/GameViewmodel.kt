package com.example.auction.game

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class GameViewmodel(){

    inner class Timerz(tot_countdown: Long) : CountDownTimer(tot_countdown, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //_bid_timer_milis_left.value = "%.1f".format(millisUntilFinished.toFloat()/1000)
            _bid_timer_milis_left.value = (millisUntilFinished/1000).toInt().toString()
        }
        override fun onFinish() {
            //_bid_timer_milis_left.value = "0.0"
            _bid_timer_milis_left.value = "0"
        }
    }
    private var _players_list = listOf<Map<String,String>>()
    val players_list get() = _players_list
    var game_started = false
    lateinit var start_time : OffsetDateTime
    lateinit var bid_end_time :OffsetDateTime
    var total_players = 0
    private var _player_no = 0
    val player_no get() = _player_no
    var countdown_milis =0L
    var server_client_mili_difference = 0L
    private val _game_start_timer_milis = MutableLiveData<String>("0")
    val game_start_timer_milis :LiveData<String> get() = _game_start_timer_milis
    private val _bid_timer_milis_left = MutableLiveData<String>()
    val bid_timer_milis_left:LiveData<String> get() = _bid_timer_milis_left
    val game_starts_layout_invisibility = MutableLiveData(false)
    private val _current_player = MutableLiveData<Map<String,String>>()
    val current_player:LiveData<Map<String,String>> get() = _current_player
    var bid_timer_object = Timerz(0)
    var current_bid = 0F
    var current_bidder:String? = null
    var each_members_points = mutableMapOf<String,Int>()
    var each_members_balance = mutableMapOf<String,Float>()
    var each_members_is_qualified = mutableMapOf<String,Boolean>()
    var bid_timer_over_msg_received = false
    var role_req = mutableMapOf<String,Int>()
    var minimum_req = mutableMapOf<String,Int>()
    var game_members = mutableListOf<String>()
    var points_table_list = mutableListOf<String>()


    fun initialise_role_req(){
        role_req["BAT"] = 0
        role_req["BOWL"] = 0
        role_req["ALL"] = 0
        role_req["WK"] = 0
        role_req["F"] = 0
        role_req["I"] = 0
        /*minimum_req["BAT"]= 4
        minimum_req["BOWL"] = 4
        minimum_req["ALL"] = 3
        minimum_req["WK"] = 2
        minimum_req["F"] = 5
        minimum_req["I"] = 9*/
        minimum_req["BAT"]= 1
        minimum_req["BOWL"] = 1
        minimum_req["ALL"] = 1
        minimum_req["WK"] = 1
        minimum_req["F"] = 1
        minimum_req["I"] = 1
    }
    fun update_role_req(role:String){
        role_req[role] = role_req[role]!!.plus(1)
    }

    fun initialise_balance_and_points(room_members:List<String>){
        room_members.forEach {
            each_members_balance[it] = 80F
            each_members_points[it] = 0
            game_members.add(it)
            points_table_list.add(it)
            each_members_is_qualified[it] = true
        }

    }


    fun update_players_list(play_list:List<Map<String,String>>){
        _players_list = play_list
        total_players = play_list.size
    }
    fun update_start_time (time:OffsetDateTime){
        start_time = time
        bid_end_time= time.plusSeconds(8)
    }

    fun game_start_timer_page(){
        var current_utc = OffsetDateTime.now(ZoneOffset.UTC)
        val milis_remaining= (current_utc.until(start_time, ChronoUnit.MILLIS)) - server_client_mili_difference
        val start_timer = object :CountDownTimer(milis_remaining,1000){
            init {
                Log.v("MyActivity","timer started")
            }
            override fun onTick(mili_left: Long) {
                //_game_start_timer_milis.value = "%.1f".format(mili_left.toFloat()/1000)
                _game_start_timer_milis.value = (mili_left/1000).toInt().toString()
            }

            override fun onFinish() {
                //_game_start_timer_milis.value="0.0"
                _game_start_timer_milis.value ="0"
                game_starts_layout_invisibility.value = true
                start_game()
            }
        }.start()
    }

    fun start_game(){
        _current_player.value = players_list[player_no]
        start_bid_timer_until_bid_end_time()

    }

    fun update_bid_timer(end_time:OffsetDateTime){
        bid_end_time = end_time
        bid_timer_object.cancel()
        start_bid_timer_until_bid_end_time()
    }


    fun start_bid_timer_until_bid_end_time(){
        val milis_until_bid_end_time = (OffsetDateTime.now(ZoneOffset.UTC).until(bid_end_time, ChronoUnit.MILLIS)) - server_client_mili_difference
        bid_timer_object = Timerz(milis_until_bid_end_time)
        bid_timer_object.start()
    }

    fun get_bid_amount():Float{
        val current_player_value = current_player.value
        if (current_bid == 0F){
            val base = current_player_value?.get("base")
            if (base!!.get(base.length-1) == 'L'){
                return base.dropLast(1).toFloat()/100
            }
            else{
                return base.dropLast(1).toFloat()
            }
        }
        else{
            return current_bid + 0.25F
        }
    }
    fun update_current_bid(bid:Float){
        current_bid = bid
    }

    fun next_player(){
        _player_no +=1
        _current_player.value = players_list[player_no]
        current_bid = 0F
    }

    fun update_balance_points(member_name:String,current_bid:Float){
        each_members_balance[member_name] = each_members_balance[member_name]!!.minus(current_bid)
        each_members_points[member_name] = each_members_points[member_name]!!.plus(current_player.value?.get("points")!!.toInt())
    }


    fun update_points_table_list(){
        for (i in 0..(points_table_list.size -2) ){
            for(j in 0..(points_table_list.size - i - 2)){
                if (each_members_points[points_table_list[j]]!! < each_members_points[points_table_list[j+1]]!!){
                    val temp = points_table_list[j]
                    points_table_list[j] = points_table_list[j+1]
                    points_table_list[j+1] = temp
                }
                if (each_members_points[points_table_list[j]]!! == each_members_points[points_table_list[j+1]]!!){
                    if (each_members_balance[points_table_list[j]]!! < each_members_balance[points_table_list[j+1]]!!){
                        val temp = points_table_list[j]
                        points_table_list[j] = points_table_list[j+1]
                        points_table_list[j+1] = temp
                    }
                }
            }
        }
        Log.v("MyActivity","zyxxxx...$points_table_list")
    }

    fun check_if_qualified(bat_nos:Int, bowl_nos:Int, all_nos:Int, wk_nos:Int, f_nos:Int, i_nos:Int):Boolean{
        if( bat_nos>= minimum_req["BAT"]!! && bowl_nos >= minimum_req["BOWL"]!! && all_nos >= minimum_req["ALL"]!! && wk_nos >= minimum_req["WK"]!! && f_nos >= minimum_req["F"]!! && i_nos >= minimum_req["I"]!!){
            return true
        }
        else{
            return false
        }
    }

    fun rearrange_disqualified(){
        var disqualified_list = mutableListOf<String>()
        for( player in points_table_list){
            if (each_members_is_qualified[player] == false){
                points_table_list.remove(player)
                disqualified_list.add(player)
            }
        }
        points_table_list.addAll(disqualified_list)
        Log.v("MyActivity","${points_table_list}")

    }


}