package com.example.smartassist.understanding

data class ScreenUnderstandingResult(
    val screenType: String,
    val primaryAction: String?,
    val summary: String,
    val confidence: Float
)