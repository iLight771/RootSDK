package com.lightingjb.app // Убедитесь, что package совпадает с вашим

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings // Используем шестеренку, если кисточка не работает
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush as GradientBrush // Переименовываем градиент, чтобы избежать конфликта
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                GradientBrush.verticalGradient( // Используем переименованный Brush
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xCCFFFFFF),
                ) {
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
}

@Composable
fun MainTab(onNavigateToCustomization: () -> Unit) {
    var magiskStatus by remember { mutableStateOf<Boolean?>(null) }
    var showSmoothEffect by remember { mutableStateOf(false) }
    var isTextVisible by remember { mutableStateOf(false) }

    val textAlpha by animateFloatAsState(
        targetValue = if (isTextVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "textAlpha"
    )

    LaunchedEffect(showSmoothEffect) {
        if (showSmoothEffect) {
            isTextVisible = true
            delay(1500)
            isTextVisible = false
            showSmoothEffect = false
            onNavigateToCustomization()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RootSDK",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(0.9f)
            )
            Spacer(modifier = Modifier.height(40.dp))

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
                    containerColor = if (magiskStatus == true) Color(0xCC4CAF50) else Color(0xCC9E9E9E)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = when (magiskStatus) {
                        true -> "Magisk: Обнаружен"
                        false -> "Magisk: Не найден"
                        null -> "Проверить Magisk"
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showSmoothEffect = true },
                enabled = magiskStatus == true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCC6200EE)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Перейти к кастомизации", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (showSmoothEffect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        GradientBrush.verticalGradient(
                            colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
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
                    modifier = Modifier.alpha(textAlpha)
                )
            }
        }
    }
}

@Composable
fun CustomizationTab() {
    val options = listOf<String>()
    var activeStates by remember { mutableStateOf(List(options.size) { false }) }

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
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (options.isEmpty()) {
            Text(
                text = "Здесь пока ничего нет.\nДобавьте функции в код.",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            )
        } else {
            options.forEachIndexed { index, title ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xCCFFFFFF)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        
                        Button(
                            onClick = {
                                activeStates = activeStates.toMutableList().apply { this[index] = !this[index] }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeStates[index]) Color(0xCCF44336) else Color(0xCC4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (activeStates[index]) "Выключить" else "Установить",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
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
