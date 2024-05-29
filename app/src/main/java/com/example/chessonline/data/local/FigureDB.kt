package com.example.chessonline.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FigureEntity::class], version = 1)
abstract class FigureDB: RoomDatabase() {
    abstract fun getFigureDao(): FigureDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        private var INSTANCE: FigureDB? = null

        fun getInstance(context: Context): FigureDB {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let { return it }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FigureDB::class.java,
                    "form_database_v1.0"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
