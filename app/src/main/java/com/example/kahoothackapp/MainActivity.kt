package com.example.kahoothackapp

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var imageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var idPrompt: EditText
    private var editMode = false

    private var QUIZ_ID_REGEX_ONE: Regex = Regex("details/(.+)")
    private var QUIZ_ID_REGEX_TWO: Regex = Regex("quiz(?:i|l)d=(.+)", RegexOption.IGNORE_CASE)
    private lateinit var answersIntent: Intent

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        button = findViewById(R.id.gallery)
        imageView = findViewById(R.id.image)
        statusText = findViewById(R.id.status)
        idPrompt = findViewById(R.id.editID)
        disableEditMode()

        var activity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(IMAGE_REQUEST_CODE, result)
        }

        button.setOnClickListener {
            if(editMode){
                disableEditMode()
                fetchAnswers(idPrompt.text.toString())
            }else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"

                activity.launch(intent)
                setStatusText(false)
            }
        }

        answersIntent = Intent(this, Answers::class.java)
    }



    private fun onActivityResult(requestCode: Int, result: ActivityResult) {
        if(requestCode == IMAGE_REQUEST_CODE && result.resultCode == RESULT_OK){
            var intent: Intent? = result.data
            imageView.setImageURI(intent?.data)
            var uri: Uri? = intent?.getData()
            if (uri != null) {
                findQuizID(uri)
            }

        }
    }

    private fun findQuizID(uri: Uri){
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var image: InputImage = InputImage.fromFilePath(applicationContext, uri)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                handleText((visionText))
            }
            .addOnFailureListener { e ->
                setStatusText(true, "There was an error detecting text on the image")
                Log.d("textError", e.toString())
            }
    }

    private fun handleText(text: Text){
        var match: MatchResult? = null
        for (block in text.textBlocks) {
            val blockText = block.text
            match = QUIZ_ID_REGEX_ONE.find(blockText)
            if(match == null) match = QUIZ_ID_REGEX_TWO.find(blockText)
            if(match != null) break
        }
        if(match == null)
            return setStatusText(true, "Quiz ID couldn't be extracted")

        fetchAnswers(match.groupValues[1])
    }

    private fun fetchAnswers(quizID: String){
        var request = Request.Builder()
            .url("https://kahoot.it/rest/kahoots/${quizID}")
            .build()

        var client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                var body = response.body?.string()
                var json: JsonObject? = JsonParser().parse(body)?.asJsonObject

                if (detectError(body, json)) {
                    runOnUiThread(Runnable() {
                        editQuizID(quizID)
                    })
                    return
                }
                answersIntent.putExtra("questions", body)
                startActivity(answersIntent)
            }

            override fun onFailure(call: Call, e: IOException) {
                editQuizID(quizID)
            }
        })
    }

    private fun detectError(body: String?, json: JsonObject?): Boolean {
        if(body == null || json == null) return true
        if(json.get("error")?.asString == "NOT_FOUND") return true
        if(body.contains("UUID is invalid")) return true
        return false
    }

    private fun setStatusText(enabled: Boolean, text: String? = null){
        if(!enabled) {
            statusText.setAlpha(0f)
            return
        }

        statusText.setText(text)
        statusText.setAlpha(1f)
    }

    private fun disableEditMode(){
        editMode = false
        setStatusText(false)
        idPrompt.visibility = View.GONE
        button.setText("SELECT IMAGE")

    }

    private fun editQuizID(foundID: String = ""){
        editMode = true
        setStatusText(true, "Extracted ID wasn't resolved properly. Check characters like o/0 or i/l/f")
        idPrompt.setText(foundID)
        idPrompt.setHint("Input quiz ID manually")
        idPrompt.visibility = View.VISIBLE
        button.setText("Confirm Quiz ID")

    }
}