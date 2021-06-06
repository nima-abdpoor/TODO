package com.nima.todo.business.interactors.notelist

import com.nima.todo.business.data.cache.CacheResponseHandler
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.util.safeCacheCall
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.state.*
import com.nima.todo.framework.presentaion.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {
    fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        var updatePage = page
        if (page <= 0) {
            updatePage = 1
        }
        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.searchNotes(
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatePage
            )
        }
        val response = object : CacheResponseHandler<NoteListViewState, List<Note>>(
        response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState> {
                var message :String? = SEARCH_NOTES_SUCCESS
                var uiComponentType : UIComponentType = UIComponentType.None()
                if (resultObj.isEmpty()){
                    message = SEARCH_NOTES_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast()
                }
                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = NoteListViewState(
                        noteList = ArrayList(resultObj)
                    ),
                    stateEvent = stateEvent
                )
            }
        }.getResult()
        emit(response)
    }

    companion object {
        val SEARCH_NOTES_SUCCESS = "Successfully retrieved list of notes."
        val SEARCH_NOTES_NO_MATCHING_RESULTS = "There are no notes that match that query."
        val SEARCH_NOTES_FAILED = "Failed to retrieve the list of notes."
    }
}