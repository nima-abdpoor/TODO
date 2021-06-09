package com.nima.todo.business.interactors.notedetail

import com.nima.todo.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.model.NoteFactory
import com.nima.todo.business.domain.state.DataState
import com.nima.todo.business.interactors.notedetails.UpdateNote
import com.nima.todo.business.interactors.notedetails.UpdateNote.Companion.UPDATE_NOTE_FAILED
import com.nima.todo.business.interactors.notedetails.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import com.nima.todo.data.cache.FORCE_UPDATE_NOTE_EXCEPTION
import com.nima.todo.di.DependencyContainer
import com.nima.todo.framework.presentaion.notedetails.state.NoteDetailStateEvent
import com.nima.todo.framework.presentaion.notedetails.state.NoteDetailViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. updateNote_success_confirmNetworkAndCacheUpdated()
    a) select a random note from the cache
    b) update that note
    c) confirm UPDATE_NOTE_SUCCESS msg is emitted from flow
    d) confirm note is updated in network
    e) confirm note is updated in cache
2. updateNote_fail_confirmNetworkAndCacheUnchanged()
    a) attempt to update a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) attempt to update a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
 */
@InternalCoroutinesApi
class UpdateNoteTest {
    // system in test
    private val updateNote: UpdateNote

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
        updateNote = UpdateNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun updateNote_success_confirmNetworkAndCacheUpdated() = runBlocking {

        val randomNote = noteCacheDataSource.searchNotes("", "", 1)
            .get(0)
        val updatedNote = Note(
            id = randomNote.id,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = dependencyContainer.dateUtil.getCurrentTimestamp(),
            created_at = randomNote.created_at
        )
        updateNote.updateNote(
            note = updatedNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    UPDATE_NOTE_SUCCESS
                )
            }
        })

        // confirm cache was updated
        val cacheNote = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { cacheNote == updatedNote }

        // confirm that network was updated
        val networkNote = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { networkNote == updatedNote }
    }

    @Test
    fun updateNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {
        // create a note that doesnt exist in cache
        val noteToUpdate = Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        updateNote.updateNote(
            note = noteToUpdate,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    UPDATE_NOTE_FAILED
                )
            }
        })
        // confirm nothing updated in cache
        val cacheNote = noteCacheDataSource.searchNoteById(noteToUpdate.id)
        assertTrue { cacheNote == null }

        // confirm nothing updated in network
        val networkNote = noteNetworkDataSource.searchNote(noteToUpdate)
        assertTrue { networkNote == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        // create a note that doesn't exist in cache
        val noteToUpdate = Note(
            id = FORCE_UPDATE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        updateNote.updateNote(
            note = noteToUpdate,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm nothing updated in cache
        val cacheNote = noteCacheDataSource.searchNoteById(noteToUpdate.id)
        assertTrue { cacheNote == null }

        // confirm nothing updated in network
        val networkNote = noteNetworkDataSource.searchNote(noteToUpdate)
        assertTrue { networkNote == null }
    }
}