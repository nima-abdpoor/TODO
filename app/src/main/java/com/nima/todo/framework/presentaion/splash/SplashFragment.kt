package com.nima.todo.framework.presentaion.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.nima.todo.R
import com.nima.todo.business.domain.state.DialogInputCaptureCallback
import com.nima.todo.business.domain.util.printLogD
import com.nima.todo.framework.datasource.network.implementation.NoteFirestoreServiceImp.Companion.EMAIL
import com.nima.todo.framework.presentaion.common.BaseNoteFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
class SplashFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseNoteFragment(R.layout.fragment_splash) {

    val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
    }

    private fun checkFirebaseAuth() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            displayCapturePassword()
        } else {
            subscribeObservers()
        }
    }

    // add password input b/c someone used my firestore and deleted the data
    private fun displayCapturePassword() {
        uiController.displayInputCaptureDialog(
            getString(R.string.text_enter_password),
            object : DialogInputCaptureCallback {
                override fun onTextCaptured(text: String) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(EMAIL, text)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                printLogD(
                                    "MainActivity",
                                    "Signing in to Firebase: ${it.result}"
                                )
                                subscribeObservers()
                            } else {
                                printLogD("MainActivity", "cannot log in")
                            }
                        }
                }
            }
        )
    }

    private fun subscribeObservers() {
        viewModel.hasSyncBeenExecuted().observe(viewLifecycleOwner, { hasSyncBeenExecuted ->

            if (hasSyncBeenExecuted) {
                navNoteListFragment()
            }
        })
    }

    private fun navNoteListFragment() {
        findNavController().navigate(R.id.action_splashFragment_to_noteListFragment)
    }

    override fun inject() {
        getAppComponent().inject(this)
    }

}
