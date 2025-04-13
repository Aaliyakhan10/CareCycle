package com.example.carecycle.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carecycle.MainActivity
import com.example.carecycle.R
import com.example.carecycle.Utils
import com.example.carecycle.databinding.EmailLayoutBinding
import com.example.carecycle.databinding.FragmentLoginBinding
import com.example.carecycle.viewmodel.ViewModelAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginFragment : Fragment() {

        private lateinit var binding: FragmentLoginBinding
        private lateinit var firebaseUser: FirebaseUser
        private lateinit var firebaseAuth: FirebaseAuth
        private val viewModelAuth: ViewModelAuth by viewModels()


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            checkAndRequestStoragePermission()
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate the layout for this fragment
            binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
            onLoginButtonClick()
            onSignInButtonClick()
            onResetButtonClick()

            return binding.root
        }

        private fun onResetButtonClick() {
            binding.resetTv.setOnClickListener {
                val dialog:AlertDialog
                val emailBinding=EmailLayoutBinding.inflate(layoutInflater)
                dialog=AlertDialog.Builder(requireContext())
                    .setView(emailBinding.root)
                    .create()
                emailBinding.resetPassBtn.setOnClickListener {
                    val email=emailBinding.emailEditTx.text.toString()
                    if(email.isNotEmpty()){
                        viewModelAuth.resetPassword(requireContext(),email)
                        dialog.dismiss()

                    }
                }



                dialog.show()

            }
        }

        private fun onNotVerify() {


        }

        private fun onSignInButtonClick() {
            binding.singin.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }

        }

        private fun onLoginButtonClick() {
            binding.loginBtn.setOnClickListener {
                val email = binding.emailTv.text!!.toString()
                val pass = binding.passwordTv.text!!.toString()
                if (email.isNotEmpty() || pass.isNotEmpty()) {
                    Utils.getInstance().signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser = FirebaseAuth.getInstance().currentUser!!
                                if (firebaseUser.isEmailVerified) {
                                    Log.d(
                                        "EmailVerification",
                                        "Email Verified: ${Utils.getInstance().currentUser?.isEmailVerified}"
                                    )


                                    Utils.successToast(requireContext(), "Email verified")
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    Utils.successToast(requireContext(), "Login Succesfully")
                                    requireActivity().finish()
                                } else {
                                    Utils.normalDialog(
                                        requireContext(),
                                        "Verify your email by clicking on link send on your email "
                                    )
                                    Log.d(
                                        "EmailVerification",
                                        "Email Verified: ${Utils.getInstance().currentUser?.isEmailVerified}"
                                    )

                                    Utils.makeToast(
                                        requireContext(),
                                        "Verify your email by clicking on link send on your email"
                                    )


                                }


                            } else {
                                Utils.makeToast(requireContext(), "Login Failed")
                            }

                        }

                }


            }
        }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

        }

        companion object {

        }
    private fun checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                101
            )
        } else {
            // Android 12 and below
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }
    }

}
