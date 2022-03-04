package com.dalibortrampota.kahoothackapp

class KahootQuestion (
    var title: String,
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