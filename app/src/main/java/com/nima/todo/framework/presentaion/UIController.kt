package com.nima.todo.framework.presentaion

import com.nima.todo.business.domain.state.DialogInputCaptureCallback
import com.nima.todo.business.domain.state.Response
import com.nima.todo.business.domain.state.StateMessageCallback

interface   UIController {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

}