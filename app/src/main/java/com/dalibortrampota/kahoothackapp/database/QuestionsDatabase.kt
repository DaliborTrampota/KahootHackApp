package com.dalibortrampota.kahoothackapp.database

import android.content.Context
import androidx.room.*

@Database(entities = [Quiz::class], version = 2, exportSchema = false)
abstract class QuestionsDatabase : RoomDatabase(){
    abstract fun questionsDao(): QuestionsDao

    companion object{
        @Volatile
        private var INSTANCE: QuestionsDatabase? = null

        fun getDatabase(context: Context): QuestionsDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuestionsDatabase::class.java,
                    "questions_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}