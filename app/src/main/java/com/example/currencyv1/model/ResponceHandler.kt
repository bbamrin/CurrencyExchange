package com.example.currencyv1.model

import retrofit2.HttpException
import java.net.SocketTimeoutException

object ResponseHandler {

    private const val TIMEOUT_CODE  = 1
    private const val UNDEFINED_RATE_CODE = 2
    private const val NULL_ARG_ERROR_CODE = 3
    const val UNDEFINED_RATE_ERROR = "Rate is not loaded"
    const val ERROR = "Something went wrong"
    const val TIMEOUT_ERROR = "Timeout"
    const val UNAUTHORIZED_ERROR  = "Unauthorised"
    const val NOT_FOUND_ERROR  = "Not found"
    const val ARGS_NULL_ERROR  = "Some of arguments is null"

    fun <T : Any> handleSuccess(data: T): Resource<T> {
        return Resource.success(data)
    }

    fun <T : Any> handleException(e: Exception): Resource<T> {
        return when (e) {
            is HttpException -> Resource.error(getErrorMessage(e.code()), null)
            is SocketTimeoutException -> Resource.error(getErrorMessage(TIMEOUT_CODE), null)
            is RateIsUndefinedException -> Resource.error(getErrorMessage(UNDEFINED_RATE_CODE), null)
            else -> Resource.error(getErrorMessage(Int.MAX_VALUE), null)
        }
    }

    fun <T: Any>handleNullArgumentError() : Resource<T> {
        return Resource.error(getErrorMessage(NULL_ARG_ERROR_CODE), null)
    }

    private fun getErrorMessage(code: Int): String {
        return when (code) {
            TIMEOUT_CODE -> TIMEOUT_ERROR
            UNDEFINED_RATE_CODE -> UNDEFINED_RATE_ERROR
            NULL_ARG_ERROR_CODE -> ARGS_NULL_ERROR
            401 -> UNAUTHORIZED_ERROR
            404 -> NOT_FOUND_ERROR
            else -> ERROR
        }
    }
}