package com.subreax.lightclient.data.color_lib

import kotlinx.coroutines.flow.Flow

interface ColorRepository {
    val argbColors: Flow<List<Int>>

    suspend fun add(argb: Int)
    suspend fun delete(argb: Int)
}