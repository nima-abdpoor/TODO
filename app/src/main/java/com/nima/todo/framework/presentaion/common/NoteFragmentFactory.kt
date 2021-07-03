package com.nima.todo.framework.presentaion.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.nima.todo.business.domain.util.DateUtil
import com.nima.todo.framework.presentaion.notedetails.NoteDetailFragment
import com.nima.todo.framework.presentaion.notelist.NoteListFragment
import com.nima.todo.framework.presentaion.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class NoteFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
): FragmentFactory(){

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when(className){

            NoteListFragment::class.java.name -> {
                val fragment = NoteListFragment(viewModelFactory, dateUtil)
                fragment
            }

            NoteDetailFragment::class.java.name -> {
                val fragment = NoteDetailFragment(viewModelFactory)
                fragment
            }

            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                fragment
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }
}