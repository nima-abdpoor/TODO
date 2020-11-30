package com.nima.todo.business.domain.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nima.todo.business.domain.util.Constants.DEBUG
import com.nima.todo.business.domain.util.Constants.TAG

var isUnitTest = false

fun printLogD(className: String?, message: String ) {
    if (DEBUG && !isUnitTest) {
        Log.d(TAG, "$className: $message")
    }
    else if(DEBUG && isUnitTest){
        println("$className: $message")
    }
}

/*
    Priorities: Log.DEBUG, Log. etc....
 */
fun cLog(msg: String?){
    msg?.let {
        if(!DEBUG){
            FirebaseCrashlytics.getInstance().log(it)
        }
    }

}