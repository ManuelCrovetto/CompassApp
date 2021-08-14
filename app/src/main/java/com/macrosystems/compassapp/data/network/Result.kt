package com.macrosystems.compassapp.data.network

import java.lang.Exception

sealed class Result <out T> {
    data class OnSuccess<T> (val data: T): Result<T>()
    data class OnError<T> (val e: Exception?): Result<T>()
}