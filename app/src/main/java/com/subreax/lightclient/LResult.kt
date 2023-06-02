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
}
