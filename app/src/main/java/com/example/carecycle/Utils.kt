package com.example.carecycle

import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.marsad.stylishdialogs.StylishAlertDialog
import com.shashank.sony.fancytoastlib.FancyToast

object Utils {
    private lateinit var firebaseDatabase: DatabaseReference
    fun makeToast(context: Context, message: String) {
        FancyToast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private var firebaseAuth: FirebaseAuth? = null
    fun getInstance(): FirebaseAuth {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
        return firebaseAuth!!
    }

    fun getCurrentUser(): String {
        return FirebaseAuth.getInstance().currentUser?.uid.toString()

    }


    private lateinit var pDialog: StylishAlertDialog
    fun progressDialog(context: Context, message: String) {
        pDialog = StylishAlertDialog(context, StylishAlertDialog.PROGRESS)
        pDialog.progressHelper.barColor = R.color.colorPrimaryComplement
        pDialog.setTitleText(message)
            .setCancellable(false)
            //.setCancelledOnTouchOutside(false)
            .show()

    }

    fun normalDialog(context: Context, message: String) {
        pDialog = StylishAlertDialog(context, StylishAlertDialog.NORMAL)
        pDialog.progressHelper.barColor =R.color.colorPrimaryComplement
        pDialog.setTitleText(message)

            .show()


    }
    private lateinit var firebasereference:DatabaseReference
    fun userInfo(callback: (String, String, String, String, String, String) -> Unit) {
        val uid = getCurrentUsersId()
        val firebaseDatabase = FirebaseDatabase.getInstance().getReference("users")

        firebaseDatabase.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value.toString()
                val address=it.child("address").value.toString()
                val email=it.child("email").value.toString()
                val usertype=it.child("userType").value.toString()
                val coin=it.child("coins").value.toString()
                val imageurl=it.child("profileImageUrl").value.toString()
                callback(name,address,email,usertype,coin,imageurl)
            } else {
                callback("People","","","","","")
            }
        }.addOnFailureListener {
            callback("People","","","","","")
        }
    }

    fun warningDialog(context: Context, message: String, yesButtonText: String) {
        StylishAlertDialog(context, StylishAlertDialog.WARNING)
            .setTitleText(message)

            .setConfirmText(yesButtonText)
            .setConfirmClickListener(StylishAlertDialog::dismissWithAnimation)

            .show()

    }

    fun defaultAlert(context: Context) {
        var dialog: AlertDialog? = null

        dialog = AlertDialog.Builder(context)
            .setTitle("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                dialog?.dismiss()

            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog?.dismiss()
            }
            .show()
    }
    fun hideProgressDialog(context: Context) {
        pDialog = StylishAlertDialog(context, StylishAlertDialog.PROGRESS)
        pDialog.hide()
    }

    fun successToast(context: Context, message: String) {
        FancyToast.makeText(context, message, FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
    }
    const val IMG_BB_API_KEY = "API_KEY"

    fun getRealPathFromURI(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val path = cursor?.getString(idx!!)
        cursor?.close()
        return path ?: ""
    }

    fun getCurrentUsersId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

}

