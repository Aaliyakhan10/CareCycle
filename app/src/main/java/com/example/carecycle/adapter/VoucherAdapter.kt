package com.example.carecycle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.carecycle.Utils
import com.example.carecycle.databinding.VoucherItemBinding
import com.example.carecycle.model.Voucher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class VoucherAdapter(
    private val vouchers: List<Voucher>,
    private val onClaimed: () -> Unit
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    inner class VoucherViewHolder(val binding: VoucherItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val binding = VoucherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoucherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        val context = holder.binding.root.context

        holder.binding.apply {
            voucherTitle.text = voucher.title
            voucherDescription.text = voucher.description


            if (voucher.userClaimed) {
                btnClaim.text = "Already claimed"
                voucherCode.text = "Code: ${voucher.code}"

            }else{
                voucherCode.visibility= View.GONE
                btnClaim.text = "Claim for ${voucher.coinCost} Coins"
            }
            btnClaim.setOnClickListener {
                if (voucher.userClaimed) {
                    btnClaim.text = "Already claimed"
                    Toast.makeText(context, "Already claimed!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val userId = Utils.getCurrentUsersId()
                FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val userCoins = snapshot.child("coins").getValue(Int::class.java) ?: 0

                        if (userCoins >= voucher.coinCost) {
                            performCoinTransaction(userId, voucher, context)
                        } else {
                            Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show()
                        }
                    }
            }


        }
    }
    private fun performCoinTransaction(
        userId: String,
        voucher: Voucher,
        context: Context
    ) {
        val db = FirebaseDatabase.getInstance().reference

        db.child("users").child(userId).child("coins")
            .runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentCoins = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = currentCoins - voucher.coinCost
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (committed) {
                        val voucher= Voucher(voucher.id,voucher.title,voucher.description,voucher.code, voucher.coinCost, userClaimed = true)
                        db.child("claimedVouchers").child(userId).child(voucher.id)
                            .setValue(voucher)

                        Toast.makeText(context, "Voucher claimed!", Toast.LENGTH_SHORT).show()
                        onClaimed()
                    } else {
                        Toast.makeText(context,
                            "Failed to claim. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    override fun getItemCount() = vouchers.size
}

