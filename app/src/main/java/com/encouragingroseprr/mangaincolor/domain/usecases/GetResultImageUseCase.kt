package com.encouragingroseprr.mangaincolor.domain.usecases

import com.encouragingroseprr.mangaincolor.domain.repository.MainRepository
import java.io.File
import javax.inject.Inject

class GetResultImageUseCase @Inject constructor(private val repository: MainRepository) {

    suspend operator fun invoke(file: File, type: String) = repository.getResultImage(file, type)
}