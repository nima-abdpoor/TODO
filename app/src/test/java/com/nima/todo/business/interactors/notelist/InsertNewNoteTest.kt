package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.data.cache.CacheErrors
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.NoteFactory
import com.nima.todo.business.domain.state.DataState
import com.nima.todo.business.interactors.notelist.InsertNewNotes.Companion.INSERT_NOTE_FAILED
import com.nima.todo.business.interactors.notelist.InsertNewNotes.Companion.INSERT_NOTE_SUCCESS
import com.nima.todo.data.cache.FORCE_GENERAL_FAILURE
import com.nima.todo.data.cache.FORCE_NEW_NOTE_EXCEPTION
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
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */

@InternalCoroutinesApi
class InsertNewNoteTest {
    private val insertNewNote: InsertNewNotes

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
        insertNewNote = InsertNewNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }

    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            id = null,
            title = UUID.randomUUID().toString()
        )
        insertNewNote.insertNewNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        )
            .collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        INSERT_NOTE_SUCCESS
                    )
                }
            }
            )
        //confirm cache was updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == newNote }

        //confirm network was updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == newNote }
    }


    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            id = FORCE_GENERAL_FAILURE,
            title = UUID.randomUUID().toString()
        )
        insertNewNote.insertNewNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        )
            .collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        INSERT_NOTE_FAILED
                    )
                }
            }
            )
        //confirm cache was NOT updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }

        //confirm network was NOT updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged()= runBlocking {
        val newNote = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString()
        )
        insertNewNote.insertNewNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        )
            .collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    assert(
                        value?.stateMessage?.response?.message?.contains(
                            CacheErrors.CACHE_ERROR_UNKNOWN
                        )?: false
                    )
                }
            }
            )
        //confirm cache was NOT updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }

        //confirm network was NOT updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null }
    }
}