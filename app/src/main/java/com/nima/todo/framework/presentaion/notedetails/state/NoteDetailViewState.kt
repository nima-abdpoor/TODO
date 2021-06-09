package com.nima.todo.framework.presentaion.notedetails.state

import android.os.Parcelable
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteDetailViewState(

    var note: Note? = null,

    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState