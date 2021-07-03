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

class DeleteMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    private var onDeleteError: Boolean = false
    fun deleteNotes(
        notes: List<Note>,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val successfulDelete: ArrayList<Note> = ArrayList()
        for (note in notes) {
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                noteCacheDataSource.deleteNote(note.id)
            }
            val response = object : CacheResponseHandler<NoteListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
                    if (resultObj < 0) {
                        //error
                        onDeleteError = true
                    } else {
                        //success
                        successfulDelete.add(note)
                    }
                    return null
                }

            }.getResult()
            if (response?.stateMessage?.response?.message?.contains(stateEvent.errorInfo()) == true)
                onDeleteError = true
        }
        if (onDeleteError) {
            emit(
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTE_FAILED,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }else{
            emit( DataState.data<NoteListViewState>(
                response = Response(
                    message = DELETE_NOTE_SUCCESS,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Success()
                ),
                data = null,
                stateEvent = stateEvent
            ))
        }
        updateNetwork(successfulDelete)
    }

    private suspend fun updateNetwork(successfulDelete: java.util.ArrayList<Note>) {
        for (note in successfulDelete){
            //delete for 'notes' node
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.deleteNote(note.id)
            }
            //insert into the 'deleted notes'
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object{
        val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        val DELETE_NOTE_PENDING = "Delete pending..."
        val DELETE_NOTE_FAILED = "Failed to delete note."
        val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"
        val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
    }
}