package com.example.carecycle.model
data class Post(
    var id: String = "",
    val userId: String = "",
    val foodName: String = "",
    val description: String = "",
    var platesAvailable: Int = 0,
    val geohash: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var claimants: MutableMap<String, Long> = mutableMapOf()
)
