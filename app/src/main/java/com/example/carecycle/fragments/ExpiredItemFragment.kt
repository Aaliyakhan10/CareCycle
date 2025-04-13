package com.example.carecycle.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carecycle.Utils
import com.example.carecycle.adapter.ExpiredItemAdapter
import com.example.carecycle.databinding.ExpiredItemUploadBinding
import com.example.carecycle.databinding.FragmentExpiredItemBinding
import com.example.carecycle.model.ExpiredPostData
import com.example.carecycle.viewmodel.ExpiredItemViewModel
import kotlinx.coroutines.launch
import java.util.Calendar


class ExpiredItemFragment : Fragment() {
    private lateinit var binding: FragmentExpiredItemBinding
    private val viewModel: ExpiredItemViewModel by viewModels()

    private lateinit var mutableItemList: MutableList<ExpiredPostData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExpiredItemBinding.inflate(layoutInflater, container, false)
        onAddButtonClick()

        binding.expiredItemRv.layoutManager= LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
        binding.expiredItemRv.setHasFixedSize(true)
        getExpiredItemList()
        return binding.root
    }

    private fun getExpiredItemList() {
        val uid = Utils.getCurrentUsersId()
        mutableItemList = mutableListOf()

        lifecycleScope.launch {
            viewModel.getListOFExpireItem(requireContext()).observe(viewLifecycleOwner, Observer { lists ->
                binding.expiredItemRv.adapter = ExpiredItemAdapter(lists)
            })
        }
    }


    private fun onAddButtonClick() {
        binding.expiredIBtntemAdd.setOnClickListener {
            val layoutExpiredBinding = ExpiredItemUploadBinding.inflate(layoutInflater)
            var alertDialog: AlertDialog? = null
            alertDialog = AlertDialog.Builder(requireContext())
                .setView(layoutExpiredBinding.root)
                .create()
            alertDialog.show()

            var expiryDate: String = ""

            // Open DatePicker when clicked
            layoutExpiredBinding.editTextExpiryDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                        layoutExpiredBinding.editTextExpiryDate.text = formattedDate
                        expiryDate = formattedDate
                        Utils.makeToast(requireContext(), "Expiry date selected: $expiryDate")
                    },
                    year, month, day
                )

                datePickerDialog.show()
            }

            layoutExpiredBinding.submitButton.setOnClickListener {
                val itemQty = layoutExpiredBinding.editTextQuantity.text.toString().toIntOrNull()
                val itemType = layoutExpiredBinding.editTextItemType.text.toString()
                val itemName = layoutExpiredBinding.editTextItemName.text.toString()

                if (itemQty != null && itemQty != 0 && itemType.isNotEmpty() && expiryDate.isNotEmpty() && itemName.isNotEmpty()) {
                    val uid = Utils.getCurrentUsersId().toString()
                    val expiredPost = ExpiredPostData(
                        uid,
                        itemType,
                        itemName,
                        itemQty,
                        expiryDate,
                        ExpiredItemStatus.PENDING_REVIEW.toString()
                    )

                    viewModel.UploadExpiredDatabase(expiredPost, requireContext())
                    alertDialog?.dismiss()
                } else {
                    Utils.makeToast(requireContext(), "Please select all the fields")
                }
            }
        }
    }



    enum class ExpiredItemStatus {
        PENDING_REVIEW,
        SCHEDULED_FOR_PICKUP,
        PICKUP_IN_PROGRESS,
        COLLECTED,
        PROCESSING,
        REWARD_CREDITED
    }

}
