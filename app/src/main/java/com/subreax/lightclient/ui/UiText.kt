package com.subreax.lightclient.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    class Res(@StringRes val id: Int, vararg val args: Any = emptyArray()) : UiText()
    class Hardcoded(val str: String) : UiText()

    @Composable
    fun stringValue(): String {
        return when (this) {
            is Res -> stringResource(id, *args)
            is Hardcoded -> str
        }
    }

    fun stringValue(context: Context): String {
        return when (this) {
            is Res -> context.resources.getString(id, *args)
            is Hardcoded -> str
        }
    }

    override fun toString(): String {
        return when (this) {
            is Res -> "res#$id"
            is Hardcoded -> str
        }
    }

    companion object {
        fun Empty() = Hardcoded("")
    }
}
