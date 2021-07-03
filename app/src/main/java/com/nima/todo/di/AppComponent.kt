package com.nima.todo.di

import com.nima.todo.framework.presentaion.MainActivity
import com.nima.todo.framework.presentaion.BaseApplication
import com.nima.todo.framework.presentaion.notedetails.NoteDetailFragment
import com.nima.todo.framework.presentaion.notelist.NoteListFragment
import com.nima.todo.framework.presentaion.splash.NoteNetworkSyncManager
import com.nima.todo.framework.presentaion.splash.SplashFragment
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
@FlowPreview
@Component(modules = [
    AppModule::class,ProductionModule::class,
    AppModule::class,
    NoteViewModelModule::class,
    NoteFragmentFactoryModule::class
])
interface AppComponent {

    val noteNetworkSync: NoteNetworkSyncManager

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(splashFragment: SplashFragment)

    fun inject(noteListFragment: NoteListFragment)

    fun inject(noteDetailFragment: NoteDetailFragment)
}