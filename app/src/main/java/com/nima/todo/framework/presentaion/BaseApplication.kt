package com.nima.todo.framework.presentaion

import android.app.Application
import com.nima.todo.di.AppComponent
import com.nima.todo.di.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : Application(){

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent(){
        appComponent = DaggerAppComponent.factory().create(this)
    }


}