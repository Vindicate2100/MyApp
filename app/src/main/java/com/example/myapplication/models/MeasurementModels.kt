package com.example.myapplication.models

data class MeasurementGroup(
    val name: String,
    val maxAllowedError: Float,
    val measurements: List<VoltageMeasurement>,
    val completed: Boolean = false
)

data class VoltageMeasurement(
    val id: Int,
    val scaleMark: Float,
    val referenceIncreasing: Float,
    val referenceDecreasing: Float,
    val errorIncreasing: Float,
    val errorDecreasing: Float,
    val variation: Float
)