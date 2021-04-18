package com.nima.todo.business.domain.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateUtil
@Inject
constructor(
    private val dataFormat: SimpleDateFormat
) {

    fun removeTimeFromDataString(sd: String): String {
        return sd.substring(0, sd.indexOf(" "))
    }

    fun convertFirebaseTimestampToStringDate(timestamp: Timestamp): String {
        return dataFormat.format(timestamp.toDate())
    }

    fun convertStringToTimestamp(date: String): Timestamp {
        val d = dataFormat.parse(date) ?: Date()
        return Timestamp(d)
    }

    fun getCurrentTimestamp(): String {
        return dataFormat.format(Date())
    }
}