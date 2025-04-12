package com.example.carecycle.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.carecycle.Utils
import com.example.carecycle.model.User

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow

class ViewModelAuth:ViewModel() {
    val _iscurrentuser= MutableStateFlow(false)
    private lateinit var databaseReference: DatabaseReference
    val currentuser=_iscurrentuser


    init {
        Utils.getInstance().currentUser?.let {
            _iscurrentuser.value=true
        }

    }

    fun setUserData(user: User){
        val uid= Utils.getCurrentUser()
        FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(user)
    }
    fun getUserdata(callback:(String,String,String)->Unit){
        val uid= Utils.getCurrentUser()
        var name:String
        var email:String
        FirebaseDatabase.getInstance().getReference("users").child(uid).get().addOnSuccessListener {
            if(it.exists()){
                name=it.child("name").value.toString()

                email=it.child("email").value.toString()


                callback(name,email,"")

            }
        }


    }
    fun updateUserData(context: Context,name:String,email:String){
        val uid= Utils.getCurrentUser()
        val user= mapOf<String,String>("name" to name,"email" to email,"userid" to uid)
        FirebaseDatabase.getInstance().getReference("users").child(uid).updateChildren(user).addOnSuccessListener {
            Utils.successToast(context,"Successfully Updated the Value")

        }.addOnFailureListener {
            Utils.normalDialog(context,"Error while chnaging the name ,Try Again")
        }


    }
    fun resetPassword(context: Context,email: String){
        val auth=Utils.getInstance()
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful){
                Utils.successToast(context,"Password reset email send successfully")
            }else{

                it.exception?.let {
                    when (it) {
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        }
                        is com.google.firebase.auth.FirebaseAuthEmailException -> {
                            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


    }
}