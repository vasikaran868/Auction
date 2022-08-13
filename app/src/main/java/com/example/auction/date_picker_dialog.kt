package com.example.auction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.auction.databinding.FragmentDashboardBinding
import com.example.auction.databinding.FragmentDatePickerDialogBinding
import com.example.auction.login.signup_page

class date_picker_dialog : DialogFragment() {

    var _binding: FragmentDatePickerDialogBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentDatePickerDialogBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupDatePicker.setOnDateChangedListener { datePicker, i, i2, i3 ->
            if (parentFragment is signup_page){
                (parentFragment as signup_page).update_date_et(i3,i2,i)
            }
        }
    }
}