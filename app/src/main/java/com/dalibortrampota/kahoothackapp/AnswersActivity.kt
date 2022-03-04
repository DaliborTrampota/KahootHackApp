package com.dalibortrampota.kahoothackapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dalibortrampota.kahoothackapp.adapters.AnswersAdapter
import com.google.gson.Gson

class AnswersActivity : AppCompatActivity() {

    private lateinit var data: KahootQuestion
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnswersAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answers)

        data = getQuestions()

        initializeToolbar(data.title)

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = AnswersAdapter(data)
        recyclerView.adapter = adapter
    }

    private fun initializeToolbar(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun getQuestions(): KahootQuestion {
        var gson = Gson()
        return gson.fromJson(intent.getStringExtra("questions"), KahootQuestion::class.java)
    }
}