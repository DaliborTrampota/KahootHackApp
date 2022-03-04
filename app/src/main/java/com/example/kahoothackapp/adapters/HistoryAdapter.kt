package com.example.kahoothackapp.adapters

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.kahoothackapp.AnswersActivity
import com.example.kahoothackapp.R
import com.example.kahoothackapp.database.QuestionsDao
import com.example.kahoothackapp.database.Quizes
import com.github.marlonlom.utilities.timeago.TimeAgo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HistoryAdapter(private val data: List<Quizes>, private val dao: QuestionsDao) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = data.get(position)
        val title = "${currentItem.quiz_name}"
        holder.question.text = title
        val timestamp = LocalDateTime.parse(currentItem.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(
            ZoneOffset.UTC).toEpochMilli()
        holder.answer.text = TimeAgo.using(timestamp)

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val intent = Intent(holder.itemView.context, AnswersActivity::class.java)
                GlobalScope.launch {
                    val quiz = dao.loadSpecificQuiz(currentItem.uid)
                    val answers = quiz.answers
                    intent.putExtra("questions", answers)
                    holder.itemView.context.startActivity(intent)
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var question: TextView
        var answer: TextView

        init {
            question = itemView.findViewById(R.id.question)
            answer = itemView.findViewById(R.id.answer)
        }
    }
}