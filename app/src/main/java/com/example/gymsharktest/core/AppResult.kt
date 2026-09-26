package com.example.gymsharktest.core

/** Result of an operation that can fail in a way the UI has to react to. */
sealed interface AppResult<out T> {

    data class Success<out T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

/**
 * The failure vocabulary the UI understands.
 *
 * Deliberately carries no throwable, message, or stack trace. Exceptions are caught at the
 * data-layer boundary and never travel upward, which keeps host names, transport details, and
 * payload fragments out of anything that could be rendered or screenshotted.
 */
sealed interface AppError {

    /** No usable connection, DNS failure, or the request never reached the server. */
    data object Network : AppError

    /** The request reached the server but did not complete in time. */
    data object Timeout : AppError

    /** A response arrived but could not be understood. */
    data object Parsing : AppError

    /** The request succeeded but yielded nothing usable. */
    data object Empty : AppError

    /** Anything else. Opaque here by design. */
    data object Unexpected : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.value

fun AppResult<*>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error
