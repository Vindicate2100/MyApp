package com.example.myapplication.data

// Models.kt
data class MeasurementGroup(
    val name: String,
    val measurements: List<VoltageMeasurement>,
    val maxAllowedError: Float = 1.0f,
    val completed: Boolean = false
)

data class VoltageMeasurement(
    val id: Int,
    val scaleMark: Float, // Например: 10.0f, 20.0f и т.д.
    val referenceIncreasing: Float = 0f,
    val referenceDecreasing: Float = 0f,
    val errorIncreasing: Float = 0f,
    val errorDecreasing: Float = 0f,
    val variation: Float = 0f
)

fun generateDefaultGroups(pointCount: Int): List<MeasurementGroup> {
    val step = 100f / (pointCount + 1)
    return listOf(
        MeasurementGroup(
            name = "U=220В",
            measurements = List(pointCount) { idx ->
                VoltageMeasurement(
                    id = idx,
                    scaleMark = (idx + 1) * step
                )
            }
        ),
        MeasurementGroup(
            name = "U=380В",
            measurements = List(pointCount) { idx ->
                VoltageMeasurement(
                    id = pointCount + idx,
                    scaleMark = (idx + 1) * step * 1.5f
                )
            }
        )
    )
}