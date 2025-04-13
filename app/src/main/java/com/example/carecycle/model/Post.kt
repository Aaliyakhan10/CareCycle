package com.example.carecycle.model

data class Post(
    val id: String = "",
    val title: String = "",
    var numOfPlates: Int=0,
    var delivering: Boolean=false,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val geohash: String = ""
)
