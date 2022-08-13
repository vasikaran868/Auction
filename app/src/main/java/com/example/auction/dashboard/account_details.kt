package com.example.auction.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.auction.AuctionApplication
import com.example.auction.AuctionViewModel
import com.example.auction.AuctionViewModelFactory
import com.example.auction.R
import com.example.auction.databinding.FragmentAccountDetailsBinding
import com.example.auction.databinding.FragmentDashboardBinding
import com.example.auction.login.signup_page


class account_details : DialogFragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }

    private var _binding: FragmentAccountDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentAccountDetailsBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            accDetUsername.text = viewModel.current_user.username
            accDetPassword.text = viewModel.current_user.password
            accDetEmail.text = viewModel.current_user.email
        }

        binding.accDetUsernameChangeBut.setOnClickListener {
            if (binding.accDetUsernameChangeBut.text == "change"){
                binding.apply {
                    accDetUsername.visibility = View.INVISIBLE
                    accDetUsernameEtBox.visibility = View.VISIBLE
                    accDetUsernameChangeBut.text = "submit"
                }
            }
            else if (binding.accDetUsernameChangeBut.text == "submit"){
                if (binding.accDetUsernameEt.text.isNullOrBlank()){
                    binding.accDetUsernameEt.setError("This Field Cannot Be Blank")
                }
                else{
                    val msg = "16 ${binding.accDetUsernameEt.text.toString()} ${viewModel.current_user.username}"
                    if (parentFragment is dashboard){
                        (parentFragment as dashboard).send_to_server(msg)
                    }
                }
            }
        }

        binding.accDetPasswordChangeBut.setOnClickListener {
            if (binding.accDetPasswordChangeBut.text == "change"){
                binding.apply {
                    accDetPassword.visibility = View.INVISIBLE
                    accDetPasswordEtBox.visibility = View.VISIBLE
                    accDetPasswordChangeBut.text = "submit"
                }
            }
            else if (binding.accDetPasswordChangeBut.text == "submit"){
                if (binding.accDetPasswordEt.text.isNullOrBlank()){
                    binding.accDetPasswordEt.setError("This Field Cannot Be Blank")
                }
                else{
                    val msg = "15 ${binding.accDetPasswordEt.text.toString()} ${viewModel.current_user.username}"
                    if (parentFragment is dashboard){
                        (parentFragment as dashboard).send_to_server(msg)
                    }
                }
            }
        }

        binding.accDetEmailChangeBut.setOnClickListener {
            if (binding.accDetEmailChangeBut.text == "change"){
                binding.apply {
                    accDetEmail.visibility = View.INVISIBLE
                    accDetEmailEtBox.visibility = View.VISIBLE
                    accDetEmailChangeBut.text = "submit"
                }
            }
            else if (binding.accDetEmailChangeBut.text == "submit"){
                if (binding.accDetEmailEt.text.isNullOrBlank()){
                    binding.accDetEmailEt.setError("This Field Cannot Be Blank")
                }
                else{
                    val msg = "17 ${binding.accDetEmailEt.text.toString()} ${viewModel.current_user.username}"
                    if (parentFragment is dashboard){
                        (parentFragment as dashboard).send_to_server(msg)
                    }
                }
            }
        }
    }
    fun username_changed(username:String){
        Log.v("MyActivity","username_change_ fun called")
        binding.apply {
            accDetUsername.text = username
            accDetUsernameEt.setText("")
            accDetUsernameEt.clearFocus()
            accDetUsernameEt.visibility = View.INVISIBLE
            accDetUsername.visibility = View.VISIBLE
            accDetUsernameChangeBut.text = "change"
        }
    }
    fun password_changed(password:String){
        Log.v("MyActivity","password_change_ fun called")
        binding.apply {
            accDetPassword.text = password
            accDetPasswordEt.setText("")
            accDetPasswordEt.clearFocus()
            accDetPasswordEt.visibility = View.INVISIBLE
            accDetPassword.visibility = View.VISIBLE
            accDetPasswordChangeBut.text = "change"
        }

    }
    fun email_changed(email:String){
        Log.v("MyActivity","email_change_ fun called")
        binding.apply {
            accDetEmail.text = email
            accDetEmailEt.setText("")
            accDetEmailEt.clearFocus()
            accDetEmailEt.visibility = View.INVISIBLE
            accDetEmail.visibility = View.VISIBLE
            accDetEmailChangeBut.text = "change"
        }
    }
}