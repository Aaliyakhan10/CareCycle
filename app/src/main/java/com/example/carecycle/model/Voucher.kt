package com.example.carecycle.model

data class Voucher(
val id: String = "",
val title: String = "",
val description: String = "",
val code: String = "",
val coinCost: Int = 0,
val userClaimed: Boolean = false

)
