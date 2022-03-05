package com.dalibortrampota.kahoothackapp.database

import androidx.room.*

@Entity(tableName = "questions", indices = [Index(value = ["quiz_name"], unique = true)])
data class Quiz (
    @ColumnInfo(name = "quiz_name") val quiz_name: String?,
    @ColumnInfo(name = "questions") val answers: String?,
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP") val timestamp: String?
)

@Entity
data class QuizInsert(
    @ColumnInfo(name = "quiz_name") val quiz_name: String?,
    @ColumnInfo(name = "questions") val answers: String?
)

@Dao
interface QuestionsDao {
    @Insert(entity = Quiz::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: QuizInsert)

    @Query("SELECT * FROM questions ORDER BY uid DESC")
    suspend fun loadAllQuizes(): List<Quiz>

    @Query("SELECT quiz_name, questions FROM questions WHERE uid LIKE :id")
    suspend fun loadSpecificQuiz(id: Int?): Quiz
}