package com.example.carecycle.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carecycle.Utils
import com.example.carecycle.model.ExpiredPostData
import com.example.carecycle.model.Post
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.*

class FoodDonationAuth : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val postMap = mutableMapOf<String, Post>() // Helps to update/remove easily

    private val postsRef = FirebaseDatabase.getInstance().getReference("posts")
    private val geoFireRef = FirebaseDatabase.getInstance().getReference("posts_geofire")

    fun savePost(
        userId: String,
        foodName: String,
        description: String,
        platesAvailable: Int,
        lat: Double,
        lng: Double
    ) {
        val postRef = postsRef.push()
        val postId = postRef.key ?: run {
            Log.e("SavePost", "Failed to generate post ID")
            return
        }

        val post = Post(
            id = postId,
            userId = userId,
            foodName = foodName,
            description = description,
            platesAvailable = platesAvailable,
            geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng)),
            latitude = lat,
            longitude = lng
        )

        postRef.setValue(post).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("SavePost", "Post saved successfully")
                saveGeoLocation(postId, lat, lng)
            } else {
                Log.e("SavePost", "Failed to save post", task.exception)
            }
        }
    }

    private fun saveGeoLocation(postId: String, lat: Double, lng: Double) {
        val geoFire = GeoFire(geoFireRef)
        geoFire.setLocation(postId, GeoLocation(lat, lng)) { key, error ->
            if (error != null) {
                Log.e("GeoFire", "Error saving location for $key: ${error.message}")
            } else {
                Log.d("GeoFire", "Location saved successfully for $key")
            }
        }
    }

    /**
     * Add a post by ID to the observable list if it's not already included.
     */
    fun addPostToMonitor(postId: String) {
        if (postMap.containsKey(postId)) return

        postsRef.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    postMap[postId] = post
                    _posts.value = postMap.values.toList()
                    Log.d("PostMonitor", "Post added: ${post.foodName}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostMonitor", "Failed to load post: ${error.message}")
            }
        })
    }
    fun getListOfItem(context: Context): LiveData<MutableList<Post>> {
        val listLiveData = MutableLiveData<MutableList<Post>>()
        val database =  FirebaseDatabase.getInstance().getReference("posts")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = mutableListOf<Post>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(Post::class.java)
                    item?.let { itemList.add(it) }
                }
                val list=itemList.reversed().toMutableList()
                listLiveData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.makeToast(context, "Failed to read data: ${error.message}")
            }
        })

        return listLiveData
    }


    fun removePost(postId: String) {
        if (postMap.containsKey(postId)) {
            postMap.remove(postId)
            _posts.value = postMap.values.toList()
            Log.d("PostMonitor", "Post removed: $postId")
        }
    }


    fun updatePostLocation(postId: String, location: GeoLocation) {
        postMap[postId]?.let { post ->
            val updatedPost = post.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                geohash = GeoFireUtils.getGeoHashForLocation(location)
            )
            postMap[postId] = updatedPost
            _posts.value = postMap.values.toList()
            Log.d("PostMonitor", "Location updated for: $postId")
        }
    }
}
