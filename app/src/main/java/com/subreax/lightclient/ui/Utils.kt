package com.subreax.lightclient.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

val edgePaddingValue = 16.dp
fun Modifier.edgePadding() = padding(horizontal = edgePaddingValue)

val LocalContentColorMediumAlpha: Color
    @Composable get() = LocalContentColor.current.copy(ContentAlpha.medium)

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun Dp.toPx() = with(LocalDensity.current) { toPx() }

@Composable
fun Float.toDp() = with(LocalDensity.current) { toDp() }
