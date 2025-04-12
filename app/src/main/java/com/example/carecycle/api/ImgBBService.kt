package com.example.carecycle.api

import com.example.carecycle.model.ImgBBResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImgBBService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("key") apiKey: RequestBody
    ): Response<ImgBBResponse>
}
