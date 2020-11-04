package com.example.currencyv1.model

import retrofit2.HttpException
import java.net.SocketTimeoutException

object ResponseHandler {

    private const val TIMEOUT_CODE  = 1
    private const val UNDEFINED_RATE = 2
    private const val NULL_ARG_ERROR = 3

    fun <T : Any> handleSuccess(data: T): Resource<T> {
        return Resource.success(data)
    }

    fun <T : Any> handleException(e: Exception): Resource<T> {
        return when (e) {
            is HttpException -> Resource.error(getErrorMessage(e.code()), null)
            is SocketTimeoutException -> Resource.error(getErrorMessage(TIMEOUT_CODE), null)
            is RateIsUndefinedException -> Resource.error(getErrorMessage(UNDEFINED_RATE), null)
            else -> Resource.error(getErrorMessage(Int.MAX_VALUE), null)
        }
    }

    fun <T: Any>handleNullArgumentError() : Resource<T> {
        return Resource.error(getErrorMessage(NULL_ARG_ERROR), null)
    }

    private fun getErrorMessage(code: Int): String {
        return when (code) {
            TIMEOUT_CODE -> "Timeout"
            UNDEFINED_RATE -> "Rate is not loaded"
            NULL_ARG_ERROR -> "Some of arguments is null"
            401 -> "Unauthorised"
            404 -> "Not found"
            else -> "Something went wrong"
        }
    }
}