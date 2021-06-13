package com.nima.todo.business.interactors.notedetails

import com.nima.todo.business.interactors.common.DeleteNote
import com.nima.todo.framework.presentaion.notedetails.state.NoteDetailViewState

class NoteDetailInteractors(
    val deleteNote : DeleteNote<NoteDetailViewState>,
    val updateNote: UpdateNote
) {
}