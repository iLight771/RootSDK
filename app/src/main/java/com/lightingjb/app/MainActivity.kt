package com.lightingjb.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.*
import rikka.shizuku.Shizuku
import java.io.DataOutputStream
import java.text.SimpleDateFormat
import java.util.*

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

data class PluginItem(val id: String, val name: String, val description: String)
val installedPlugins = mutableStateListOf<String>()

@Composable
fun MainAppScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPlugin by remember { mutableStateOf<PluginItem?>(null) }

    if (selectedPlugin != null) {
        if (selectedPlugin?.id == "icon_painter") {
            IconPainterScreen(onBack = { selectedPlugin = null })
        } else {
            PluginDetailScreen(plugin = selectedPlugin!!, onBack = { selectedPlugin = null })
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(GradientBrush.verticalGradient(colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))))) {
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
    var hasMagisk by remember { mutableStateOf<Boolean?>(null) }
    var shizukuRunning by remember { mutableStateOf(false) }
    var shizukuGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasMagisk = checkMagiskRoot()
        shizukuRunning = checkShizukuRunning()
        if (shizukuRunning) shizukuGranted = checkShizukuPermission()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (installedPlugins.contains("clock26")) {
            Clock26Widget()
            Spacer(modifier = Modifier.height(32.dp))
        }
        Text(text = "RootSDK", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { CoroutineScope(Dispatchers.IO).launch { hasMagisk = checkMagiskRoot() } },
            colors = ButtonDefaults.buttonColors(containerColor = if (hasMagisk == true) Color(0xCC4CAF50) else Color(0xCC9E9E9E)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(text = if (hasMagisk == true) "Magisk: Обнаружен" else "Проверить Magisk", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (hasMagisk != true) {
            Button(
                onClick = {
                    if (shizukuRunning && !shizukuGranted) {
                        requestShizukuPermission()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            shizukuRunning = checkShizukuRunning()
                            if (shizukuRunning) shizukuGranted = checkShizukuPermission()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !shizukuRunning -> Color(0xCC9E9E9E)
                        shizukuGranted -> Color(0xCC4CAF50)
                        else -> Color(0xCCFF9800)
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = when {
                        !shizukuRunning -> "Shizuku: Не запущен"
                        shizukuGranted -> "Shizuku: Готов"
                        else -> "Shizuku: Дать разрешение"
                    }, color = Color.White
                )
            }
        }
    }
}

@Composable
fun Clock26Widget() {
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    Text(text = currentTime, fontSize = 80.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20), letterSpacing = 4.sp)
}

@Composable
fun CustomizationTab(onPluginClick: (PluginItem) -> Unit) {
    val plugins = listOf(
        PluginItem("icon_painter", "Icon Painter", "Перекрашивает иконки системно через Shizuku."),
        PluginItem("clock26", "Clock26", "Скрывает часы в статус-баре.")
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(text = "Плагины", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.padding(bottom = 16.dp))
        plugins.forEach { plugin ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onPluginClick(plugin) },
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
fun IconPainterScreen(onBack: () -> Unit) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan, Color.White)
    var selectedColor by remember { mutableStateOf(Color.Blue) }
    var status by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(GradientBrush.verticalGradient(colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))))) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color(0xFF1B5E20)) }
                Text("Icon Painter", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Выберите цвет:", fontSize = 18.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                colors.forEach { color ->
                    Box(modifier = Modifier.size(40.dp).background(color, CircleShape).clickable { selectedColor = color }.then(if (selectedColor == color) Modifier.padding(4.dp) else Modifier))
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val hexColor = String.format("#%06X", 0xFFFFFF and selectedColor.value.toInt())
                        val success = applyIconTintViaShizuku(hexColor)
                        withContext(Dispatchers.Main) { status = if (success) "Цвет применен!" else "Ошибка (Shizuku не запущен)" }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC4CAF50)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Применить ко всем иконкам", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val success = resetIconTintViaShizuku()
                        withContext(Dispatchers.Main) { status = if (success) "Сброшено!" else "Ошибка сброса" }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCF44336)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Сбросить (вернуть как было)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = status, color = Color.DarkGray, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun PluginDetailScreen(plugin: PluginItem, onBack: () -> Unit) {
    val isInstalled = installedPlugins.contains(plugin.id)
    var statusMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(GradientBrush.verticalGradient(colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))))) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color(0xFF1B5E20)) }
                Text(text = plugin.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = plugin.description, fontSize = 16.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isInstalled) {
                        // ВОЗВРАТ ЧАСОВ
                        if (plugin.id == "clock26") {
                            CoroutineScope(Dispatchers.IO).launch {
                                // 1. Очищаем черный список
                                showStatusBarClockViaShizuku()
                                // 2. Перезапускаем SystemUI, чтобы изменения применились
                                restartSystemUI()
                                withContext(Dispatchers.Main) {
                                    installedPlugins.remove(plugin.id)
                                    statusMessage = "Часы возвращены! (если не появились, перезагрузите телефон)"
                                }
                            }
                        } else {
                            installedPlugins.remove(plugin.id)
                            statusMessage = "Плагин выключен"
                        }
                    } else {
                        // СКРЫТИЕ ЧАСОВ
                        if (plugin.id == "clock26") {
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = hideStatusBarClockViaShizuku()
                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        installedPlugins.add(plugin.id)
                                        statusMessage = "Часы скрыты!"
                                    } else {
                                        statusMessage = "Ошибка: Shizuku не запущен"
                                    }
                                }
                            }
                        } else {
                            installedPlugins.add(plugin.id)
                            statusMessage = "Плагин активирован"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isInstalled) Color(0xCCF44336) else Color(0xCC4CAF50)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(text = if (isInstalled) "Выключить" else "Установить", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = statusMessage, color = Color.DarkGray, textAlign = TextAlign.Center)
            }
        }
    }
}

// --- Функции Shizuku ---
fun checkShizukuRunning(): Boolean = try { Shizuku.pingBinder() } catch (e: Exception) { false }
fun checkShizukuPermission(): Boolean = try { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }
fun requestShizukuPermission() { try { Shizuku.requestPermission(0) } catch (e: Exception) { } }

fun applyIconTintViaShizuku(hexColor: String): Boolean {
    return try {
        if (!checkShizukuRunning() || !checkShizukuPermission()) return false
        val method = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        method.isAccessible = true
        val process = method.invoke(null, arrayOf("sh", "-c", "settings put secure icon_color_filter \"$hexColor\""), null, null) as Process
        process.waitFor() == 0
    } catch (e: Exception) { e.printStackTrace(); false }
}

fun resetIconTintViaShizuku(): Boolean {
    return try {
        if (!checkShizukuRunning() || !checkShizukuPermission()) return false
        val method = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        method.isAccessible = true
        val process = method.invoke(null, arrayOf("sh", "-c", "settings delete secure icon_color_filter"), null, null) as Process
        process.waitFor() == 0
    } catch (e: Exception) { e.printStackTrace(); false }
}

fun hideStatusBarClockViaShizuku(): Boolean {
    return try {
        if (!checkShizukuRunning() || !checkShizukuPermission()) return false
        val method = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        method.isAccessible = true
        val process = method.invoke(null, arrayOf("sh", "-c", "settings put secure icon_blacklist clock"), null, null) as Process
        process.waitFor() == 0
    } catch (e: Exception) { e.printStackTrace(); false }
}

fun showStatusBarClockViaShizuku(): Boolean {
    return try {
        if (!checkShizukuRunning() || !checkShizukuPermission()) return false
        val method = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        method.isAccessible = true
        // Очищаем черный список, возвращая все иконки
        val process = method.invoke(null, arrayOf("sh", "-c", "settings put secure icon_blacklist \"\""), null, null) as Process
        process.waitFor() == 0
    } catch (e: Exception) { e.printStackTrace(); false }
}

fun restartSystemUI(): Boolean {
    return try {
        if (!checkShizukuRunning() || !checkShizukuPermission()) return false
        val method = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        method.isAccessible = true
        // Убиваем процесс SystemUI, чтобы он перезапустился и вернул часы
        val process = method.invoke(null, arrayOf("sh", "-c", "pkill -f com.android.systemui"), null, null) as Process
        process.waitFor() == 0
    } catch (e: Exception) { e.printStackTrace(); false }
}

fun checkMagiskRoot(): Boolean {
    return try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val os = DataOutputStream(process.outputStream)
        os.writeBytes("exit\n")
        os.flush()
        process.waitFor() == 0
    } catch (e: Exception) { false }
}
