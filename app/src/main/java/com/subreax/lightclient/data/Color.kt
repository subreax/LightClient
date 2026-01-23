package com.subreax.lightclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "color_lib")
data class Color(
    @PrimaryKey
    val argb: Int
)