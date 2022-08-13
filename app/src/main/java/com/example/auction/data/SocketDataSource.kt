package com.example.auction.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface SocketDataSource {

    suspend fun start_session():String

    suspend fun send_message(message:String)

    suspend fun observe_message():Flow<String>

    suspend fun close_session()

    suspend fun send_udp_server(message: String)

    suspend fun receive_udp_server()

    fun is_websocket_active():Boolean

    companion object{
        const val BASE_URL = "ws://192.168.0.108:8082"
        const val UDP_ADDRESS="192.168.0.108"
        const val UDP_PORT=8080
    }

}