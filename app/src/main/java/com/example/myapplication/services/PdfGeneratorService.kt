package com.example.myapplication.services

import android.content.Context
import android.os.Environment
import com.example.myapplication.viewmodels.verification.VerificationViewModel
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class PdfGeneratorService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    // Константы для работы с документом
    companion object {
        private const val DEFAULT_FONT_SIZE = 9f
        private const val SMALL_FONT_SIZE = 7f

        private const val ENCODING_IDENTITY = "Identity-H"

        // Путь к шрифту
        private const val FONT_OPENSANS = "fonts/OpenSans.ttf"
    }

    // Шрифты для документа
    private lateinit var normalFont: PdfFont
    private lateinit var boldFont: PdfFont

    // Инициализация шрифтов
    private fun initFonts(): Boolean {
        return try {
            // Получаем шрифт OpenSans из assets и копируем его во временный файл
            val openSansTempFile = copyFontFromAssetsToTemp()

            // Создаем шрифт из временного файла
            normalFont = PdfFontFactory.createFont(
                openSansTempFile.absolutePath,
                ENCODING_IDENTITY,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
            )

            // Используем тот же шрифт для жирного начертания
            boldFont = normalFont

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Копирует шрифт из assets во временный файл
     */
    private fun copyFontFromAssetsToTemp(): File {
        val tempFile = File(context.cacheDir, "temp_font_${System.currentTimeMillis()}.ttf")

        context.assets.open(FONT_OPENSANS).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    suspend fun generateVerificationProtocol(verificationData: VerificationData): File = withContext(Dispatchers.IO) {
        // Создаем файл для сохранения PDF
        val fileName = "Протокол_${verificationData.protocolNumber}_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }.pdf"

        // Убедимся, что директория существует
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: File(context.filesDir, "documents").apply { mkdirs() }

        val file = File(outputDir, fileName)

        try {
            // Инициализируем PdfWriter и Document с указанием кодировки для русских символов
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            // Уменьшаем отступы страницы для экономии места
            document.setMargins(20f, 20f, 20f, 20f)

            // Инициализируем шрифты
            initFonts()

            // Настраиваем свойства документа для поддержки кириллицы
            val metadataTitle = "Протокол поверки №${verificationData.protocolNumber}"
            val metadataAuthor = "АО \"Уралэлемент\""
            val metadataSubject = "Протокол периодической поверки"
            val metadataKeywords = "поверка, протокол, измерения"

            pdfDocument.documentInfo.title = metadataTitle
            pdfDocument.documentInfo.author = metadataAuthor
            pdfDocument.documentInfo.subject = metadataSubject
            pdfDocument.documentInfo.keywords = metadataKeywords
            pdfDocument.documentInfo.creator = "PdfGeneratorService"

            try {
                // Создаем содержимое документа
                addHeader(document, verificationData)
                addDeviceInfo(document, verificationData)
                addVerificationConditions(document, verificationData)
                addVerificationResults(document, verificationData)
                addMeasurementTable(document, verificationData)
                addConclusion(document)
                addSignature(document)
            } catch (e: Exception) {
                e.printStackTrace()
                // Добавляем информацию об ошибке в документ
                document.add(createParagraph("Произошла ошибка при формировании документа: ${e.message}", DEFAULT_FONT_SIZE, true, TextAlignment.LEFT))
                document.add(createParagraph("Обратитесь к разработчику.", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT))
            } finally {
                document.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        return@withContext file
    }

    // Вспомогательный метод для создания параграфа с нужным шрифтом
    private fun createParagraph(
        text: String,
        fontSize: Float,
        isBold: Boolean,
        alignment: TextAlignment
    ): Paragraph {
        val pdfFont: PdfFont = if (isBold) {
            PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
        } else {
            PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA)
        }

        val content = Text(text)
            .setFont(pdfFont)
            .setFontSize(fontSize)

        if (isBold) {
            content.simulateBold() // здесь это работает, т.к. content — именно Text
        }

        return Paragraph(content).setTextAlignment(alignment)
    }



    /**
     * Возвращает директорию, в которой сохраняются PDF-файлы
     */
    fun getDocumentsDirectory(): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: File(context.filesDir, "documents")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return dir
    }

    /**
     * Возвращает список всех PDF-файлов в директории документов
     */
    fun getAllPdfFiles(): List<File> {
        val documentDir = getDocumentsDirectory()
        if (!documentDir.exists()) {
            documentDir.mkdirs()
            return emptyList()
        }

        return documentDir.listFiles { file ->
            file.isFile && file.name.lowercase().endsWith(".pdf")
        }?.toList() ?: emptyList()
    }

    private fun addHeader(document: Document, data: VerificationData) {
        // Сделаем первые строки плотнее
        val headerParagraph = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMultipliedLeading(0.7f) // Уменьшаем межстрочный интервал еще сильнее
            .setMarginBottom(2f) // Уменьшаем отступ снизу

        headerParagraph.add("АО \"Уралэлемент\"\n")
        headerParagraph.add("456800, Челябинская обл., г. Верхний Уфалей, ул. Дмитриева д.24")
        document.add(headerParagraph)

        // Минимальный отступ вместо пустого параграфа
        document.add(Paragraph("").setMarginBottom(1f))

        // Создаем строку с "Периодическая поверка" слева и "Протокол №" справа
        val headerLine = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setMarginBottom(2f) // Минимальный отступ снизу

        // Добавляем периодическую поверку (слева)
        headerLine.add("Периодическая поверка")

        // Добавляем пробелы для заполнения середины
        for (i in 1..40) {
            headerLine.add(" ")
        }

        // Добавляем протокол (справа) с подчеркиванием
        headerLine.add("Протокол № ")
        headerLine.add(Text(data.protocolNumber).setUnderline())

        document.add(headerLine)
    }

    private fun addDeviceInfo(document: Document, data: VerificationData) {
        // Первая строка - информация о приборе с равномерным распределением
        val deviceTable = Table(floatArrayOf(10f, 12f, 8f, 14f, 5f, 10f, 23f, 18f))
            .useAllAvailableWidth()
            .setBorder(null)
            .setMarginBottom(2f) // Уменьшаем отступ снизу

        deviceTable.addCell(createUnborderedCell("поверен", TextAlignment.LEFT))
        deviceTable.addCell(createUnborderedCell(data.deviceType, TextAlignment.LEFT, true)) // Подчеркиваем
        deviceTable.addCell(createUnborderedCell("типа", TextAlignment.LEFT))
        deviceTable.addCell(createUnborderedCell(data.deviceModel, TextAlignment.LEFT, true)) // Подчеркиваем
        deviceTable.addCell(createUnborderedCell("№", TextAlignment.LEFT))
        deviceTable.addCell(createUnborderedCell(data.deviceNumber, TextAlignment.LEFT, true)) // Подчеркиваем
        deviceTable.addCell(createUnborderedCell("диапазоны измерения", TextAlignment.LEFT))
        deviceTable.addCell(createUnborderedCell("${data.lowerRange} - ${data.upperRange} В", TextAlignment.LEFT, true)) // Подчеркиваем

        document.add(deviceTable)

        // Вторая строка - "Номер в госреестре СИ        17057-08"
        val registryTable = Table(floatArrayOf(30f, 70f))
            .useAllAvailableWidth()
            .setBorder(null)
            .setMarginBottom(2f) // Уменьшаем отступ снизу

        registryTable.addCell(createUnborderedCell("Номер в госреестре СИ", TextAlignment.LEFT))
        
        // Создаем параграф для номера с фиксированной шириной
        val numberParagraph = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.LEFT)
            .add(Text(data.registryNumber).setUnderline())
            .setWidth(200f) // Фиксированная ширина для номера
            
        registryTable.addCell(Cell().add(numberParagraph).setBorder(null))

        document.add(registryTable)

        // Третья строка - "Род тока Постоянный измерительный механизм Магнитоэлектрический класс точности 1.5%"
        val classTable = Table(floatArrayOf(8f, 18f, 24f, 30f, 14f, 6f))
            .useAllAvailableWidth()
            .setBorder(null)
            .setMarginBottom(2f) // Уменьшаем отступ снизу

        classTable.addCell(createUnborderedCell("Род тока", TextAlignment.LEFT))
        classTable.addCell(createUnborderedCell("Постоянный", TextAlignment.LEFT,true))
        classTable.addCell(createUnborderedCell("измерительный механизм", TextAlignment.LEFT))
        classTable.addCell(createUnborderedCell("Магнитоэлектрический", TextAlignment.LEFT,true))
        classTable.addCell(createUnborderedCell("класс точности", TextAlignment.LEFT))
        classTable.addCell(createUnborderedCell(data.accuracyClass + "%", TextAlignment.LEFT,true))

        document.add(classTable)

        // Четвертая строка - информация о калибраторе
        val calibratorTable = Table(floatArrayOf(35f, 65f))
            .useAllAvailableWidth()
            .setBorder(null)
            .setMarginBottom(2f) // Уменьшаем отступ снизу

        calibratorTable.addCell(createUnborderedCell("Поверка проведена с применением:", TextAlignment.LEFT,true))
        calibratorTable.addCell(createUnborderedCell("Калибратор-компаратор универсальный КМ300КНТ №334/175/198", TextAlignment.CENTER,true))

        document.add(calibratorTable)
    }

    // Вспомогательный метод для создания ячейки без границ
    private fun createUnborderedCell(text: String, alignment: TextAlignment, underline: Boolean = false, fontSize: Float = DEFAULT_FONT_SIZE): Cell {
        val paragraph = Paragraph()
            .setFontSize(fontSize)
            .setTextAlignment(alignment)
        
        if (::normalFont.isInitialized) {
            paragraph.setFont(normalFont)
        }
        
        if (underline) {
            paragraph.add(Text(text).setUnderline())
        } else {
            paragraph.add(text)
        }
        
        return Cell()
            .add(paragraph)
            .setPadding(2f)
            .setBorder(null)
    }

    private fun addVerificationConditions(document: Document, data: VerificationData) {
        // Создаем таблицу с равномерным распределением колонок
        val conditionsTable = Table(floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f))
            .useAllAvailableWidth()
            .setBorder(null)
            .setMarginBottom(2f)

        // Добавляем ячейки с равномерным распределением
        conditionsTable.addCell(createUnborderedCell("Условия поверки:", TextAlignment.LEFT,true))
        conditionsTable.addCell(createUnborderedCell("температура ${data.temperature} °C,", TextAlignment.CENTER,true))
        conditionsTable.addCell(createUnborderedCell("относительная влажность ${data.humidity} %,", TextAlignment.CENTER,true))
        conditionsTable.addCell(createUnborderedCell("Атм. давление ${data.pressure} кПа", TextAlignment.CENTER,true))
        // Добавляем пустые ячейки для равномерного распределения
        conditionsTable.addCell(createUnborderedCell("", TextAlignment.CENTER))
        conditionsTable.addCell(createUnborderedCell("", TextAlignment.CENTER))
        conditionsTable.addCell(createUnborderedCell("", TextAlignment.CENTER))

        document.add(conditionsTable)

        // Методика поверки - заголовок
        document.add(createParagraph("Методика поверки:", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT))

        // Таблица для методики поверки: ГОСТ слева, описание справа
        val methodologyTable = Table(floatArrayOf(25f, 75f)) // Увеличиваем ширину первой колонки
            .useAllAvailableWidth()
            .setBorder(null)

        // Создаем параграф с неразрывным текстом
        val gostParagraph = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.LEFT)
            .add(Text("ГОСТ\u00A08.497\u2212\u00A083")) // Используем специальный символ дефиса (минус)
            
        methodologyTable.addCell(Cell().add(gostParagraph).setBorder(null))
        methodologyTable.addCell(createUnborderedCell("Государственная система обеспечения единства измерений (ГСИ).Амперметры, вольтметры, ваттметры, варметры. Методика поверки.", TextAlignment.CENTER,true))

        document.add(methodologyTable)
    }

    private fun addVerificationResults(document: Document, data: VerificationData) {
        // 1. Внешний осмотр - уменьшаем отступы
        document.add(createParagraph("1.Внешний осмотр", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT)
            .setMarginBottom(2f))
        
        // Убираю границу и добавляю подчеркивание для текста "Соответствует..."
        val verificationText = "Соответствует п.4.1. ГОСТ 8.497-83"
        val paragraph1 = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .add(Text(verificationText).setUnderline())
        paragraph1.setMarginBottom(2f) // Уменьшаем отступ снизу
        document.add(paragraph1)

        // 2. Опробование - уменьшаем отступы
        document.add(createParagraph("2. Опробование", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT)
            .setMarginBottom(2f))
        
        // Убираю границу и добавляю подчеркивание для текста "Соответствует..."
        val testingText = "Соответствует п.4.2. ГОСТ 8.497-83"
        val paragraph2 = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .add(Text(testingText).setUnderline())
        paragraph2.setMarginBottom(2f) // Уменьшаем отступ снизу
        document.add(paragraph2)
    }

    private fun addMeasurementTable(document: Document, data: VerificationData) {
        document.add(createParagraph("3. Определение основной приведенной погрешности", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT)
            .setMarginBottom(2f))

        if (data.measurements.isEmpty()) {
            document.add(createParagraph("Нет данных измерений", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT))
            return
        }

        // Определяем, используется ли функция преобразования
        val showTransformValues = data.transformFunction != VerificationViewModel.TRANSFORM_NONE
        
        // Создаем таблицу с колонками:
        // - Точка (В)
        // - Задано (мВ/А) (если есть преобразование)
        // - Эталон при подъеме (мВ/А)
        // - Эталон при спуске (мВ/А)
        // - Преобразованное значение при подъеме (В) (если есть преобразование)
        // - Преобразованное значение при спуске (В) (если есть преобразование)
        // - Погрешность при подъеме (%)
        // - Погрешность при спуске (%)
        // - Вариация
        
        // Создаем таблицу с учетом всех колонок (даже если они будут пустыми)
        val table = Table(floatArrayOf(0.9f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f))
            .useAllAvailableWidth()
            .setMarginTop(2f) // Уменьшаем отступ сверху
            .setMarginBottom(2f) // Уменьшаем отступ снизу

        // Сложный заголовок таблицы с вложенностью
        val headerCell1 = Cell(2, 1)
            .add(createParagraph("Точка\nВ", SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
            .setPadding(1f) // Уменьшаем отступы в ячейках

        val headerCell2 = Cell(2, 1)
            .add(createParagraph("Задано\n" + getTransformUnitText(data.transformFunction), SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
            .setPadding(1f) // Уменьшаем отступы в ячейках
            
        val headerCell3 = Cell(1, 2)
            .add(createParagraph("Эталон\n" + getTransformUnitText(data.transformFunction), SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
            .setPadding(1f) // Уменьшаем отступы в ячейках
            
        val headerCell4 = Cell(1, 2)
            .add(createParagraph("Преобразовано\nВ", SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
            .setPadding(1f) // Уменьшаем отступы в ячейках

        val headerCell5 = Cell(1, 2)
            .add(createParagraph("Погрешность\n%", SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
            .setPadding(1f) // Уменьшаем отступы в ячейках

        val headerCell6 = Cell(2, 1)
            .add(createParagraph("Вариация", SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
            .setPadding(1f) // Уменьшаем отступы в ячейках

        table.addCell(headerCell1)
        table.addCell(headerCell2)
        table.addCell(headerCell3)
        table.addCell(headerCell4)
        table.addCell(headerCell5)
        table.addCell(headerCell6)

        // Подзаголовки
        val subHeaderCells = listOf(
            "при подъеме", "при спуске",
            "при подъеме", "при спуске",
            "при подъеме", "при спуске"
        )

        for (subHeader in subHeaderCells) {
            table.addCell(
                Cell()
                    .add(createParagraph(subHeader, SMALL_FONT_SIZE, false, TextAlignment.CENTER))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
                    .setPadding(1f) // Уменьшаем отступы в ячейках
            )
        }

        // Максимальная погрешность для определения превышения допуска
        val maxAllowedError = 1.5f // это значение нужно получать из данных
        
        // Данные измерений
        for (measurement in data.measurements) {
            val formattedScaleMark = String.format(Locale.US, "%.1f", measurement.scaleMark)
            
            // Трансформированное входное значение (зависит от функции преобразования)
            val transformedInput = if (showTransformValues) {
                getFormattedTransformedScaleMark(measurement.scaleMark, data.transformFunction, 
                    data.lowerRange.toFloatOrNull() ?: 0f, 
                    data.upperRange.toFloatOrNull() ?: 100f)
            } else ""
            
            val formattedIncRef = String.format(Locale.US, "%.2f", measurement.referenceIncreasing)
            val formattedDecRef = String.format(Locale.US, "%.2f", measurement.referenceDecreasing)
            
            // Преобразованные значения (могут быть пустыми, если нет функции преобразования)
            val transformedIncValue = if (showTransformValues) 
                String.format(Locale.US, "%.2f", measurement.referenceIncreasing) else ""
            val transformedDecValue = if (showTransformValues) 
                String.format(Locale.US, "%.2f", measurement.referenceDecreasing) else ""
            
            val formattedIncErr = String.format(Locale.US, "%.2f", measurement.errorIncreasing)
            val formattedDecErr = String.format(Locale.US, "%.2f", measurement.errorDecreasing)
            val formattedVar = String.format(Locale.US, "%.2f", measurement.variation)
            
            // Проверяем превышение допуска
            val incErrorExceeded = abs(measurement.errorIncreasing) > maxAllowedError
            val decErrorExceeded = abs(measurement.errorDecreasing) > maxAllowedError

            // Добавляем ячейки в таблицу с учетом возможного превышения допуска
            table.addCell(createTableCell(formattedScaleMark))
            table.addCell(createTableCell(transformedInput))
            table.addCell(createTableCell(formattedIncRef))
            table.addCell(createTableCell(formattedDecRef))
            table.addCell(createTableCell(transformedIncValue))
            table.addCell(createTableCell(transformedDecValue))
            
            // Ячейки с погрешностями могут быть выделены при превышении допуска
            if (incErrorExceeded) {
                table.addCell(createTableCellWithHighlight("$formattedIncErr%"))
            } else {
                table.addCell(createTableCell("$formattedIncErr%"))
            }
            
            if (decErrorExceeded) {
                table.addCell(createTableCellWithHighlight("$formattedDecErr%"))
            } else {
                table.addCell(createTableCell("$formattedDecErr%"))
            }
            
            table.addCell(createTableCell(formattedVar))
        }

        // Максимальное значение погрешности и вариации
        val maxErrorInc = data.measurements.maxByOrNull { abs(it.errorIncreasing) }?.errorIncreasing ?: 0f
        val maxErrorDec = data.measurements.maxByOrNull { abs(it.errorDecreasing) }?.errorDecreasing ?: 0f
        val maxVariation = data.measurements.maxByOrNull { it.variation }?.variation ?: 0f
        
        // Итоговая строка с максимальными значениями
        table.addCell(createTableCellBold("Макс."))
        table.addCell(createTableCell("—"))
        table.addCell(createTableCell("—"))
        table.addCell(createTableCell("—"))
        table.addCell(createTableCell("—"))
        table.addCell(createTableCell("—"))
        
        // Максимальные погрешности с возможным выделением
        if (abs(maxErrorInc) > maxAllowedError) {
            table.addCell(createTableCellWithHighlight(String.format(Locale.US, "%.2f%%", maxErrorInc), true))
        } else {
            table.addCell(createTableCellBold(String.format(Locale.US, "%.2f%%", maxErrorInc)))
        }
        
        if (abs(maxErrorDec) > maxAllowedError) {
            table.addCell(createTableCellWithHighlight(String.format(Locale.US, "%.2f%%", maxErrorDec), true))
        } else {
            table.addCell(createTableCellBold(String.format(Locale.US, "%.2f%%", maxErrorDec)))
        }
        
        table.addCell(createTableCellBold(String.format(Locale.US, "%.2f", maxVariation)))

        document.add(table)
        
        // Результирующая строка
        val hasErrors = abs(maxErrorInc) > maxAllowedError || abs(maxErrorDec) > maxAllowedError
        val resultText = if (hasErrors) 
            "Результат: НЕ СООТВЕТСТВУЕТ (допуск ±${maxAllowedError}%)" 
        else 
            "Результат: СООТВЕТСТВУЕТ (допуск ±${maxAllowedError}%)"
            
        val resultTable = Table(1).useAllAvailableWidth()
        resultTable.addCell(
            Cell()
                .add(createParagraph(resultText, DEFAULT_FONT_SIZE, true, TextAlignment.CENTER))
                .setBorder(SolidBorder(ColorConstants.WHITE, 0f))
        )
        
        document.add(resultTable)
        
        // 4. Остаточное отклонение указателя - уменьшаем отступы
        document.add(createParagraph("4. Остаточное отклонение указателя", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT)
            .setMarginBottom(2f))
        
        // Убираю границу и добавляю подчеркивание для текста "Соответствует..."
        val residualText = "Соответствует п. 6.6.2 ГОСТ 8711-93"
        val paragraph4 = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .add(Text(residualText).setUnderline())
        paragraph4.setMarginBottom(2f) // Уменьшаем отступ снизу
        document.add(paragraph4)
    }

    // Вспомогательные методы для таблицы измерений
    private fun createTableCellBold(text: String): Cell {
        return Cell()
            .add(createParagraph(text, SMALL_FONT_SIZE, true, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setPadding(1f) // Уменьшаем отступы в ячейках
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
    }
    
    private fun createTableCellWithHighlight(text: String, isBold: Boolean = false): Cell {
        return Cell()
            .add(createParagraph(text, SMALL_FONT_SIZE, isBold, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setPadding(1f) // Уменьшаем отступы в ячейках
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
    }
    
    private fun getTransformUnitText(transformFunction: String): String {
        return when (transformFunction) {
            VerificationViewModel.TRANSFORM_75MV -> "мВ"
            VerificationViewModel.TRANSFORM_5A -> "А"
            else -> "—"
        }
    }
    
    // Возвращает форматированное трансформированное значение
    private fun getFormattedTransformedScaleMark(
        scaleMark: Float,
        transformFunction: String,
        lowerRange: Float,
        upperRange: Float
    ): String {
        val range = upperRange - lowerRange
        return when (transformFunction) {
            VerificationViewModel.TRANSFORM_75MV -> {
                // Преобразование В в мВ (0-100В -> 0-75мВ)
                val transformedValue = scaleMark * 75f / range
                String.format(Locale.US, "%.2f", transformedValue)
            }
            VerificationViewModel.TRANSFORM_5A -> {
                // Преобразование В в А (0-100В -> 0-5А)
                val transformedValue = scaleMark * 5f / range
                String.format(Locale.US, "%.2f", transformedValue)
            }
            else -> ""
        }
    }

    private fun createTableCell(text: String): Cell {
        return Cell()
            .add(createParagraph(text, SMALL_FONT_SIZE, false, TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setPadding(1f) // Уменьшаем отступы в ячейках
            .setBorder(SolidBorder(ColorConstants.BLACK, 0.5f))
    }

    private fun addConclusion(document: Document) {
        // Добавляем вывод
        document.add(createParagraph("Вывод: Годен", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT))
        
        // Добавляем дату поверки под выводом
        document.add(createParagraph("Дата поверки: 16 февраля 2024 г.", DEFAULT_FONT_SIZE, false, TextAlignment.LEFT))
    }

    private fun addSignature(document: Document) {
        document.add(createParagraph("\n", DEFAULT_FONT_SIZE, false, TextAlignment.JUSTIFIED_ALL))

        // Таблица для подписи с четырьмя колонками
        val signatureTable = Table(floatArrayOf(10f, 30f, 30f, 30f))
            .useAllAvailableWidth()
            .setBorder(null)

        // Первая колонка - "Поверитель"
        signatureTable.addCell(createUnborderedCell("Поверитель", TextAlignment.LEFT))

        // Вторая колонка - должность с подчеркиванием
        val positionCell = Cell().setBorder(null)
        val positionParagraph = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .add("Слесарь КИП и А\n")
            .add(Text("_________________\n").setUnderline())
            .add(Text("должность").setFontSize(SMALL_FONT_SIZE))
        positionCell.add(positionParagraph)
        signatureTable.addCell(positionCell)

        // Третья колонка - место для подписи
        val signatureCell = Cell().setBorder(null)
        val signatureParagraph = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .add("\n") // Пустая строка для места подписи
            .add(Text("_________________\n").setUnderline())
            .add(Text("подпись").setFontSize(SMALL_FONT_SIZE))
        signatureCell.add(signatureParagraph)
        signatureTable.addCell(signatureCell)

        // Четвертая колонка - расшифровка
        val nameCell = Cell().setBorder(null)
        val nameParagraph = Paragraph()
            .setFont(normalFont)
            .setFontSize(DEFAULT_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .add("Суслов А.В.\n")
            .add(Text("_________________\n").setUnderline())
            .add(Text("расшифровка").setFontSize(SMALL_FONT_SIZE))
        nameCell.add(nameParagraph)
        signatureTable.addCell(nameCell)

        document.add(signatureTable)
    }

    data class VerificationData(
        val protocolNumber: String,
        val deviceNumber: String,
        val deviceType: String,
        val deviceModel: String,
        val lowerRange: String,
        val upperRange: String,
        val registryNumber: String,
        val accuracyClass: String,
        val temperature: String,
        val humidity: String,
        val pressure: String,
        val transformFunction: String,
        val status: String,
        val measurements: List<VerificationViewModel.VoltageMeasurement>
    )
}