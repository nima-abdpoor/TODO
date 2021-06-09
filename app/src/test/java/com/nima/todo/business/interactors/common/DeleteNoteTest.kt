package com.nima.todo.business.interactors.common

import com.nima.todo.business.data.cache.CacheErrors
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.model.NoteFactory
import com.nima.todo.business.domain.state.DataState
import com.nima.todo.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_FAILURE
import com.nima.todo.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import com.nima.todo.data.cache.FORCE_DELETES_NOTE_EXCEPTION
import com.nima.todo.di.DependencyContainer
import com.nima.todo.framework.presentaion.notelist.state.NoteListStateEvent
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. deleteNote_success_confirmNetworkUpdated()
    a) delete a note
    b) check for success message from flow emission
    c) confirm note was deleted from "notes" node in network
    d) confirm note was added to "deletes" node in network
2. deleteNote_fail_confirmNetworkUnchanged()
    a) attempt to delete a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
3. throwException_checkGenericError_confirmNetworkUnchanged()
    a) attempt to delete a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm network was not changed
 */
@InternalCoroutinesApi
class DeleteNoteTest {

    private val deleteNote: DeleteNote<NoteListViewState>


    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        deleteNote = DeleteNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }


    @Test
    fun deleteNote_success_confirmNetworkUpdated() = runBlocking {
        val noteToDelete = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = "",
            page = 1
        )[0]
        deleteNote.deleteNote(
            note = noteToDelete,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(value?.stateMessage?.response?.message, DELETE_NOTE_SUCCESS)
            }
        })

        //confirm was deleted from 'notes' node
        val wasNoteDeleted = !noteNetworkDataSource.getAllNotes().contains(noteToDelete)
        assertTrue { wasNoteDeleted }

        //confirm was inserted to 'delete' node
        val wasDeletedNoteInserted = noteNetworkDataSource.getAllNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }

    }

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() = runBlocking {
        val noteToDelete = Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        deleteNote.deleteNote(
            note = noteToDelete,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(value?.stateMessage?.response?.message, DELETE_NOTE_FAILURE)
            }
        })

        //confirm was not deleted from 'notes' node
        val notes = noteNetworkDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == notes.size }

        //confirm was not inserted to 'delete' node
        val wasDeletedNoteInserted = !noteNetworkDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }

    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking {
        val noteToDelete = Note(
            id = FORCE_DELETES_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        deleteNote.deleteNote(
            note = noteToDelete,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN)?:false
                )
            }
        })

        //confirm was not deleted from 'notes' node
        val notes = noteNetworkDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == notes.size }

        //confirm was not inserted to 'delete' node
        val wasDeletedNoteInserted = !noteNetworkDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }
}