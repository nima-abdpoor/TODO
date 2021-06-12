package com.nima.todo.business.interactors.splash

import com.nima.todo.business.data.cache.CacheResponseHandler
import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.ApiResponseHandler
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.data.util.safeApiCall
import com.nima.todo.business.data.util.safeCacheCall
import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.state.DataState
import com.nima.todo.business.domain.util.printLogD
import kotlinx.coroutines.Dispatchers

class SyncDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    suspend fun syncDeletedNotes() {
        val apiResult = safeApiCall(Dispatchers.IO) {
            noteNetworkDataSource.getDeletedNotes()
        }
        val response = object : ApiResponseHandler<List<Note>, List<Note>>(
            response = apiResult, stateEvent = null
        ) {
            override fun handleSuccess(resultObj: List<Note>): DataState<List<Note>> {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }

        }.getResult()
        val notes = response.data ?: ArrayList()
        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.deleteNotes(notes)
        }
        object : CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = null
        ) {
            override fun handleSuccess(resultObj: Int): DataState<Int>? {
                printLogD(
                    "SyncDeletedNotes",
                    "Number of Deleted Notes: $resultObj"
                )
                return DataState.data(
                    response = null,
                    data = null,
                    stateEvent = null
                )
            }
        }
    }
}