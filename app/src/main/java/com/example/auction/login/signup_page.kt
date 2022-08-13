package com.example.auction.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.auction.*
import com.example.auction.databinding.FragmentLoginFragmentBinding
import com.example.auction.databinding.FragmentSignupPageBinding
import kotlinx.coroutines.launch


class signup_page : Fragment() {

    private val viewModel: AuctionViewModel by activityViewModels {
        AuctionViewModelFactory(
            (activity?.application as AuctionApplication).database.UserDao()
        )
    }
    private var _binding: FragmentSignupPageBinding? = null
    private val binding get() = _binding!!
    var dob= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun update_date_et(day:Int,month:Int,year:Int){
        binding.signupDateEdittexxt.setText("${day}-${month+1}-${year}")
        dob = "${day}/${month+1}/${year}"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signup_username_et=binding.signupUsernameEdittext
        val signup_email_et = binding.signupEmailEdittexxt
        val signup_password_et = binding.signupPasswordEdittexxt
        val signup_date_et = binding.signupDateEdittexxt

        if (viewModel.is_collector_initialised["sign_up"] == false){
            lifecycleScope.launch {
                Log.v("MyActivity","collector initialised")
                viewModel.is_collector_initialised["sign_up"]=true
                viewModel.shared_msg.collect(){
                    Log.v("MyActivity","from signup: $it")
                    if (it == "username_exist"){
                        Toast.makeText(context, "username already exist", Toast.LENGTH_SHORT).show()
                    }
                    else if (it =="user_added"){
                        Toast.makeText(context, "user added", Toast.LENGTH_SHORT).show()
                        val action = signup_pageDirections.actionSignupPageToLoginFragment()
                        findNavController().navigate(action)
                    }
                    else if (it =="failed_add_user"){
                        Toast.makeText(context, "failed to add user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        signup_date_et.setOnFocusChangeListener { view, b ->
            if(view.isFocused == false){
                val date_dialog_frag = date_picker_dialog()
                date_dialog_frag.show(childFragmentManager,"date_dialog_frag")
            }
            signup_date_et.clearFocus()
        }

        binding.backToLoginButton.setOnClickListener {
            this.findNavController().navigateUp()
        }
        binding.signupButton.setOnClickListener {
            if (signup_username_et.text.isNullOrBlank()){
                signup_username_et.setError("This Field Cannot Be Blank")
            }
            else if (signup_email_et.text.isNullOrBlank()){
                signup_email_et.setError("This Field Cannot Be Blank")
            }
            else if (signup_date_et.text.isNullOrBlank()){
                signup_date_et.setError("This Field Cannot Be Blank")
            }
            /*
            else if (signup_date_et.text.toString().toIntOrNull()==null){
                signup_date_et.setError("enter a valid date")
            }
            else if (signup_month_et.text.isNullOrBlank()){
                signup_month_et.setError("This Field Cannot Be Blank")
            }
            else if (signup_month_et.text.toString().toIntOrNull()==null) {
                signup_month_et.setError("enter a valid month")
            }
            else if (signup_year_et.text.isNullOrBlank()){
                signup_year_et.setError("This Field Cannot Be Blank")
            }
            else if (signup_year_et.text.toString().toIntOrNull()==null) {
                signup_year_et.setError("enter a valid year")
            }*/
            else if (signup_password_et.text.isNullOrBlank()){
                signup_password_et.setError("This Field Cannot Be Blank")
            }
            else if(viewModel.is_connected_to_server.value==false){
                Toast.makeText(requireContext(), "Not Connected To Server", Toast.LENGTH_LONG).show()
            }
            else{
                /*if (signup_date_et.text?.length==1){
                    signup_date_et.setText("0${signup_date_et.text}")
                }
                if (signup_month_et.text?.length==1){
                    signup_month_et.setText("0${signup_month_et.text}")
                }
                */
                val username = signup_username_et.text
                val password = signup_password_et.text
                val email = signup_email_et.text
                val message= "1 ${username!!.trim()} ${password!!.trim()} ${email!!.trim()} ${dob.trim()}"
                viewModel.send_to_Server(message)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.is_collector_initialised["sign_up"] = false
        Log.v("MyActivity","login frag destroyed")
    }
}