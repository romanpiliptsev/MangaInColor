package com.encouragingroseprr.mangaincolor

import android.app.Application
import com.encouragingroseprr.mangaincolor.di.ApplicationComponent
import com.encouragingroseprr.mangaincolor.di.DaggerApplicationComponent

class MangaInColorApp : Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .build()
    }
}