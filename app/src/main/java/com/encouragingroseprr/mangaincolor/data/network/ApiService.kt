package com.encouragingroseprr.mangaincolor.data.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST(PROCESS)
    suspend fun processImage(@Part file: MultipartBody.Part): Response<ResponseBody>

    companion object {

        private const val PROCESS = "process_images"
    }
}