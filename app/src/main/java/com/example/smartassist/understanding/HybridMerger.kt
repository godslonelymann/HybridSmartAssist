package com.example.smartassist.understanding

import com.example.smartassist.ocr.TextBlock
import org.json.JSONObject

object HybridMerger {

    fun merge(
        ocrBlocks: List<TextBlock>,
        groqRaw: String?
    ): ScreenUnderstandingResult {

        val ocrText =
            ocrBlocks
                .map { it.text.trim() }
                .distinct()
                .joinToString("\n")

        if (groqRaw.isNullOrBlank()) {
            return ScreenUnderstandingResult(
                screenType = "CONTENT",
                primaryAction = null,
                summary = ocrText,
                confidence = 0.5f
            )
        }

        return try {

            // Try JSON parsing first
            val json = JSONObject(groqRaw)

            val screenType =
                json.optString("screenType", "CONTENT")

            val primaryAction =
                json.optString("primaryAction")
                    .takeIf { it.isNotBlank() }

            val visualDescription =
                json.optString("visualDescription", "")

            val confidence =
                json.optDouble("confidence", 0.8)
                    .toFloat()
                    .coerceIn(0f, 1f)

            val finalSummary =
                buildString {
                    if (visualDescription.isNotBlank()) {
                        appendLine(visualDescription)
                    }
                    if (ocrText.isNotBlank()) {
                        appendLine()
                        appendLine("Text on screen:")
                        appendLine(ocrText)
                    }
                }.trim()

            ScreenUnderstandingResult(
                screenType = screenType,
                primaryAction = primaryAction,
                summary = finalSummary,
                confidence = confidence
            )

        } catch (e: Exception) {

            // Not JSON → treat as natural LLM explanation

            ScreenUnderstandingResult(
                screenType = "CONTENT",
                primaryAction = null,
                summary = groqRaw.trim(),
                confidence = 0.85f
            )
        }
    }
}