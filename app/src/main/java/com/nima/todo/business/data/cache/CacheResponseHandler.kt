package com.nima.todo.business.data.cache

import com.nima.todo.business.data.cache.CacheErrors.CACHE_DATA_NULL
import com.nima.todo.business.domain.state.*

abstract class CacheResponseHandler<ViewState , Data>(  
    private val response : CacheResult<Data?> ,
    private val stateEvent: StateEvent?
){
    suspend fun getResult()  :DataState<ViewState>?{
        return when(response){
            is CacheResult.Error ->errorHandling(response)
            is CacheResult.Success -> successHandling(response)
        }
    }

    private fun errorHandling(response: CacheResult.Error): DataState<ViewState>? {
        return DataState.error(
            response = Response(
                message = "${stateEvent?.errorInfo()}\n\n Reason : ${response.errorMessage}",
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateEvent =stateEvent
        )
    }

     private fun successHandling(response: CacheResult.Success<Data?>): DataState<ViewState>? {
         response.value?:let {
            return DataState.error(
                response = Response(
                    message = "${stateEvent?.errorInfo()}\n\n Reason : $CACHE_DATA_NULL",
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                ),
                stateEvent =stateEvent
            )
        }
         response.value.let {
             return handleSuccess(it)
         }
    }

    abstract fun handleSuccess(resultObj: Data): DataState<ViewState>?
}