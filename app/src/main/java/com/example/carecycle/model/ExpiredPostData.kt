package com.example.carecycle.model

data class ExpiredPostData(
    val uid: String = "",
    val type: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val expiryDate: String = "",
    val status: String = ""
)
