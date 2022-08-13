package com.example.auction.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.auction.AuctionApplication
import com.example.auction.AuctionViewModel
import com.example.auction.AuctionViewModelFactory
import com.example.auction.R
import com.example.auction.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch


class forgot_password : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var username:String
    var otp_is_verified = false
    var email_is_verified = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentForgotPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            username= it.getString("username").toString()
        }

        if (viewModel.is_collector_initialised["forget_password"]==false) {
            viewModel.is_collector_initialised["forget_password"]=true
            lifecycleScope.launch {
                viewModel.shared_msg.collect(){
                    Log.v("MyActivity","from forgot_password: $it")
                    if (it =="veri_code_sent"){
                        email_is_verified = true
                        binding.apply {
                            sendOtpButton.text = "verify"
                            forgotpasswordEmailEdittext.hint = "OTP"
                            forgetpasswordEmailInput.hint = "OTP"
                            forgotpasswordEmailEdittext.setText("")
                            forgotpasswordEmailEdittext.clearFocus()
                        }
                    }
                    if (it == "otp_verified"){
                        otp_is_verified = true
                        binding.apply {
                            sendOtpButton.text = "submit"
                            forgotpasswordEmailEdittext.hint = "New Password"
                            forgetpasswordEmailInput.hint = "New Password"
                            forgotpasswordEmailEdittext.setText("")
                            forgotpasswordEmailEdittext.clearFocus()
                        }
                    }
                    if (it == "changed_password"){
                        val action = forgot_passwordDirections.actionForgotPasswordToLoginFragment()
                        findNavController().navigate(action)

                    }
                }
            }
        }

        binding.sendOtpButton.setOnClickListener {

            if (!email_is_verified && !otp_is_verified ){
                val message= "2 $username ${binding.forgotpasswordEmailEdittext.text.toString()}"
                viewModel.send_to_Server(message)
            }
            else if (email_is_verified && !otp_is_verified){
                val message = "14 $username ${binding.forgotpasswordEmailEdittext.text.toString()}"
                viewModel.send_to_Server(message)
            }
            else if (email_is_verified && otp_is_verified){
                val message = "15 $username ${binding.forgotpasswordEmailEdittext.text.toString()}"
                viewModel.send_to_Server(message)
            }
        }
        binding.toLoginButton.setOnClickListener {
            this.findNavController().navigateUp()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["forget_password"]= false
        Log.v("MyActivity","forget_password destroyed")
    }
}