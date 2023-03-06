package com.subreax.lightclient.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val edgePaddingValue = 16.dp
fun Modifier.edgePadding() = padding(horizontal = edgePaddingValue)

val LocalContentColorMediumAlpha: Color
    @Composable get() = LocalContentColor.current.copy(ContentAlpha.medium)
