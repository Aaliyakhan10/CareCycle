package com.example.carecycle.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.carecycle.LoginActivity
import com.example.carecycle.R
import com.example.carecycle.Utils
import com.example.carecycle.databinding.FragmentAccountBinding
import com.marsad.stylishdialogs.StylishAlertDialog

class AccountFragment : Fragment() {

     private lateinit var binding: FragmentAccountBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentAccountBinding.inflate(layoutInflater,container,false)
        setUserInfo()
        binding.logoutButton.setOnClickListener {
            StylishAlertDialog(requireContext(), StylishAlertDialog.WARNING)
                .setTitleText("Are you sure you want to logout?")
                .setConfirmText("Logout")
                .setConfirmClickListener(StylishAlertDialog.OnStylishClickListener {
                    logout()
                })
                .setCancelButton(
                    "Cancel",
                    StylishAlertDialog::dismissWithAnimation
                )
                .show()
        }
        binding.VoucherButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_voucherClaimedFragment)
        }
        return binding.root
    }

    private fun setUserInfo() {
        Utils.userInfo { name,address,email,usertype,coin,imgUrl ->
            binding.nameText.text=name
            binding.userTypeText.text=usertype
            binding.addressText.text=address
            binding.emailText.text=email
            binding.coinsText.text= "Coins: $coin"
            Glide.with(this).load(imgUrl).circleCrop().into(binding.profileImage)
        }
    }

    fun logout() {
        Utils.getInstance().signOut()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        this.startActivity(intent)

        this.requireActivity().finish()
    }

    companion object {

    }
}