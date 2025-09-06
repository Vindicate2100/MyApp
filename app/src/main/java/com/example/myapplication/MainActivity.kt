package com.example.myapplication

// --- Android ---
import android.os.Bundle // Класс для сохранения и передачи состояния между компонентами (например, при пересоздании Activity)

// --- Jetpack Activity ---
import androidx.activity.ComponentActivity // Базовый класс для Activity с поддержкой Jetpack Compose
import androidx.activity.compose.setContent // Функция для установки UI на Compose в Activity

// --- Material 3 (Compose UI) ---
import androidx.compose.material3.MaterialTheme // Основной контейнер для темы (цвета, типографика, shapes)
import androidx.compose.material3.Typography // Определение стилей текста (заголовки, подписи, body)
import androidx.compose.material3.dynamicLightColorScheme // Динамическая цветовая схема (Material You, Android 12+)
import androidx.compose.material3.lightColorScheme // Базовая светлая цветовая схема (fallback, если dynamic недоступен)

// --- Compose runtime ---
import androidx.compose.runtime.Composable // Аннотация для функций, которые можно использовать в Compose UI
import androidx.compose.ui.platform.LocalContext // Позволяет получить текущий Android Context внутри Compose

// --- SplashScreen API ---
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Новый API для Splash (Android 12+), совместимый и с более старыми версиями

// --- WindowInsets / Edge-to-Edge ---
import androidx.core.view.WindowCompat // Современный API для работы с Window (например, edge-to-edge режим)
import androidx.core.view.WindowInsetsCompat // Универсальный класс для работы с системными отступами (status bar, gestures, cutouts)
import androidx.core.view.WindowInsetsControllerCompat // Контроллер для управления системными барами (показать/спрятать)

// --- Project imports ---
import com.example.myapplication.navigation.AppNavigation // Твой собственный граф навигации (экранное дерево)

// --- Hilt (Dependency Injection) ---
import dagger.hilt.android.AndroidEntryPoint // Аннотация, которая указывает, что в эту Activity можно внедрять зависимости через Hilt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Новый SplashScreen API (Android 12+)
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Скрываем системные бары для полноэкранного режима
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Скрываем статус-бар и навигационный бар
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Позволяем приложению обрабатывать жесты, даже когда системные бары скрыты
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MyAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    val dynamicColor = true
    val colorScheme = when {
        dynamicColor -> dynamicLightColorScheme(LocalContext.current)
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
