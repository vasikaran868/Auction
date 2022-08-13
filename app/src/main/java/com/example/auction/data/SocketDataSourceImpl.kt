package com.example.auction.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class SocketDataSourceImpl(
    private val client:HttpClient
):SocketDataSource {

    private var socket:WebSocketSession?= null

    private val udp_socket = DatagramSocket()

    private val server_address= InetAddress.getByName(SocketDataSource.UDP_ADDRESS)

    val udp_flow = MutableSharedFlow<String>()


    init {
        Log.v("MyActivity","${udp_socket.localSocketAddress}")
    }

    override suspend fun start_session(): String {
            Log.v("MyActivity","hii")
        return try {
            Log.v("MyActivity","hello")
            Log.v("MyActivity","vvvvv..$socket")
            socket = client.webSocketSession {
                url("${SocketDataSource.BASE_URL}/")
            }
            Log.v("MyActivity","vasi .....$socket")
            if (socket?.isActive == true) {
                socket?.ensureActive()
                "success"
            } else "Couldn't establish a connection."
        }  catch (e:Exception){
            Log.v("MyActivity","$e")
            return e.toString()
        }
    }

    override fun is_websocket_active():Boolean{
        return socket!!.isActive
    }

    override suspend fun send_message(message: String) {
        try{
            Log.v("MyActivity","msg= $message")
            socket?.send(Frame.Text(message))
        }catch (e:Exception){
            Log.v("MyActivity","unable to send the message $e")
        }
    }

    override suspend fun observe_message():Flow<String> {
    return try{
            Log.v("MyActivity","123")
            socket?.incoming?.receiveAsFlow()?.filter { it is Frame.Text }?.map {
                (it as? Frame.Text)?.readText() ?: ""
            }?: flow {  }
        }catch (e:Exception){
            Log.v("MyActivity","error in receiving msg $e")
            flow {  }
        }
    }

    override suspend fun close_session() {
        socket?.close()
    }

    override suspend fun send_udp_server(message:String) {
        Log.v("MyActivity","to udp server: $message")
        withContext(Dispatchers.IO) {
            val packet = DatagramPacket(message.toByteArray(),message.length,server_address,SocketDataSource.UDP_PORT)
            udp_socket.send(packet)
        }
    }

    override suspend fun receive_udp_server() {
        return withContext(Dispatchers.IO){
            val recvBuf = ByteArray(1024)
            val rec_packet =DatagramPacket(recvBuf,recvBuf.size,udp_socket.localSocketAddress)
            udp_socket.receive(rec_packet)
            val msg = rec_packet.data.decodeToString()
            udp_flow.emit(msg.split(";")[0])
            receive_udp_server()
        }
    }
}