package com.lightingjb.app // Убедитесь, что package совпадает с вашим

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import java.io.DataOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Главная") },
                    label = { Text("Главная") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Кастомизация") },
                    label = { Text("Кастомизация") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> MainTab(onNavigateToCustomization = { selectedTab = 1 })
                1 -> CustomizationTab()
            }
        }
    }
}

@Composable
fun MainTab(onNavigateToCustomization: () -> Unit) {
    var magiskStatus by remember { mutableStateOf<Boolean?>(null) }
    var showSmoothEffect by remember { mutableStateOf(false) }
    var isTextVisible by remember { mutableStateOf(false) }

    // Анимация плавного появления текста
    val textAlpha by animateFloatAsState(
        targetValue = if (isTextVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "textAlpha"
    )

    LaunchedEffect(showSmoothEffect) {
        if (showSmoothEffect) {
            isTextVisible = true
            delay(1500) // Ждем 1.5 секунды
            isTextVisible = false
            showSmoothEffect = false
            onNavigateToCustomization()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Основной контент
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RootSDK",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Кнопка 1: Проверка Magisk
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val hasRoot = checkMagiskRoot()
                        withContext(Dispatchers.Main) {
                            magiskStatus = hasRoot
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (magiskStatus == true) Color(0xFF4CAF50) else Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (magiskStatus) {
                        true -> "Magisk: Обнаружен"
                        false -> "Magisk: Не найден"
                        null -> "Проверить Magisk"
                    },
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка 2: Перейти к кастомизации
            Button(
                onClick = { showSmoothEffect = true },
                enabled = magiskStatus == true, // Активна только если есть рут
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Перейти к кастомизации")
            }
        }

        // Оверлей с зеленым фоном как в Dopamine
        if (showSmoothEffect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50), // Светло-зеленый
                                Color(0xFF2E7D32)  // Темно-зеленый
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Активирована кастомизация",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(textAlpha) // Плавное появление
                )
            }
        }
    }
}

@Composable
fun CustomizationTab() {
    // Пример списка режимов кастомизации
    val options = listOf("Скрыть часы", "Заменить иконку", "Прозрачный фон", "Свои шрифты")
    var checkedStates by remember { mutableStateOf(List(options.size) { false }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Режимы кастомизации",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        options.forEachIndexed { index, title ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontSize = 18.sp)
                Switch(
                    checked = checkedStates[index],
                    onCheckedChange = { isChecked ->
                        checkedStates = checkedStates.toMutableList().apply { this[index] = isChecked }
                    }
                )
            }
            Divider()
        }
    }
}

fun checkMagiskRoot(): Boolean {
    return try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val os = DataOutputStream(process.outputStream)
        os.writeBytes("exit\n")
        os.flush()
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}
