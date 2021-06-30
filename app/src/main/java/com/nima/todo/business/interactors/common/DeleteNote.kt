package com.nima.todo.business.interactors.common

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

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {
        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.deleteNote(note.id)
        }
        val response = object : CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(resultObj: Int): DataState<ViewState> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_FAILURE,
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
        updateNetwork(
            message = response?.stateMessage?.response?.message,
            note = note
        )
    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if (message == DELETE_NOTE_SUCCESS) {
            //delete form 'notes' node
            safeApiCall(Dispatchers.IO) {
                noteNetworkDataSource.deleteNote(note.id)
            }
            //insert into 'deleted' node
            safeApiCall(Dispatchers.IO) {
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object {
        const val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        const val DELETE_NOTE_FAILURE = "Failed to delete note."
    }
}