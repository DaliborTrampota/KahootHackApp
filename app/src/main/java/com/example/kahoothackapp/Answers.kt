package com.example.kahoothackapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson

class Answers : AppCompatActivity() {

    private lateinit var data: KahootQuestion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answers)

        data = getQuestions()
        Log.d("there", data.questions[0].question.toString())
        Log.d("there", data.questions[0].choices[0].answer.toString())
        Log.d("there", data.questions[0].choices[0].correct.toString())
    }

    fun getQuestions(): KahootQuestion {
        var gson = Gson()
        return gson.fromJson(intent.getStringExtra("questions"), KahootQuestion::class.java)
    }
}