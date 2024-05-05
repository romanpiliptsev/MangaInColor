package com.encouragingroseprr.mangaincolor.di

import androidx.lifecycle.ViewModel
import com.encouragingroseprr.mangaincolor.presentation.viewmodels.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(viewModel: MainViewModel): ViewModel
}