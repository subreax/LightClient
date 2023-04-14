package com.subreax.lightclient.ui.home

import com.subreax.lightclient.data.Property

data class PropertyCallback(
    val colorPropertyClicked: (Property.Color) -> Unit,
    val stringEnumClicked: (Property.Enum) -> Unit,
    val floatSliderChanged: (Property.FloatSlider, Float) -> Unit,
    val floatSliderClicked: (Property.FloatSlider) -> Unit,
    val toggleChanged: (Property.Bool, Boolean) -> Unit,
    val intChanged: (Property.BaseInt, Int) -> Unit,
    val intClicked: (Property.BaseInt) -> Unit,
)