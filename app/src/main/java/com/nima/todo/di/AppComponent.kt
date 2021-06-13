package com.nima.todo.di

import com.nima.todo.framework.presentaion.MainActivity
import com.nima.todo.framework.presentaion.BaseApplication
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
@FlowPreview
@Component(modules = [
    AppModule::class,ProductionModule::class
])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}