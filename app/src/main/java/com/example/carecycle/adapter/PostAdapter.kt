package com.example.carecycle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carecycle.R
import com.example.carecycle.Utils
import com.example.carecycle.model.ExpiredPostData
import com.example.carecycle.model.Post
import com.google.firebase.database.FirebaseDatabase

class PostAdapter(
    var mutableItemList: MutableList<Post>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvPlates: TextView = view.findViewById(R.id.tvPlates)
        val btnClaim: Button = view.findViewById(R.id.btnClaim)
        val tvClaimants: TextView = view.findViewById(R.id.tvClaimants)
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        val typeOfuser: TextView=view.findViewById(R.id.typeofUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = mutableItemList[position]
        val uid = post.userId
        val firebaseDatabase = FirebaseDatabase.getInstance().getReference("users")

        firebaseDatabase.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                holder.tvUsername.text = it.child("name").value?.toString() ?: "User"

                val imageUrl = it.child("profileImageUrl").value?.toString()
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.person)
                    .into(holder.ivProfile)
                holder.typeOfuser.text= it.child("userType").value?.toString()?: "Individual"
            }
        }

        holder.tvDescription.text = post.description
        holder.tvPlates.text = "Plates available: ${post.platesAvailable}"
        holder.tvClaimants.text = "${post.claimants.size} claimants"
        holder.btnClaim.isEnabled = post.platesAvailable > 0

        holder.btnClaim.setOnClickListener {

            Utils.successToast(
                holder.itemView.context,
                "Okay, we will deliver it to you or you can come to take it if you want."
            )
        }
    }

    override fun getItemCount(): Int = mutableItemList.size

    fun updateList(newList: List<Post>) {
        mutableItemList.clear()
        mutableItemList.addAll(newList)
        notifyDataSetChanged()
    }
}
