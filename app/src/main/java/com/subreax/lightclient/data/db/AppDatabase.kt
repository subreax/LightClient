package com.subreax.lightclient.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.subreax.lightclient.data.Color

@Database(entities = [Color::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val colorLibraryDao: ColorLibraryDao
}