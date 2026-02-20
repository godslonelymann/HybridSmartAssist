package com.example.smartassist.narration

import com.example.smartassist.understanding.ScreenUnderstandingResult

object ScreenNarrationBuilder {

    fun build(
        result: ScreenUnderstandingResult
    ): String {

        val builder = StringBuilder()

        if (result.screenType != "UNKNOWN") {
            builder.append(
                result.screenType
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
            )
            builder.append(" screen.\n\n")
        }

        builder.append(result.summary)

        result.primaryAction?.let {
            builder.append("\n\nMain action: ")
            builder.append(it)
        }

        if (result.confidence < 0.4f) {
            builder.append("\n\nNote: Low confidence understanding.")
        }

        return builder.toString().trim()
    }
}