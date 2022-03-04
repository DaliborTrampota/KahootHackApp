package com.example.kahoothackapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kahoothackapp.adapters.HistoryAdapter
import com.example.kahoothackapp.database.QuestionsDao
import com.example.kahoothackapp.database.QuestionsDatabase
import com.example.kahoothackapp.database.Quizes
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        initializeToolbar()

        val db = QuestionsDatabase.getDatabase(applicationContext)
        val dao = db.questionsDao()

        val recyclerView = findViewById<RecyclerView>(R.id.historyRecycler)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        GlobalScope.launch {
            val adapter = HistoryAdapter(getHistory(dao), dao)
            recyclerView.adapter = adapter
        }
    }

    private fun initializeToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "History"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private suspend fun getHistory(dao: QuestionsDao): List<Quizes> {
        return dao.loadAllQuizes()
    }
}