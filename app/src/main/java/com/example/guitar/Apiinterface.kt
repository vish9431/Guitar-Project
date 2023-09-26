package com.example.guitar

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface Apiinterface {
    @Multipart
    @POST("predict")
     fun uploadAudio(@Part audio: MultipartBody.Part): Response<responseDataClass>
}
