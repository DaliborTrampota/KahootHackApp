package com.dalibortrampota.kahoothackapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.answers_toolbar, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_share -> {
            val stringToSend = StringBuilder()
            stringToSend.append("Here are answers to \"${data.title}\" Kahoot:\n")
            var questionNumber = 1
            for (question in data.questions) {
                val questionBuilder = StringBuilder()
                questionBuilder.append("${questionNumber}. question: ${question.question} => ${question.choices.find({it.correct})?.answer?.let {
                    HtmlCompat.fromHtml(
                        it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }}\n")
                stringToSend.append(questionBuilder)
                questionNumber++
            }
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, stringToSend.toString())
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
            true
        } else ->
            super.onOptionsItemSelected(item)
    }

    fun getQuestions(): KahootQuestion {
        var gson = Gson()
        return gson.fromJson(intent.getStringExtra("questions"), KahootQuestion::class.java)
    }
}