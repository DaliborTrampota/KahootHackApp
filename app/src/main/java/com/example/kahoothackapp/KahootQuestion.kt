package com.example.kahoothackapp

class KahootQuestion (
    var questions: List<Question>
)

class Question (
    var type: String,
    var question: String,
    var choices: List<Choice>
)

class Choice (
    var answer: String,
    var correct: Boolean
)