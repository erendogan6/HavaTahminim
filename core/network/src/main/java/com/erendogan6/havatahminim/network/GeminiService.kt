package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.core.network.BuildConfig.GEMINI_API_KEY
import com.erendogan6.havatahminim.core.network.R
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

/** Gemini SDK adapter behind [SuggestionGenerator]. */
@Singleton
class GeminiService
    @Inject
    constructor(
        private val resourcesProvider: ResourcesProvider,
    ) : SuggestionGenerator {
        private val systemInstruction: String
            get() = resourcesProvider.getString(R.string.weather_assistant_instruction)

        private val model: GenerativeModel by lazy {
            GenerativeModel(
                "gemini-2.5-flash",
                apiKey = GEMINI_API_KEY,
                generationConfig =
                    generationConfig {
                        // Sampling tuned for coherent advice; latency is not critical here.
                        temperature = 0.9f
                        topK = 40
                        topP = 0.95f
                        maxOutputTokens = 8192
                        responseMimeType = "text/plain"
                    },
                systemInstruction = content { text(systemInstruction) },
            )
        }

        override suspend fun generate(userMessage: String): String? = model.generateContent(userMessage).text
    }
