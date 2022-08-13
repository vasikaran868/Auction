package com.example.auction.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auction.*
import com.example.auction.databinding.FragmentLoginFragmentBinding
import kotlinx.coroutines.launch
import java.net.*
import java.time.*
import java.util.*


class login_fragment : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }

    private var _binding: FragmentLoginFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var username:String
    lateinit var password:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("MyActivity","login fragment created")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val passwordedittext=binding.passwordEditText
        val usernameedittext= binding.usernameEditText
        viewModel.is_connected_to_server.observe(viewLifecycleOwner,{
            if (it == true){
                if (viewModel.is_collector_initialised["login"] == false){
                    lifecycleScope.launch {
                        Log.v("MyActivity","collector initialised")
                        viewModel.is_collector_initialised["login"]=true
                        viewModel.shared_msg.collect(){
                            val params = split_to_list(it)
                            Log.v("MyActivity","from login: $it")
                            if (params[0] == "logged_in"){
                                viewModel.current_user = User(username= params[1], password =  params[2], email = params[3], xp = params[4].toInt(), match_played = params[5].toInt(), match_won = params[6].toInt(), avg_points = params[7].toFloat())
                                val action= login_fragmentDirections.actionLoginFragmentToLoading(username,password)
                                activity?.findNavController(R.id.login_page)?.navigate(action)
                            }else if(it == "no_user"){
                                Toast.makeText(requireContext(), "Invalid username", Toast.LENGTH_SHORT).show()
                            }else if(it == "password_incorrect"){
                                Toast.makeText(requireContext(), "Invalid password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })

        binding.forgetPasswordButton.setOnClickListener {
            if(usernameedittext.text.isNullOrBlank()){
                usernameedittext.setError("This Field Cannot Be Empty")
            }
            else{
                val action= login_fragmentDirections.actionLoginFragmentToForgotPassword(usernameedittext.text.toString())
                this.findNavController().navigate(action)
            }
        }

        binding.loginButton.setOnClickListener {
            if(usernameedittext.text.isNullOrBlank()){
                usernameedittext.setError("This Field Cannot Be Blank")
            }
            else if (passwordedittext.text.isNullOrBlank()){
                passwordedittext.setError("This Field Cannot Be Blank")
            }
            else if (viewModel.is_connected_to_server.value==false){
                Toast.makeText(requireContext(), "Not Connected To Server", Toast.LENGTH_LONG).show()
            }
            else{
                username= usernameedittext.text.toString().trim()
                password = passwordedittext.text.toString().trim()
                viewModel.send_to_Server("0 ${username} ${password}")
            }
        }

        binding.signuppageButton.setOnClickListener {
            val action = login_fragmentDirections.actionLoginFragmentToSignupPage()
            this.findNavController().navigate(action)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["login"] = false
        Log.v("MyActivity","login frag destroyed")
    }

    fun split_to_list(text:String):List<String> = text.trim().split("\\s+".toRegex())

}