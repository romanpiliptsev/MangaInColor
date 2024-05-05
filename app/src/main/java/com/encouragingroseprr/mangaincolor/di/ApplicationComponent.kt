package com.encouragingroseprr.mangaincolor.di

import com.encouragingroseprr.mangaincolor.presentation.activities.ArActivity
import com.encouragingroseprr.mangaincolor.presentation.activities.MainActivity
import com.encouragingroseprr.mangaincolor.presentation.fragments.MenuFragment
import dagger.Component

@ApplicationScope
@Component(modules = [Module::class, ViewModelModule::class])
interface ApplicationComponent {

    fun inject(activity: MainActivity)
    fun inject(activity: ArActivity)
    fun inject(fragment: MenuFragment)
}