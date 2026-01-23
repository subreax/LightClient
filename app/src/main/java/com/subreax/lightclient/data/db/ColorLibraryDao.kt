package com.subreax.lightclient.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.subreax.lightclient.data.Color
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorLibraryDao {
    @Query("SELECT * FROM color_lib")
    fun getAll(): Flow<List<Color>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(color: Color)

    @Query("DELETE FROM color_lib WHERE argb=:argb")
    suspend fun delete(argb: Int)
}