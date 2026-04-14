import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// Use the long timeout client to prevent the errors from your screenshot
val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

fun main() = application {
    val windowState = rememberWindowState(width = 400.dp, height = 600.dp)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        alwaysOnTop = true,
        transparent = true,
        undecorated = true
    ) {
        val scope = rememberCoroutineScope()
        var userInput by remember { mutableStateOf("") }
        var aiResponse by remember { mutableStateOf("Type and press Enter, or use the clipboard.") }
        var isLoading by remember { mutableStateOf(false) }

        // Logic to send question
        val sendQuestion = {
            if (userInput.isNotBlank() && !isLoading) {
                isLoading = true
                scope.launch {
                    aiResponse = askOllama(userInput)
                    isLoading = false
                }
            }
        }

        WindowDraggableArea {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Header with X Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("POCKET LLM ASSISTANT", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        IconButton(
                            onClick = { exitApplication() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Input Field with Enter Key detection
                    TextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                    sendQuestion()
                                    true
                                } else {
                                    false
                                }
                            },
                        placeholder = { Text("Ask a question...", color = Color.Gray) },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.White,
                            backgroundColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = Color.Cyan
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = sendQuestion, enabled = !isLoading) {
                            Text(if (isLoading) "Thinking..." else "Ask")
                        }

                        Button(onClick = { userInput = getClipboardText() }) {
                            Text("Paste Clipboard")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = aiResponse,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

fun getClipboardText(): String {
    return try {
        Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
    } catch (e: Exception) { "" }
}

suspend fun askOllama(content: String): String = withContext(Dispatchers.IO) {
    val json = JSONObject().apply {
        put("model", "llama3")
        put("prompt", "Explain this: $content")
        put("stream", false)
    }

    val request = Request.Builder()
        .url("http://localhost:11434/api/generate")
        .post(json.toString().toRequestBody("application/json".toMediaType()))
        .build()

    try {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (responseBody != null) {
                JSONObject(responseBody).getString("response")
            } else "Error: Empty response"
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}