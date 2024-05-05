package com.encouragingroseprr.mangaincolor.presentation.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encouragingroseprr.mangaincolor.domain.usecases.GetResultImageUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val getResultImageUseCase: GetResultImageUseCase
) : ViewModel() {

    private val _getImageStateLiveData = MutableLiveData<GetImageState>()
    val getImageStateLiveData: LiveData<GetImageState>
        get() = _getImageStateLiveData

    sealed interface GetImageState {
        object Error : GetImageState
        object Loading : GetImageState
        class Loaded(val imageBitmap: Bitmap?) : GetImageState
    }

    private val getMatchesListHandler = CoroutineExceptionHandler { _, th ->
        _getImageStateLiveData.value = GetImageState.Error
        Log.e("MIC VM exception", th.toString())
    }

    fun getResultImage(file: File, type: String) {
        _getImageStateLiveData.value = GetImageState.Loading

        viewModelScope.launch(getMatchesListHandler) {
            _getImageStateLiveData.value =
                GetImageState.Loaded(getResultImageUseCase.invoke(file, type))
        }
    }
}