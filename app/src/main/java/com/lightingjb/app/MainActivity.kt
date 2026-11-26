package com.lightingjb.app // Убедитесь, что package совпадает с вашим

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush as GradientBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

// Данные для плагина
data class PluginItem(val id: String, val name: String, val description: String)

@Composable
fun MainAppScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPlugin by remember { mutableStateOf<PluginItem?>(null) }

    // Если выбран плагин, показываем его страницу вместо вкладок
    if (selectedPlugin != null) {
        PluginDetailScreen(plugin = selectedPlugin!!, onBack = { selectedPlugin = null })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                GradientBrush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Color(0xCCFFFFFF)) {
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
                    0 -> MainTab()
                    1 -> CustomizationTab(onPluginClick = { selectedPlugin = it })
                }
            }
        }
    }
}

@Composable
fun MainTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RootSDK",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun CustomizationTab(onPluginClick: (PluginItem) -> Unit) {
    // Список плагинов
    val plugins = listOf(
        PluginItem("hide_clock", "Скрыть часы", "Убирает стандартные часы с экрана блокировки."),
        PluginItem("clear_bg", "Прозрачный фон", "Делает фон системного интерфейса прозрачным."),
        PluginItem("custom_icons", "Свои иконки", "Позволяет заменить иконки приложений на свои.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Плагины",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        plugins.forEach { plugin ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onPluginClick(plugin) }, // Переход на страницу плагина
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = plugin.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = plugin.description, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PluginDetailScreen(plugin: PluginItem, onBack: () -> Unit) {
    var isInstalled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                GradientBrush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(plugin.name, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = plugin.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = plugin.description,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { isInstalled = !isInstalled },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInstalled) Color(0xCCF44336) else Color(0xCC4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = if (isInstalled) "Выключить" else "Установить",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
