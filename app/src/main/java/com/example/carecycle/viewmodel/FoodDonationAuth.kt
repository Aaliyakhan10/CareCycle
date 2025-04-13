package com.example.carecycle.viewmodel

import androidx.lifecycle.ViewModel
import com.example.carecycle.model.Post
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.FirebaseDatabase

class FoodDonationAuth: ViewModel() {
    fun savePost(title: String, lat: Double, lng: Double) {
        val postRef = FirebaseDatabase.getInstance().getReference("posts").push()
        val post = Post(
            id = postRef.key ?: "",
            title = title,
            lat = lat,
            lng = lng,
            geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))
        )
        postRef.setValue(post)
    }
}