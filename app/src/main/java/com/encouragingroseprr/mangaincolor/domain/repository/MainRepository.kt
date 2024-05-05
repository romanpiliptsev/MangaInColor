package com.encouragingroseprr.mangaincolor.domain.repository

import android.graphics.Bitmap
import java.io.File

interface MainRepository {

    suspend fun getResultImage(file: File, type: String): Bitmap?
}