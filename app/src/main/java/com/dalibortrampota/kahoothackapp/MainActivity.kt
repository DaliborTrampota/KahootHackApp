package com.dalibortrampota.kahoothackapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import com.dalibortrampota.kahoothackapp.database.QuestionsDatabase
import com.dalibortrampota.kahoothackapp.database.QuizInsert
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    private lateinit var button: Button
    private lateinit var photoButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var idPrompt: EditText
    private lateinit var modeSwitchButton: MenuItem
    private lateinit var tmpUri: Uri
    private lateinit var answersIntent: Intent
    private var editMode = false

    private var activityImageRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onActivityResult(IMAGE_REQUEST_CODE, result, null)
    }

    private var takePictureRequest = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            onActivityResult(IMAGE_REQUEST_CODE, null, tmpUri)
        }
    }


    private var QUIZ_ID_REGEX_ONE: Regex = Regex("details/(.+)")
    private var QUIZ_ID_REGEX_TWO: Regex = Regex("quiz(?:i|l)d=(.+)", RegexOption.IGNORE_CASE)
    private var QUIZ_ID_REGEX_THREE: Regex = Regex("([\\d\\w]+-[\\d\\w]+-[\\d\\w]+-[\\d\\w]+-[\\d\\w]+)")




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        button = findViewById(R.id.gallery)
        photoButton = findViewById(R.id.takePhotoButton)
        imageView = findViewById(R.id.image)
        statusText = findViewById(R.id.status)
        idPrompt = findViewById(R.id.editID)
        disableEditMode()

        button.setOnClickListener(imageListener)
        photoButton.setOnClickListener(imageListener)

        answersIntent = Intent(this, AnswersActivity::class.java)
    }

    private val imageListener = {view: View ->
        if(editMode){
            val inputText: String = idPrompt.text.toString()
            var quizID: String? = null

            if(!inputText.isEmpty() && inputText.length == 36)
                quizID = detectQuizID(inputText)

            if(quizID == null) {
                setStatusText(true, "Invalid quiz ID provided")
            }else{
                fetchAnswers(quizID)
                disableEditMode()
            }
        }else{
            if(view.id == R.id.takePhotoButton) {
                tmpUri = getTmpFileUri()
                takePictureRequest.launch(tmpUri)
                setStatusText(false)
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"

                activityImageRequest.launch(intent)
                setStatusText(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.default_toolbar, menu)
        if(menu != null) modeSwitchButton = menu.findItem(R.id.action_paste)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_history -> {
            val historyIntent = Intent(this, HistoryActivity::class.java)
            startActivity(historyIntent)
            true
        }
        R.id.action_paste -> {
            if(editMode) disableEditMode()
            else enableEditMode("", false)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun onActivityResult(requestCode: Int, result: ActivityResult?, tempUri: Uri?) {
        if(tempUri != null) {
            imageView.setImageURI(tempUri)
            return findQuizID(tempUri)
        }

        if(requestCode == IMAGE_REQUEST_CODE && result?.resultCode == RESULT_OK){
            var intent: Intent? = result.data
            imageView.setImageURI(intent?.data)
            var uri: Uri? = intent?.getData()
            if (uri != null) findQuizID(uri)
        }
    }

    private fun findQuizID(uri: Uri){
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var image: InputImage = InputImage.fromFilePath(applicationContext, uri)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                handleText(visionText)
            }
            .addOnFailureListener { e ->
                setStatusText(true, "There was an error detecting text on the image")
                Log.d("textError", e.toString())
            }
    }

    private fun handleText(text: Text){
        var quizID: String? = null
        for (block in text.textBlocks) {
            val blockText = block.text
            quizID = detectQuizID(blockText)
            if(quizID != null) break
        }
        if(quizID == null)
            return setStatusText(true, "Quiz ID couldn't be extracted")

        fetchAnswers(quizID)
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

                if(isPrivate(json)){
                    return runOnUiThread(Runnable() {
                        setStatusText(true, "This Kahoot is private :(")
                    })
                }

                if (detectError(body, json)) {
                    return runOnUiThread(Runnable() {
                        enableEditMode(quizID)
                    })
                }

                val db = QuestionsDatabase.getDatabase(applicationContext)
                val dao = db.questionsDao()
                val db_data = QuizInsert(Gson().fromJson(body, KahootQuestion::class.java).title, body)

                GlobalScope.launch {
                    dao.insert(db_data)
                }

                answersIntent.putExtra("questions", body)
                startActivity(answersIntent)
            }

            override fun onFailure(call: Call, e: IOException) {
                enableEditMode(quizID)
            }
        })
    }

    private fun isPrivate(json: JsonObject?): Boolean {
        return json?.get("error")?.asString == "FORBIDDEN"
    }

    private fun detectError(body: String?, json: JsonObject?): Boolean {
        if(body == null || json == null) return true
        if(json.get("error")?.asString == "NOT_FOUND") return true
        if(body.contains("UUID is invalid")) return true
        return false
    }

    private fun setStatusText(enabled: Boolean, text: String? = null){
        if(!enabled) return statusText.setAlpha(0f)

        statusText.setText(text)
        statusText.setAlpha(1f)
    }

    private fun disableEditMode(){
        editMode = false
        setStatusText(false)
        idPrompt.visibility = View.GONE
        photoButton.visibility = View.VISIBLE
        button.setText("SELECT IMAGE")

        if(this::modeSwitchButton.isInitialized) {
            DrawableCompat.setTint(modeSwitchButton.icon, Color.WHITE)
        }
    }

    private fun enableEditMode(foundID: String = "", showStatusMessage: Boolean = true){
        editMode = true
        if(showStatusMessage)
            setStatusText(true, "Extracted ID wasn't resolved properly. Check characters like o/0 or i/l/f")

        if(resources.getString(R.string.mode) == "Day") idPrompt.setBackgroundColor(ContextCompat.getColor(this, R.color.primaryLight))
        else idPrompt.setBackgroundColor(ContextCompat.getColor(this, R.color.primaryDark))

        idPrompt.setText(foundID)
        idPrompt.setHint("Paste quiz ID here")
        idPrompt.visibility = View.VISIBLE
        button.setText("Confirm Quiz ID")
        photoButton.visibility = View.GONE

        if(this::modeSwitchButton.isInitialized) {
            if(resources.getString(R.string.mode) == "Day") DrawableCompat.setTint(modeSwitchButton.icon, ContextCompat.getColor(this, R.color.lightGreen))
            else DrawableCompat.setTint(modeSwitchButton.icon, ContextCompat.getColor(this, R.color.lightGreen))
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun detectQuizID(str: String): String? {
        var match = QUIZ_ID_REGEX_ONE.find(str)
        if(match == null) match = QUIZ_ID_REGEX_TWO.find(str)
        if(match == null) match = QUIZ_ID_REGEX_THREE.find(str)
        if(match == null) return null

        return match.groupValues[1].lowercase()
    }
}