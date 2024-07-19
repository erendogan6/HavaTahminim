package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.BuildConfig
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) {
    private val systemInstruction: String
        get() = resourcesProvider.getString(R.string.weather_assistant_instruction)

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
            systemInstruction = content { text(systemInstruction) },
        )
    }
}
