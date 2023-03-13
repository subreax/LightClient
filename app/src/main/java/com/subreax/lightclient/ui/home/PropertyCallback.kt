package com.subreax.lightclient.ui.home

import com.subreax.lightclient.data.Property

data class PropertyCallback(
    val colorPropertyClicked: (Property.ColorProperty) -> Unit,
    val stringEnumClicked: (Property.StringEnumProperty) -> Unit,
    val floatRangeChanged: (Property.FloatRangeProperty, Float) -> Unit,
    val toggleChanged: (Property.ToggleProperty, Boolean) -> Unit
)