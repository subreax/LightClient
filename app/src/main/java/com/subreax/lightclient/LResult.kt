package com.subreax.lightclient

import com.subreax.lightclient.ui.UiText

enum class ReturnCode {
    Ok, Unspecified
}

sealed class LResult<out T>(val code: ReturnCode) {
    class Success<T>(val value: T) : LResult<T>(ReturnCode.Ok)
    class Failure(val message: UiText, code: ReturnCode = ReturnCode.Unspecified) : LResult<Nothing>(code)
}
