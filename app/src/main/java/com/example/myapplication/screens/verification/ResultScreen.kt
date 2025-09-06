package com.example.myapplication.screens.verification

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.components.tables.MeasurementTable
import com.example.myapplication.viewmodels.verification.VerificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultScreen(
    viewModel: VerificationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    // Получаем контекст для запуска Activity
    val context = LocalContext.current
    
    // Состояние для Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Получаем данные из ViewModel
    val protocolNumber by viewModel.protocolNumber.collectAsState()
    val deviceNumber by viewModel.deviceNumber.collectAsState()
    val deviceType by viewModel.deviceType.collectAsState()
    val deviceModel by viewModel.deviceModel.collectAsState()
    val lowerRange by viewModel.lowerRange.collectAsState()
    val upperRange by viewModel.upperRange.collectAsState()
    val registryNumber by viewModel.registryNumber.collectAsState()
    val accuracyClass by viewModel.accuracyClass.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val humidity by viewModel.humidity.collectAsState()
    val pressure by viewModel.pressure.collectAsState()
    val measurementGroups by viewModel.measurementGroups.collectAsState()
    val verificationStatus by viewModel.verificationStatus.collectAsState()
    val pdfGenerationStatus by viewModel.pdfGenerationStatus.collectAsState()
    val climateDataStatus by viewModel.climateDataStatus.collectAsState()
    
    // Статус генерации PDF
    val isGeneratingPdf = pdfGenerationStatus is VerificationViewModel.PdfGenerationStatus.Loading
    
    // Определяем статус поверки при загрузке экрана
    LaunchedEffect(Unit) {
        viewModel.determineVerificationStatus()
        
        // Если климатические данные не заполнены, попробуем загрузить их из базы данных
        if (temperature.isEmpty() && humidity.isEmpty() && pressure.isEmpty()) {
            viewModel.loadLatestClimateData()
        }
    }
    
    // Форматирование текущей даты
    val currentDate = SimpleDateFormat("dd MMMM yyyy г.", Locale.forLanguageTag("ru-RU")).format(Date())
    
    // Обработка статуса генерации PDF
    LaunchedEffect(pdfGenerationStatus) {
        when (pdfGenerationStatus) {
            is VerificationViewModel.PdfGenerationStatus.Success -> {
                // Получаем созданный файл
                val file = (pdfGenerationStatus as VerificationViewModel.PdfGenerationStatus.Success).file
                
                // Создаем URI через FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                
                // Создаем Intent для отправки PDF
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Запускаем Activity для выбора приложения для отправки
                context.startActivity(Intent.createChooser(intent, "Отправить протокол"))
                
                // Показываем сообщение об успешной генерации
                snackbarHostState.showSnackbar(
                    message = "PDF успешно создан",
                    duration = SnackbarDuration.Short
                )
            }
            is VerificationViewModel.PdfGenerationStatus.Error -> {
                val errorMessage = (pdfGenerationStatus as VerificationViewModel.PdfGenerationStatus.Error).message
                snackbarHostState.showSnackbar(
                    message = "Ошибка при создании PDF: $errorMessage",
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }
    
    // Используем Scaffold для отображения Snackbar
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // Основной контент экрана
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок протокола
            ProtocolHeader(
                protocolNumber = protocolNumber,
                deviceType = deviceType,
                deviceModel = deviceModel,
                deviceNumber = deviceNumber,
                rangeText = "$lowerRange - $upperRange В",
                registryNumber = registryNumber,
                accuracyClass = accuracyClass
            )
            
            // Информация о поверке
            VerificationInfo(
                temperature = temperature,
                humidity = humidity,
                pressure = pressure,
                climateDataStatus = climateDataStatus
            )
            
            // Результаты поверки
            Text(
                text = "Результаты поверки",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            // Разделы результатов поверки
            VerificationResultSection(
                title = "1. Внешний осмотр",
                result = "Соответствует п.4.1. ГОСТ 8.497-83"
            )
            
            VerificationResultSection(
                title = "2. Опробование",
                result = "Соответствует п.4.2. ГОСТ 8.497-83"
            )
            
            // Таблица с результатами измерений
            Text(
                text = "3. Определение основной приведенной погрешности",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (measurementGroups.isNotEmpty()) {
                MeasurementTable(
                    viewModel = viewModel,
                    group = measurementGroups[0]
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            VerificationResultSection(
                title = "4. Остаточное отклонение указателя",
                result = "Соответствует п. 6.6.2 ГОСТ 8711-93"
            )
            
            // Заключение
            VerificationConclusion(
                status = verificationStatus,
                date = currentDate
            )
            
            // Кнопки навигации
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Кнопка "Назад"
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    // Блокируем кнопку во время генерации PDF
                    enabled = !isGeneratingPdf
                ) {
                    Text("Назад")
                }
                
                // Кнопка генерации PDF
                Button(
                    onClick = {
                        // Генерируем PDF-протокол напрямую, без сохранения
                        viewModel.generateAndSavePdfProtocol()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    // Блокируем кнопку во время генерации PDF
                    enabled = !isGeneratingPdf
                ) {
                    if (isGeneratingPdf) {
                        // Показываем индикатор загрузки
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Сохранить PDF")
                }
                
                // Кнопка "Завершить"
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f),
                    // Блокируем кнопку во время генерации PDF
                    enabled = !isGeneratingPdf
                ) {
                    Text("Завершить")
                }
            }
            
            // Информация о расположении PDF-файлов
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Информация о PDF-файлах",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Путь к директории с PDF-файлами
                    val pdfDir = viewModel.getPdfDirectory()
                    Text(
                        text = "Файлы сохраняются в:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pdfDir.absolutePath,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Список PDF-файлов
                    val pdfFiles = viewModel.getAllPdfFiles()
                    Text(
                        text = "Количество созданных PDF: ${pdfFiles.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (pdfFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Показываем последние 3 файла
                        pdfFiles.sortedByDescending { it.lastModified() }.take(3).forEach { file ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Кнопка для открытия файла
                                Button(
                                    onClick = {
                                        // Открываем PDF-файл
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                        
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Открыть")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolHeader(
    protocolNumber: String,
    deviceType: String,
    deviceModel: String,
    deviceNumber: String,
    rangeText: String,
    registryNumber: String,
    accuracyClass: String
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Протокол №$protocolNumber",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Тип прибора:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(160.dp)
                )
                Text(text = "$deviceType $deviceModel")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Заводской номер:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(160.dp)
                )
                Text(text = deviceNumber)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Диапазон измерения:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(160.dp)
                )
                Text(text = rangeText)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Номер в госреестре:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(160.dp)
                )
                Text(text = registryNumber)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Класс точности:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(160.dp)
                )
                Text(text = accuracyClass)
            }
        }
    }
}

@Composable
private fun VerificationInfo(
    temperature: String,
    humidity: String,
    pressure: String,
    climateDataStatus: VerificationViewModel.ClimateDataStatus
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Условия поверки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (climateDataStatus) {
                is VerificationViewModel.ClimateDataStatus.Error -> {
                    Text(
                        text = climateDataStatus.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is VerificationViewModel.ClimateDataStatus.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Температура:",
                            modifier = Modifier.width(160.dp)
                        )
                        Text(text = "$temperature °C")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Относительная влажность:",
                            modifier = Modifier.width(160.dp)
                        )
                        Text(text = "$humidity %")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Атмосферное давление:",
                            modifier = Modifier.width(160.dp)
                        )
                        Text(text = "$pressure гПа")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Методика поверки: ГОСТ 8.497-83",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun VerificationResultSection(
    title: String,
    result: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = result,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun VerificationConclusion(
    status: String,
    date: String
) {
    val isPassed = status == "PASSED"
    val backgroundColor = if (isPassed) 
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    else 
        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    
    val textColor = if (isPassed) 
        MaterialTheme.colorScheme.primary
    else 
        MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = textColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = textColor
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Вывод: ${if (isPassed) "Годен" else "Не годен"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Дата поверки: $date",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Поверитель: __________________",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}