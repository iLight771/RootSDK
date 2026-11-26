package com.lightingjb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.DataOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RootAppScreen()
                }
            }
        }
    }
}

@Composable
fun RootAppScreen() {
    var isRootGranted by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Root не активирован") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RootSDK",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusText, 
            style = MaterialTheme.typography.bodyLarge,
            color = if (isRootGranted) Color.Green else MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isProcessing = true
                statusText = "Запрос прав Root..."
                CoroutineScope(Dispatchers.IO).launch {
                    val root = requestRoot()
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        if (root) {
                            isRootGranted = true
                            statusText = "Root успешно получен!"
                        } else {
                            statusText = "Ошибка: Root не предоставлен. Установите Magisk."
                        }
                    }
                }
            },
            enabled = !isRootGranted && !isProcessing
        ) {
            Text(if (isProcessing) "Подождите..." else "Активировать Root")
        }

        Spacer(modifier = Modifier.height(48.dp))
        Divider()
        Spacer(modifier = Modifier.height(48.dp))

        if (isRootGranted) {
            Text(
                text = "Панель кастомизации", 
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                statusText = "Применено!"
            }) {
                Text("Применить кастомизацию")
            }
        } else {
            Text(
                text = "🔒 Кастомизация заблокирована.\nТребуется Root.", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

fun requestRoot(): Boolean {
    return try {
        val process = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(process.outputStream)
        os.writeBytes("exit\n")
        os.flush()
        val exitCode = process.waitFor()
        exitCode == 0
    } catch (e: Exception) {
        false
    }
}
