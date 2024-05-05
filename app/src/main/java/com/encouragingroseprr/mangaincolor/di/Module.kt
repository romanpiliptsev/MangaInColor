package com.encouragingroseprr.mangaincolor.di

import com.encouragingroseprr.mangaincolor.data.network.ApiFactory
import com.encouragingroseprr.mangaincolor.data.network.ApiService
import com.encouragingroseprr.mangaincolor.data.repository_impl.MainRepositoryImpl
import com.encouragingroseprr.mangaincolor.domain.repository.MainRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface Module {

    @Binds
    @ApplicationScope
    fun bindServiceRepository(impl: MainRepositoryImpl): MainRepository

    companion object {
        @Provides
        @ApplicationScope
        fun provideApiService(): ApiService {
            return ApiFactory.apiService
        }
    }
}