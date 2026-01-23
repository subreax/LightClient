package com.subreax.lightclient.data.color_lib

import com.subreax.lightclient.data.Color
import com.subreax.lightclient.data.db.ColorLibraryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ColorRepositoryImpl(
    private val colorLibraryDao: ColorLibraryDao
) : ColorRepository {
    private val _colors = colorLibraryDao.getAll()
    override val argbColors: Flow<List<Int>>
        get() = _colors.map { colors -> colors.map { it.argb } }

    override suspend fun add(argb: Int) {
        colorLibraryDao.add(Color(argb))
    }

    override suspend fun delete(argb: Int) {
        colorLibraryDao.delete(argb)
    }
}