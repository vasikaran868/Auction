package com.example.auction

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.*
import com.example.auction.data.Logindatapreference
import com.example.auction.data.SocketDataSourceImpl
import com.example.auction.data.UserDao
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuctionViewModel(private val userDao:UserDao):ViewModel(){


    val httpclient= HttpClient(CIO){
        install(WebSockets)
        install(Logging)
    }
    val socketdatasource_object = SocketDataSourceImpl(httpclient)

    var players_list= mutableListOf<Map<String,String>>()

    var temp_players_list= mutableListOf<String>()
    lateinit var current_user :User

    val is_websocket_disconnected = MutableLiveData<Boolean>()


    val is_collector_initialised = mutableMapOf(
        "loading" to false,
        "login" to false,
        "sign_up" to false,
        "forget_password" to false,
        "dashboard" to false,
        "room" to false,
        "play_with_frnds" to false,
        "public_rooms" to false
    )

    private val _shared_msg = MutableSharedFlow<String>()
    val shared_msg = _shared_msg.asSharedFlow()

    var room_no ="0"

    var game_start_sec =""

    var room_members = mutableListOf<String>()

    var is_room_admin = false

    val is_connected_to_server = MutableLiveData<Boolean>(false)

    var wallet_money =0.00F

    var server_client_mili_diff = 0L

    var msg_yet_to_be_sent:String? = null

    lateinit var datastore :Logindatapreference

    val players_image_uri_map = mutableMapOf<String,String>()


    init {
        Log.v("MyActivity","viewmodel initialised")
        connect_to_server()
        viewModelScope.launch {
            socketdatasource_object.udp_flow.collect(){
                Log.v("MyActivity", it)
                _shared_msg.emit(it)
            }
        }
        receive_udp_server()
    }

    fun reset_room_details(){
        room_members.clear()
        room_no = ""
        game_start_sec = ""
        players_list.clear()
        temp_players_list.clear()
        players_image_uri_map.clear()
    }

    fun initialise_data_store(context: Context){
        datastore = Logindatapreference(context)
        Log.v("MyActivity","$datastore")
    }


    fun change_datastore_values(username:String,password:String,context:Context){
        viewModelScope.launch {
            datastore.savelogindetailsToPreferencesStore(username,password,context)
        }
    }

    fun send_to_Server(msg:String) {
        if (socketdatasource_object.is_websocket_active() == true && socketdatasource_object.is_network_call_in_progress == false){
            viewModelScope.launch {
                socketdatasource_object.send_message(msg)
                socketdatasource_object.is_network_call_in_progress = true
            }
        }
        else if (socketdatasource_object.is_websocket_active() == false){
            is_websocket_disconnected.value = true
            msg_yet_to_be_sent = msg
            connect_to_server()
        }
    }

    fun split_players_details(){
       for(player in temp_players_list){
           val temp_details = split_to_list(player)
           val players_details = mapOf<String,String>("name" to "${temp_details[0]} ${temp_details[1]}","role" to temp_details[2],"base" to temp_details[3],"points" to temp_details[4],"ind/for" to temp_details[5],"image_uri_key" to "${temp_details[0]}_${temp_details[1]}")
           players_list.add(players_details)
       }
    }

    fun send_udp_server(message:String){
        viewModelScope.launch {
            socketdatasource_object.send_udp_server(message)
        }
    }

    fun receive_udp_server(){
        viewModelScope.launch {
            socketdatasource_object.receive_udp_server()
        }
    }

    fun connect_to_server() {
        Log.v("MyActivity","$httpclient")
        Log.v("MyActivity","$socketdatasource_object")
        viewModelScope.launch {
            val result= socketdatasource_object.start_session()
            Log.v("MyActivity","result= $result")
            if (result=="success"){
                is_connected_to_server.value=true
                if (is_websocket_disconnected.value == true){
                    send_to_Server("20 ${current_user.username}")
                }
                is_websocket_disconnected.value = false
                viewModelScope.launch {
                    socketdatasource_object.observe_message().collect(){
                        Log.v("MyActivity","$it")
                        _shared_msg.emit(it)
                        socketdatasource_object.is_network_call_in_progress = false
                    }
                }
                if (msg_yet_to_be_sent != null){
                    send_to_Server(msg_yet_to_be_sent.toString())
                }
            }
            else{
                connect_to_server()
            }
        }
    }

    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())
}

class AuctionViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuctionViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
