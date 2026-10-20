package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.RecentFileDao
import com.example.data.dao.ScratchpadDao
import com.example.data.model.RecentFile
import com.example.data.model.Scratchpad

@Database(
    entities = [RecentFile::class, Scratchpad::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao
    abstract fun scratchpadDao(): ScratchpadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "markflow_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
