package com.example.carecycle.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carecycle.R
import com.example.carecycle.Utils
import com.example.carecycle.api.RetrofitInstance
import com.example.carecycle.api.ImgBBService
import com.example.carecycle.databinding.FragmentRegisterBinding
import com.example.carecycle.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 100

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        binding.uploadPicBtn.setOnClickListener {
            openGallery()
        }

        binding.signinBtn.setOnClickListener {
            handleRegistration()
        }

        binding.loginin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.profileImage.setImageURI(selectedImageUri)
        }
    }

    private fun handleRegistration() {
        val name = binding.nameTxt.text.toString().trim()
        val email = binding.emailTxt.text.toString().trim()
        val password = binding.passwordTxt.text.toString().trim()
        val confirmPassword = binding.confirmPasswordTxt.text.toString().trim()
        val address = binding.addressTxt.text.toString().trim()
        val userType = getSelectedUserType()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
            address.isEmpty() || userType.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a profile image", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageToImgBB(selectedImageUri!!) { imageUrl ->
            if (imageUrl != null) {
                registerUser(name, email, password, address, userType, imageUrl)
            } else {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectedUserType(): String {
        val selectedId = binding.userTypeGroup.checkedRadioButtonId
        return if (selectedId != -1) {
            val radioButton = binding.root.findViewById<RadioButton>(selectedId)
            radioButton.text.toString()
        } else {
            ""
        }
    }

    private fun uploadImageToImgBB(uri: Uri, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageFile = File(Utils.getRealPathFromURI(requireContext(), uri))
                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                val part = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                val apiKey = RequestBody.create("text/plain".toMediaTypeOrNull(), Utils.IMG_BB_API_KEY)

                val service = RetrofitInstance.getRetrofitInstance().create(ImgBBService::class.java)
                val response = service.uploadImage(part, apiKey)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        callback(response.body()?.data?.url)
                    } else {
                        Log.e("ImgBB", "Upload error: ${response.message()}")
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("ImgBB", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    private fun registerUser(
        name: String,
        email: String,
        password: String,
        address: String,
        userType: String,
        imageUrl: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                val user = User(name=name, email=email, uid=uid, address = address, userType = userType, profileImageUrl = imageUrl)

                FirebaseDatabase.getInstance().reference
                    .child("users").child(uid)
                    .setValue(user)
                    .addOnSuccessListener {
                        auth.currentUser?.sendEmailVerification()
                        Toast.makeText(requireContext(), "Account created. Please verify your email.", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to create user", Toast.LENGTH_SHORT).show()
            }
    }
}
