package com.atrajit.fluppymodigame.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiAIManager(private val context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "GeminiAIManager"
        private const val API_KEY_PREF = "gemini_api_key"
        private const val GEMINI_API_URL = 
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    }
    
    fun getApiKey(): String? {
        return prefs.getString(API_KEY_PREF, null)
    }
    
    fun setApiKey(apiKey: String) {
        prefs.edit().putString(API_KEY_PREF, apiKey).apply()
    }
    
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrEmpty()
    }
    
    suspend fun generateDifficultyAdjustment(
        currentScore: Int,
        hitCount: Int,
        avgSurvivalTime: Float,
        currentSpeed: Float,
        currentGap: Float
    ): DifficultyAdjustment = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey() ?: return@withContext DifficultyAdjustment()
            
            val prompt = """
                Analyze this Flappy Bird style game performance and suggest difficulty adjustments:
                
                Current Score: $currentScore
                Hit Count: $hitCount
                Average Survival Time: ${avgSurvivalTime}s
                Current Speed: $currentSpeed
                Current Gap: $currentGap
                
                Based on this data, suggest:
                1. Speed multiplier (0.8 to 1.5) - lower if player struggling, higher if doing well
                2. Gap multiplier (0.8 to 1.3) - lower (harder) if doing well, higher (easier) if struggling
                
                Return ONLY a JSON object with this exact format:
                {
                  "speedMultiplier": 1.0,
                  "gapMultiplier": 1.0,
                  "reasoning": "brief explanation"
                }
            """.trimIndent()
            
            val response = callGeminiAPI(apiKey, prompt)
            parseDifficultyResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            DifficultyAdjustment() // Return default
        }
    }
    
    suspend fun generateCommentary(
        scoreLevel: Int,
        currentScore: Int,
        recentAction: String, // "flying", "close_call", "scoring"
        speaker: String // "mamata" or "modi"
    ): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                Log.e(TAG, "No API key found")
                return@withContext ""
            }
            
            Log.d(TAG, "Generating commentary for speaker=$speaker, score=$currentScore, action=$recentAction")
            
            val levelContext = when (scoreLevel) {
                in 0..20 -> "casual and playful, space theme"
                in 21..30 -> "flirty and provocative, DJ party theme. Mamata challenges Modi with suggestive comments like 'Come on Modi, hit me harder!' or 'Is that all you got?'"
                in 31..40 -> "intense and aggressive, fiery hell theme. Both are angry and competitive"
                in 41..50 -> "mystical and philosophical, Naruto genjutsu theme. References to chakra and ninja techniques"
                else -> "spooky and eerie, haunted theme. Ghost-like and supernatural references"
            }
            
            val prompt = """
                Generate a SHORT commentary line (max 10 words) for a Flappy Bird game where Modi is the bird and Mamata is the obstacle.
                
                Context:
                - Score: $currentScore
                - Theme Level: $levelContext
                - Speaker: ${if (speaker == "mamata") "Mamata (the obstacle)" else "Modi (the flying bird)"}
                - Current Action: $recentAction
                
                ${if (speaker == "mamata") {
                    when (scoreLevel) {
                        in 0..20 -> "Mamata should taunt Modi playfully"
                        in 21..30 -> "Mamata should be flirty and provocative, teasing Modi with suggestive challenges"
                        in 31..40 -> "Mamata should be angry and aggressive"
                        in 41..50 -> "Mamata should speak like a ninja villain"
                        else -> "Mamata should sound like a ghost"
                    }
                } else {
                    when (scoreLevel) {
                        in 0..20 -> "Modi should respond confidently"
                        in 21..30 -> "Modi should be determined but flustered by Mamata's teasing"
                        in 31..40 -> "Modi should sound fierce and determined"
                        in 41..50 -> "Modi should use ninja-style determination"
                        else -> "Modi should be brave despite fear"
                    }
                }}
                
                Return ONLY the dialogue line, no quotes, no explanation.
            """.trimIndent()
            
            val response = callGeminiAPI(apiKey, prompt)
            val commentary = parseCommentaryResponse(response)
            Log.d(TAG, "Generated commentary: $commentary")
            commentary
        } catch (e: Exception) {
            Log.e(TAG, "Error generating commentary", e)
            e.printStackTrace()
            "" // Return empty if error
        }
    }
    
    private fun callGeminiAPI(apiKey: String, prompt: String): String {
        val url = URL("$GEMINI_API_URL?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }
            
            Log.d(TAG, "Calling Gemini API...")
            
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "API Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "API Response: ${response.take(200)}")
                return response
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "API call failed with code: $responseCode, error: $errorStream")
                throw Exception("API call failed with code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in API call", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
    
    private fun parseDifficultyResponse(response: String): DifficultyAdjustment {
        try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0)
                .getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            
            // Extract JSON from text (might have markdown code blocks)
            val jsonText = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            val adjustmentJson = JSONObject(jsonText)
            return DifficultyAdjustment(
                speedMultiplier = adjustmentJson.optDouble("speedMultiplier", 1.0).toFloat(),
                gapMultiplier = adjustmentJson.optDouble("gapMultiplier", 1.0).toFloat(),
                reasoning = adjustmentJson.optString("reasoning", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DifficultyAdjustment()
        }
    }
    
    private fun parseCommentaryResponse(response: String): String {
        try {
            Log.d(TAG, "Parsing commentary response...")
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            
            if (candidates.length() == 0) {
                Log.e(TAG, "No candidates in response")
                return ""
            }
            
            val content = candidates.getJSONObject(0)
                .getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            val trimmedText = text.trim()
            Log.d(TAG, "Parsed commentary text: $trimmedText")
            return trimmedText
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing commentary response", e)
            e.printStackTrace()
            return ""
        }
    }
}

data class DifficultyAdjustment(
    val speedMultiplier: Float = 1.0f,
    val gapMultiplier: Float = 1.0f,
    val reasoning: String = ""
)
