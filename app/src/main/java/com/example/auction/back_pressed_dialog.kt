package com.example.auction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.auction.dashboard.dashboard
import com.example.auction.databinding.FragmentBackPressedDialogBinding
import com.example.auction.databinding.FragmentRoomPageBinding

class back_pressed_dialog(text_string:String,button_string:String,private val back_button_pressed:()->Unit) : DialogFragment() {

    private var _binding: FragmentBackPressedDialogBinding?= null
    val binding get() = _binding!!
    val txt = text_string
    val button_txt = button_string


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackPressedDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backDialogText.text = txt
        binding.backButtonDialog.text = button_txt
        binding.backButtonDialog.setOnClickListener {
            back_button_pressed()
        }
        binding.cancelButtonDialog.setOnClickListener {
            dismiss()
        }
    }
}