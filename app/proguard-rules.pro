# R8 runs in full mode for release builds. The rules below keep the reflection-driven parts
# (Gson models, Retrofit services, the Gemini SDK) that R8 cannot see being used.

# Keep generic signatures and annotations needed by Gson/Retrofit reflection.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes EnclosingMethod,InnerClasses

# --- App data models (serialized/deserialized by Gson via reflection) ---
-keep class com.erendogan6.havatahminim.model.** { *; }

# Keep every @SerializedName-annotated field name from being renamed/removed.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Retrofit / OkHttp ---
# Keep Retrofit service interfaces (their method annotations drive the HTTP calls).
-keep,allowobfuscation interface com.erendogan6.havatahminim.network.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Gson ---
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# --- Google Generative AI (Gemini) SDK ---
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Kotlin enums are accessed by name (PollenType.valueOf); keep their synthetic accessors.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
