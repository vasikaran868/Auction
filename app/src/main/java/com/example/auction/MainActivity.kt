package com.example.auction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.auction.data.SocketDataSourceImpl
import dagger.hilt.EntryPoint
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val viewModel: AuctionViewModel by viewModels {
        AuctionViewModelFactory(
            (this?.application as AuctionApplication).database.UserDao()
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Auction)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val reconnecting_layout = findViewById<ConstraintLayout>(R.id.reconnecting_server_layout)
        viewModel.is_websocket_disconnected.observe(this,{
            if (it == true){
                reconnecting_layout.visibility = View.VISIBLE
            }
            if (it == false){
                reconnecting_layout.visibility = View.INVISIBLE
            }
        })

    }
}