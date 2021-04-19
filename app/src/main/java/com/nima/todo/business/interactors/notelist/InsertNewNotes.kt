package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.model.NoteFactory
import com.nima.todo.business.domain.state.*
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class InsertNewNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory
) {
    fun insertNewNote(
        id : String? = null ,
        title : String ,
        stateEvent: StateEvent
    ):Flow<DataState<NoteListViewState>> = flow{
        val newNote = noteFactory.createSingleNote(
            id = id ?: UUID.randomUUID().toString(),
            title  = title,
            body = ""
        )
        val insertResult = noteCacheDataSource.insertNote(newNote)
        var cacheResponse : DataState<NoteListViewState>? = null
        if (insertResult > 0){
            val viewState = NoteListViewState(
                newNote = newNote
            )
            cacheResponse = DataState.data(
                response = Response(
                    message = INSERT_NOTE_SUCCESS ,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Success()
                ),
                data =  viewState,
                stateEvent = stateEvent
            )
            updateNetwork(newNote)
        }
        else{
            cacheResponse =  DataState.data(
                response = Response(
                    message = INSERT_NOTE_FAILED ,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Error()
                ),
                data =  null,
                stateEvent = stateEvent
            )
        }
        emit(cacheResponse)
    }

    private suspend fun updateNetwork(newNote: Note) {
        noteNetworkDataSource.insertOrUpdateNote(newNote)
    }

    companion object{
        const val INSERT_NOTE_SUCCESS = "Successfully inserted new Note"
        const val INSERT_NOTE_FAILED = "Failed to insert new Note"
    }
}