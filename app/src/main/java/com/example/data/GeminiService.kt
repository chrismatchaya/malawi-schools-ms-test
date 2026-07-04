package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiRepository {
    suspend fun generateReportAnalysis(
        district: String,
        schoolCount: Int,
        estimatedStudents: Int,
        attendanceRate: Double,
        teacherScore: Double,
        pendingSyncCount: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder") {
            return@withContext """
                [AI Notice: Demo Mode Active]
                Please configure a valid 'GEMINI_API_KEY' in the AI Studio Secrets Panel to enable real cloud-based machine learning analysis.
                
                DEMO RECOMMENDATIONS & ANALYSIS:
                1. District '$district' currently logs an estimated attendance rate of ${"%.1f".format(attendanceRate)}% across $schoolCount rural facilities.
                2. Teacher effectiveness stands at ${"%.2f".format(teacherScore)}/5.0. Targeted instructional coaching is recommended.
                3. Modern cryptographic synchronization is active with $pendingSyncCount local packets ready to replicate.
            """.trimIndent()
        }

        val systemInstruction = "You are a senior educational policy advisor for the Ministry of Education in Malawi. Analyze the following school monitoring metrics and provide a high-quality, professional, concise assessment (maximum 150 words) with 3 key actionable recommendations."
        val prompt = """
            Analyze the following monitoring data for Malawi School District:
            - District Name: $district
            - Active Managed Schools: $schoolCount
            - Tracked Students: $estimatedStudents
            - Average Student Attendance Rate: ${"%.1f".format(attendanceRate)}%
            - Teacher Performance Score: ${"%.2f".format(teacherScore)} / 5.0
            - Sync Queue Pending: $pendingSyncCount records
            
            Based on these indicators, write a brief regional progress assessment and suggest 3 hyper-targeted, realistic interventions for rural Malawian classrooms.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "$systemInstruction\n\n$prompt")
                    )
                )
            )
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "AI analysis generated successfully but returned empty details. Please verify input parameters."
        } catch (e: Exception) {
            "AI compilation failed to communicate with Lilongwe central analysis models: ${e.message ?: "Unknown Connection Error"}"
        }
    }
}
