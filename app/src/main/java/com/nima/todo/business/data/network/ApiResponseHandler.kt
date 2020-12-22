package com.nima.todo.business.data.network

import com.nima.todo.business.data.network.NetworkErrors.NETWORK_DATA_NULL
import com.nima.todo.business.data.network.NetworkErrors.NETWORK_ERROR
import com.nima.todo.business.domain.state.*

abstract class ApiResponseHandler <ViewState , Data>(
    private val response  :ApiResult<Data?> ,
    private val stateEvent  :StateEvent?
        ){
    suspend fun getResult() : DataState<ViewState>{
        return when(response){
            is ApiResult.Success ->success(response)
            is ApiResult.Error -> handleError(response.message)
            is ApiResult.NetworkError -> handleError(NETWORK_ERROR)
        }
    }

    private fun handleError(message: String?): DataState<ViewState> {
        return DataState.error(
            response = Response(
                message = "${stateEvent?.errorInfo()}\n\n Reason : $message",
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateEvent =stateEvent
        )
    }

    private fun success(response: ApiResult.Success<Data?>): DataState<ViewState> {
          response.value?.let {
             return handleSuccess(response.value)
        }
         response.value?:
             return handleError(NETWORK_DATA_NULL)
        return DataState()
    }

    abstract fun handleSuccess(resultObj  :Data) : DataState<ViewState>
}