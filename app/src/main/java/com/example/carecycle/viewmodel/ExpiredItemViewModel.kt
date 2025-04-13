package com.example.carecycle.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carecycle.Utils
import com.example.carecycle.model.ExpiredPostData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpiredItemViewModel: ViewModel() {
    fun UploadExpiredDatabase(expireItem: ExpiredPostData, context: Context) {
        val uid = Utils.getCurrentUser()
        val databaseRef = FirebaseDatabase.getInstance().getReference("ExpiredItem").child(uid)

        databaseRef.push().setValue(expireItem)
            .addOnSuccessListener {
                Utils.successToast(context, "Expired Item Added Successfully")
            }
            .addOnFailureListener {
                Utils.makeToast(context, "Failed to add expired item")
            }
    }

    fun getListOFExpireItem(context: Context): LiveData<MutableList<ExpiredPostData>> {
        val uid = Utils.getCurrentUser()
        val expiredItemListLiveData = MutableLiveData<MutableList<ExpiredPostData>>()
        val database = FirebaseDatabase.getInstance().getReference("ExpiredItem").child(uid)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = mutableListOf<ExpiredPostData>()

                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(ExpiredPostData::class.java)
                    item?.let { itemList.add(it) }
                }

                expiredItemListLiveData.value = itemList
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.makeToast(context, "Failed to read data: ${error.message}")
            }
        })

        return expiredItemListLiveData
    }

}