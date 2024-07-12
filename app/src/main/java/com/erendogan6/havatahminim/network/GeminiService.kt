package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {
    val model: GenerativeModel by lazy {
        GenerativeModel(
            "gemini-1.5-pro",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 2f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            systemInstruction = content { text("Sen bir akıllı hava durumu asistanısın. Kullanıcının konum ve mevcut hava durumu bilgilerini alarak, onlara günlük aktiviteler için Türkçe önerilerde bulunuyorsun.") },
        )
    }
}
