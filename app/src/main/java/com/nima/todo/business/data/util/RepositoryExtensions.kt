package com.nima.todo.business.data.util

import com.nima.todo.business.data.cache.CacheConstants.CACHE_TIMEOUT
import com.nima.todo.business.data.cache.CacheErrors.CACHE_ERROR_TIMEOUT
import com.nima.todo.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.nima.todo.business.data.cache.CacheResult
import com.nima.todo.business.data.network.ApiResult
import com.nima.todo.business.data.network.NetworkConstants.NETWORK_TIMEOUT
import com.nima.todo.business.data.network.NetworkErrors.NETWORK_ERROR_TIMEOUT
import com.nima.todo.business.data.network.NetworkErrors.NETWORK_ERROR_UNKNOWN
import com.nima.todo.business.data.util.GenericError.ERROR_UNKNOWN
import com.nima.todo.business.domain.util.cLog
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException

/**
 * Reference: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NETWORK_TIMEOUT){
                ApiResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    ApiResult.Error(code, NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    cLog(errorResponse)
                    ApiResult.Error(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    cLog(NETWORK_ERROR_UNKNOWN)
                    ApiResult.Error(
                        null,
                        NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT){
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {

                is TimeoutCancellationException -> {
                    CacheResult.Error(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    cLog(CACHE_ERROR_UNKNOWN)
                    CacheResult.Error(CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}


private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        ERROR_UNKNOWN
    }
}