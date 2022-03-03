package com.example.kahoothackapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class Answers : AppCompatActivity() {

    private lateinit var data: KahootQuestion
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answers)

        data = getQuestions()

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = MyAdapter(data)
        recyclerView.adapter = adapter

    }

    fun getQuestions(): KahootQuestion {
        var gson = Gson()
        return gson.fromJson(intent.getStringExtra("questions"), KahootQuestion::class.java)
    }
}