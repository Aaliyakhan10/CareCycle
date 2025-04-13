package com.example.carecycle.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carecycle.R
import com.example.carecycle.Utils
import com.example.carecycle.adapter.VoucherAdapter
import com.example.carecycle.databinding.FragmentVoucherBinding
import com.example.carecycle.databinding.FragmentVoucherClaimedBinding
import com.example.carecycle.model.Voucher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VoucherClaimedFragment : Fragment() {
    private lateinit var binding: FragmentVoucherBinding
    private lateinit var voucherAdapter: VoucherAdapter
    private val vouchers = mutableListOf<Voucher>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVoucherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voucherAdapter = VoucherAdapter(vouchers) {
            // Optional: refresh list after claim
            loadVouchers()
        }

        binding.voucherRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = voucherAdapter
        }

        loadVouchers()
    }

    private fun loadVouchers() {

        val uid= Utils.getCurrentUsersId()
        val db = FirebaseDatabase.getInstance().getReference("claimedVouchers").child(uid)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vouchers.clear()
                for (snap in snapshot.children) {
                    val voucher = snap.getValue(Voucher::class.java)
                    voucher?.let { vouchers.add(it) }
                }
                voucherAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load vouchers", Toast.LENGTH_SHORT).show()
            }
        })
    }

}