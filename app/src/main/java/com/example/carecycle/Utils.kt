package com.example.carecycle

import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.carecycle.api.ImgBBService
import com.example.carecycle.api.RetrofitInstance
import com.example.carecycle.model.ImgBBResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.marsad.stylishdialogs.StylishAlertDialog
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

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
    const val IMG_BB_API_KEY = "eb3102cdc9aeacb4d68eb34e6800d693"

    fun getRealPathFromURI(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val path = cursor?.getString(idx!!)
        cursor?.close()
        return path ?: ""
    }


}

