package com.subreax.lightclient.ui.home

import com.subreax.lightclient.data.Property

data class PropertyCallback(
    val colorPropertyClicked: (Property.Color) -> Unit,
    val stringEnumClicked: (Property.Enum) -> Unit,
    val floatSliderChanged: (Property.FloatSlider, Float) -> Unit,
    val toggleChanged: (Property.Bool, Boolean) -> Unit,
    val intChanged: (Property.IntNumber, Int) -> Unit,
    val intSliderChanged: (Property.IntSlider, Int) -> Unit
)