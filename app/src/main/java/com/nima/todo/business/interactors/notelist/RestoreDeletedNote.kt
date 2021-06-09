package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.data.cache.CacheResponseHandler
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.data.util.safeApiCall
import com.nima.todo.business.data.util.safeCacheCall
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.state.*
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    fun restoreDeletedNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.insertNote(note)
        }
        val response = object : CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {
                return if (resultObj > 0) {
                    val viewState = NoteListViewState(
                        notePendingDelete = NoteListViewState.NotePendingDelete(
                            note = note
                        )
                    )
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }

        }.getResult()

        emit(response)
        updateNetwork(response?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(response: String?, note: Note) {
        if (response == RESTORE_NOTE_SUCCESS) {
            // insert into "notes" node
            safeApiCall(Dispatchers.IO) {
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
            // remove from "deleted" node
            safeApiCall(Dispatchers.IO) {
                noteNetworkDataSource.deleteDeletedNote(note)
            }
        }
    }

    companion object {
        val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."
    }
}