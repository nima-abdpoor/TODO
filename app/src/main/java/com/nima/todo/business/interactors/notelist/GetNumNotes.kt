package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.data.cache.CacheResponseHandler
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.util.safeCacheCall
import com.nima.todo.business.domain.state.*
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNumNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {

    fun getNumNotes(stateEvent: StateEvent)
            : Flow<DataState<NoteListViewState>?> = flow {
                val cacheResult = safeCacheCall(Dispatchers.IO){
                    noteCacheDataSource.getNumNotes()
                }
        val response  =object :CacheResponseHandler<NoteListViewState,Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override fun handleSuccess(resultObj: Int): DataState<NoteListViewState> {
                val viewState = NoteListViewState(
                    numNotesInCache = resultObj
                )
                return DataState.data(
                    response = Response(
                        message = GET_NUM_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.None(),
                        messageType = MessageType.Success()
                    ),
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.getResult()
        emit(response)
    }
    companion object{
        val GET_NUM_NOTES_SUCCESS = "Successfully retrieved the number of notes from the cache."
        val GET_NUM_NOTES_FAILED = "Failed to get the number of notes from the cache."
    }
}