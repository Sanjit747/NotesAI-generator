package com.example.notesaigenrator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.File
import java.io.FileOutputStream
import android.graphics.pdf.PdfDocument

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }

        setContent {
            NotesAIGeneratorApp()
        }
    }
}

// 🌐 Function to call your API and get notes
fun generateNotesFromTopic(
    context: android.content.Context,
    topic: String,
    onResult: (String) -> Unit
) {
    val url = "https://sk-proj-" + "9pvkUL4z8L7PFk4axwVJStMtanl-mSDV4c21CbmnnZpJGOKmjm_JGXrpZfm0fz3q9FiGBjvhCpT3BlbkFJGjECQtvwxGzadx0RkyJ5ct4uQcjRL-hVv4dlcBGCZae2Hz2gvGKNWpDR-lIzS40TgqOZ4UvOQA/generate?topic=${topic}"
// 🔁 Replace this with your real API endpoint

    val queue = Volley.newRequestQueue(context)
    val request = StringRequest(Request.Method.GET, url, { response ->
        onResult(response)
    }, { error ->
        error.printStackTrace()
        Toast.makeText(context, "Failed to fetch notes", Toast.LENGTH_SHORT).show()
    })

    queue.add(request)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAIGeneratorApp() {
    MaterialTheme {
        Scaffold(
            topBar = { NotesTopBar() }
        ) { innerPadding ->
            NotesAIGeneratorUI(Modifier.padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar() {
    TopAppBar(
        title = { Text("NotesAI Generator") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6200EE),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun NotesAIGeneratorUI(modifier: Modifier = Modifier) {
    var topic by remember { mutableStateOf(TextFieldValue("")) }
    var generatedNotes by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Text("AI Notes Generator", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Enter topic") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                if (topic.text.isNotBlank()) {
                    generateNotesFromTopic(context, topic.text) { result ->
                        generatedNotes = result
                    }
                } else {
                    Toast.makeText(context, "Please enter a topic", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Generate Notes")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                if (generatedNotes.isNotEmpty()) {
                    saveNotesAsPDF(generatedNotes, context)
                } else {
                    Toast.makeText(context, "Please generate notes first", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save as PDF")
            }

            if (generatedNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = generatedNotes,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 📄 Function to save generated notes as PDF
fun saveNotesAsPDF(content: String, context: android.content.Context) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = pdfDocument.startPage(pageInfo)

    val canvas = page.canvas
    val paint = android.graphics.Paint()
    paint.textSize = 12f

    val lines = content.split("\n")
    var y = 20f
    for (line in lines) {
        canvas.drawText(line, 10f, y, paint)
        y += 20f
    }

    pdfDocument.finishPage(page)

    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, "GeneratedNotes.pdf")

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF saved to Downloads!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_LONG).show()
    }

    pdfDocument.close()
}
