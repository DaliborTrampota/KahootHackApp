package com.example.kahoothackapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val data: KahootQuestion) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var title = "${position + 1}. ${data.questions[position].question}"
        if(title != null){
            title = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            holder.question.text = title
        }
        var answer = data.questions[position].choices.find({ it.correct })?.answer
        if(answer != null){
            answer = HtmlCompat.fromHtml(answer, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            holder.answer.text = answer
        }
    }

    override fun getItemCount(): Int {
        return data.questions.size
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