package com.subreax.lightclient

import androidx.annotation.StringRes
import com.subreax.lightclient.ui.UiText

enum class ReturnCode {
    Ok, Unspecified
}

sealed class LResult<out T>(val code: ReturnCode) {
    class Success<T>(val value: T) : LResult<T>(ReturnCode.Ok)
    class Failure(val message: UiText, code: ReturnCode = ReturnCode.Unspecified) : LResult<Nothing>(code) {
        constructor(message: String, code: ReturnCode = ReturnCode.Unspecified)
                : this(UiText.Hardcoded(message), code)

        constructor(@StringRes res: Int, vararg args: Any = emptyArray())
                : this(UiText.Res(res, *args), ReturnCode.Unspecified)
    }

    suspend fun <G> then(actionIfSuccess: suspend (T) -> LResult<G>): LResult<G> {
        return if (this is Success) {
            actionIfSuccess(this.value)
        } else
            this as Failure
    }

    suspend fun onSuccess(actionIfSuccess: suspend (T) -> Unit): LResult<T> {
        if (this is Success) {
            actionIfSuccess(value)
        }
        return this
    }

    suspend fun onFailure(actionOnFailure: suspend (Failure) -> Unit): LResult<T> {
        if (this is Failure) {
            actionOnFailure(this)
        }
        return this
    }
}
