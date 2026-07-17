package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.core.network.R
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini adapter behind [SuggestionGenerator], through Firebase AI Logic: no API key ships in the
 * app, and App Check (Play Integrity) gates who may call the backend.
 */
@Singleton
class GeminiService
    @Inject
    constructor(
        private val resourcesProvider: ResourcesProvider,
    ) : SuggestionGenerator {
        private val systemInstruction: String
            get() = resourcesProvider.getString(R.string.weather_assistant_instruction)

        private val model: GenerativeModel by lazy {
            Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                // gemini-2.5-flash is closed to new API users; 3.5-flash is its GA successor.
                modelName = "gemini-3.5-flash",
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
