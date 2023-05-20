package com.subreax.lightclient.ui.home

import com.subreax.lightclient.data.Property

data class PropertyCallback(
    val colorPropertyClicked: (Property.Color) -> Unit,
    val enumClicked: (Property.Enum) -> Unit,
    val floatChanged: (Property.BaseFloat, Float) -> Unit,
    val floatClicked: (Property.BaseFloat) -> Unit,
    val toggleChanged: (Property.Bool, Boolean) -> Unit,
    val intChanged: (Property.BaseInt, Int) -> Unit,
    val intClicked: (Property.BaseInt) -> Unit,
)