package com.nima.todo.business.data.cache

import com.nima.todo.business.domain.state.DataState
import com.nima.todo.business.domain.state.StateEvent

abstract class CacheResponseHandler<ViewState , Data>(
    private val response : CacheResult<Data?> ,
    private val stateEvent: StateEvent?
){
    suspend fun getResult()  :DataState<ViewState>?{

    }
}