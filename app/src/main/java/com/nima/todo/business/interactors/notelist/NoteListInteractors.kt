package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.interactors.common.DeleteNote
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState

class NoteListInteractors(
    val insertNewNote:InsertNewNotes,
    val deletedNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteMultipleNotes: DeleteMultipleNotes
) {
}