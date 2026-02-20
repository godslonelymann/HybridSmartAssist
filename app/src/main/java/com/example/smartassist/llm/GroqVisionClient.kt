package com.example.smartassist.llm

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GroqVisionClient(
    private val apiKey: String
) {

    companion object {
        private const val TAG = "GroqVisionClient"
        private const val MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"
        private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    }

    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    suspend fun analyzeScreen(
        screenshot: Bitmap,
        ocrText: String
    ): String? = withContext(Dispatchers.IO) {

        try {

            Log.d(TAG, "Starting Groq Vision request")

            val base64Image = bitmapToBase64(screenshot)

            val requestJson = buildRequest(base64Image, ocrText)

            Log.d(TAG, "Request JSON: $requestJson")

            val requestBody =
                requestJson
                    .toString()
                    .toRequestBody("application/json".toMediaType())

            val request =
                Request.Builder()
                    .url(ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .build()

            httpClient.newCall(request).execute().use { response ->

                val bodyString = response.body?.string()

                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $bodyString")

                if (!response.isSuccessful) {
                    Log.e(TAG, "Groq Vision failed: ${response.code} ${response.message}")
                    return@withContext null
                }

                if (bodyString.isNullOrBlank()) return@withContext null

                extractText(bodyString)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Groq Vision exception", e)
            null
        }
    }

    private fun buildRequest(
        imageBase64: String,
        ocrText: String
    ): JSONObject {

        val prompt = """
You are a screen understanding assistant.

Analyze the screenshot and OCR text.
Return a structured explanation in natural language.

OCR Text:
$ocrText

Describe:
1. What type of screen this is
2. What the user can do here
3. Important visible elements
4. Clear summary

Keep it concise.
        """.trimIndent()

        val contentParts = JSONArray()
            .put(JSONObject().put("type", "text").put("text", prompt))
            .put(
                JSONObject()
                    .put("type", "image_url")
                    .put(
                        "image_url",
                        JSONObject()
                            .put("url", "data:image/png;base64,$imageBase64")
                    )
            )

        val message = JSONObject()
            .put("role", "user")
            .put("content", contentParts)

        return JSONObject()
            .put("model", MODEL)
            .put("messages", JSONArray().put(message))
            .put("temperature", 0.2)
            .put("max_tokens", 800)
    }

    private fun extractText(responseJson: String): String? {
        return try {

            val root = JSONObject(responseJson)
            val choices = root.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null

            val message =
                choices
                    .getJSONObject(0)
                    .getJSONObject("message")

            message.optString("content", null)

        } catch (e: Exception) {
            Log.e(TAG, "Groq parse error", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}