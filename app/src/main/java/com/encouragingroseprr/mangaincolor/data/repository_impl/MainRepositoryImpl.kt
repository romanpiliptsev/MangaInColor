package com.encouragingroseprr.mangaincolor.data.repository_impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.encouragingroseprr.mangaincolor.data.network.ApiService
import com.encouragingroseprr.mangaincolor.domain.repository.MainRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MainRepository {

    override suspend fun getResultImage(file: File, type: String): Bitmap? {
        if (type !in listOf(MEDIA_TYPE_JPEG, MEDIA_TYPE_JPG, MEDIA_TYPE_PNG)) {
            throw IllegalArgumentException(UNSUPPORTED_FILE_TYPE)
        }

        val requestBody = RequestBody.create(
            type.toMediaTypeOrNull(),
            file
        )

        val imagePart = MultipartBody.Part.createFormData(
            FILE,
            file.name,
            requestBody
        )

        var bitmap: Bitmap? = null
        val call = apiService.processImage(imagePart)

        if (call.isSuccessful) {
            // Обработка ответа сервера
            val processedImageBytes = call.body()?.bytes()
            bitmap = BitmapFactory.decodeByteArray(
                processedImageBytes,
                0,
                processedImageBytes?.size ?: 0
            )
        } else {
            Log.i("MIC", "getResultImage ${call.raw()}")
        }

        return bitmap
    }

    companion object {

        private const val FILE = "file"
        private const val MEDIA_TYPE_PNG = "image/png"
        private const val MEDIA_TYPE_JPG = "image/jpg"
        private const val MEDIA_TYPE_JPEG = "image/jpeg"
        private const val UNSUPPORTED_FILE_TYPE = "Unsupported file type"
    }
}