package com.example.carecycle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carecycle.databinding.ExpiredItemRvBinding
import com.example.carecycle.model.ExpiredPostData

class ExpiredItemAdapter(val mutableItemList: MutableList<ExpiredPostData>) :
    RecyclerView.Adapter<ExpiredItemAdapter.ExpiredItemViewHolder>() {
    class ExpiredItemViewHolder(val binding: ExpiredItemRvBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpiredItemAdapter.ExpiredItemViewHolder {
        return ExpiredItemViewHolder(
            ExpiredItemRvBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ExpiredItemAdapter.ExpiredItemViewHolder,
        position: Int
    ) {
        val currentItem = mutableItemList[position]

        holder.binding.apply {


            itemName.text = currentItem.name
            itemType.text = currentItem.type
            itemQty.text = currentItem.quantity.toString()
            itemExpiry.text = currentItem.expiryDate
            statusButton.text = currentItem.status
        }

    }

    override fun getItemCount(): Int {
        return mutableItemList.size
    }

}