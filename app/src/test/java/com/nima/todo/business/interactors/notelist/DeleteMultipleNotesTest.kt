package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.model.NoteFactory
import com.nima.todo.business.domain.state.DataState
import com.nima.todo.business.interactors.notelist.DeleteMultipleNotes.Companion.DELETE_NOTE_SUCCESS
import com.nima.todo.di.DependencyContainer
import com.nima.todo.framework.presentaion.notelist.state.NoteListStateEvent
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/*
Test cases:
1. deleteNotes_success_confirmNetworkAndCacheUpdated()
    a) select a handful of random notes for deleting
    b) delete from cache and network
    c) confirm DELETE_NOTES_SUCCESS msg is emitted from flow
    d) confirm notes are delted from cache
    e) confirm notes are deleted from "notes" node in network
    f) confirm notes are added to "deletes" node in network
2. deleteNotes_fail_confirmCorrectDeletesMade()
    - This is a complex one:
        - The use-case will attempt to delete all notes passed as input. If there
        is an error with a particular delete, it continues with the others. But the
        resulting msg is DELETE_NOTES_ERRORS. So we need to do rigorous checks here
        to make sure the correct notes were deleted and the correct notes were not.
    a) select a handful of random notes for deleting
    b) change the ids of a few notes so they will cause errors when deleting
    c) confirm DELETE_NOTES_ERRORS msg is emitted from flow
    d) confirm ONLY the valid notes are deleted from network "notes" node
    e) confirm ONLY the valid notes are inserted into network "deletes" node
    f) confirm ONLY the valid notes are deleted from cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) select a handful of random notes for deleting
    b) force an exception to be thrown on one of them
    c) confirm DELETE_NOTES_ERRORS msg is emitted from flow
    d) confirm ONLY the valid notes are deleted from network "notes" node
    e) confirm ONLY the valid notes are inserted into network "deletes" node
    f) confirm ONLY the valid notes are deleted from cache
 */
class DeleteMultipleNotesTest {

    // system in test
    private var deleteMultipleNotes: DeleteMultipleNotes? = null

    // dependencies
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var noteCacheDataSource: NoteCacheDataSource
    private lateinit var noteNetworkDataSource: NoteNetworkDataSource
    private lateinit var noteFactory: NoteFactory

    @AfterEach
    fun afterEach() {
        deleteMultipleNotes = null
    }

    @BeforeEach
    fun beforeEach() {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        deleteMultipleNotes = DeleteMultipleNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @InternalCoroutinesApi
    @Test
    fun deleteNotes_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val randomNotes: ArrayList<Note> = ArrayList()
        val noteInCache = noteCacheDataSource.searchNotes("", "", 1)
        for (note in noteInCache) {
            randomNotes.add(note)
            if (randomNotes.size > 4) break
        }

        deleteMultipleNotes?.deleteNotes(
            randomNotes, NoteListStateEvent.DeleteMultipleNotesEvent(randomNotes)
        )?.collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(value?.stateMessage?.response?.message, DELETE_NOTE_SUCCESS)
            }
        })
        // confirm notes are deleted from cache
        for (note in randomNotes) {
            val noteInCache = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { noteInCache == null }
        }
        // confirm notes are deleted from "notes" node in network
        val doNotExistInNetwork = noteNetworkDataSource.getAllNotes().containsAll(randomNotes)
        assertFalse { doNotExistInNetwork }
    }
}