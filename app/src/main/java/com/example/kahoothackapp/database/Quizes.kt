package com.example.kahoothackapp.database

import androidx.room.*
import java.time.LocalDateTime
import java.util.*

@Entity(tableName = "questions")
data class Quizes (
    @ColumnInfo(name = "quiz_name") val quiz_name: String?,
    @ColumnInfo(name = "questions") val answers: String?,
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP") val timestamp: String?
)

@Entity
data class QuizesInsert(
    @ColumnInfo(name = "quiz_name") val quiz_name: String?,
    @ColumnInfo(name = "questions") val answers: String?
)

@Dao
interface QuestionsDao {
    @Insert(entity = Quizes::class)
    suspend fun insert(data: QuizesInsert)

    @Query("SELECT * FROM questions ORDER BY uid ASC")
    suspend fun loadAllQuizes(): List<Quizes>

    @Query("SELECT quiz_name, questions FROM questions WHERE uid LIKE :id")
    suspend fun loadSpecificQuiz(id: Int?): Quizes
}