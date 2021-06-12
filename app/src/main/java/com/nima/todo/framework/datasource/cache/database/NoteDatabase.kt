package com.nima.todo.framework.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nima.todo.framework.datasource.cache.model.NoteCacheEntity

@Database(entities = [NoteCacheEntity::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao() : NoteDao
    companion object{
        const val DATABASE_NAME = "NOTE_DB"

    }
}