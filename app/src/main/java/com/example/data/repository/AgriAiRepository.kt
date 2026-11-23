package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.AniLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.FarmBudget
import com.example.data.model.PlantDiagnosisResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AgriAiRepository"
private const val AI_TIMEOUT_MS = 60_000L

class AgriAiRepository {

    private val plantDoctorSchema = Schema.obj(
        properties = mapOf(
            "cropName" to Schema.string(description = "Identified crop or plant name, e.g. Rice (Palay), Tomato (Kamatis)"),
            "conditionName" to Schema.string(description = "Likely disease, pest problem, nutrient deficiency, or Healthy"),
            "severity" to Schema.enumeration(
                values = listOf("Low", "Moderate", "High"),
                description = "Estimated urgency level"
            ),
            "summary" to Schema.string(description = "Simple, clear explanation for a Filipino farmer or student (max 3 sentences)"),
            "organicSolutions" to Schema.array(
                items = Schema.string(),
                description = "2 to 4 practical, low-cost organic or safe treatment steps"
            ),
            "preventionTips" to Schema.array(
                items = Schema.string(),
                description = "2 to 3 prevention tips for the next cropping cycle"
            )
        )
    )

    private val plantDoctorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = plantDoctorSchema
            }
        )
    }

    private val aniAssistantModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are Ani, a friendly, practical, and responsible Filipino agricultural assistant in the AgriQuest AI app. " +
                        "You help Filipino farmers and students solve crop diseases, pests, soil problems, weather stress, and high farming costs. " +
                        "Always give clear, step-by-step, low-cost, and environmentally safe advice. Keep answers concise (3-6 short bullets or paragraphs) and never invent fake statistics. " +
                        "When diagnosing serious outbreaks or recommending regulated chemicals, gently remind users to consult their local Municipal Agriculture Office (MAO) or PhilRice/DA technician."
                )
            }
        )
    }

    private val budgetTipModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite"
        )
    }

    suspend fun analyzePlantPhoto(
        bitmap: Bitmap,
        cropHint: String
    ): PlantDiagnosisResult {
        val hintText = if (cropHint.isBlank()) "Unknown crop" else cropHint.trim()
        val promptText = """
            Examine this plant/leaf photo carefully for a Filipino farmer or agricultural student.
            User's crop hint: $hintText.
            Identify the plant if visible, diagnose any visible disease, pest damage, or nutrient issue (or note if it looks healthy), and suggest safe, practical, affordable Filipino farming solutions and prevention tips.
            Keep the language simple, clear, and responsible. Note that visual inspection is an initial screening.
        """.trimIndent()

        return try {
            val response = withTimeout(AI_TIMEOUT_MS) {
                plantDoctorModel.generateContent(
                    content {
                        image(bitmap)
                        text(promptText)
                    }
                )
            }
            val rawJson = response.text ?: throw IllegalStateException("AI returned an empty diagnosis.")
            val json = JSONObject(rawJson)
            val solutionsJson = json.optJSONArray("organicSolutions")
            val preventionJson = json.optJSONArray("preventionTips")
            val solutions = buildList {
                if (solutionsJson != null) {
                    for (i in 0 until solutionsJson.length()) {
                        val item = solutionsJson.optString(i)
                        if (item.isNotBlank()) add(item)
                    }
                }
            }
            val tips = buildList {
                if (preventionJson != null) {
                    for (i in 0 until preventionJson.length()) {
                        val item = preventionJson.optString(i)
                        if (item.isNotBlank()) add(item)
                    }
                }
            }
            PlantDiagnosisResult(
                cropName = json.optString("cropName", hintText).ifBlank { hintText },
                conditionName = json.optString("conditionName", "Visual Assessment"),
                severity = json.optString("severity", "Moderate"),
                summary = json.optString("summary", "Initial AI visual screening completed."),
                organicSolutions = solutions.ifEmpty { listOf("Inspect undersides of leaves and remove severely damaged foliage.") },
                preventionTips = tips.ifEmpty { listOf("Ensure proper spacing, drainage, and regular field scouting.") },
                isDemoMode = false
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Plant diagnosis timed out", e)
            throw IllegalStateException("AI Plant Doctor timed out after 60 seconds. Please try again or test a Demo Case.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Plant diagnosis failed", e)
            throw e
        }
    }

    fun streamAniReply(
        history: List<ChatMessage>,
        question: String,
        language: AniLanguage
    ): Flow<String> = flow {
        val chatHistory: List<Content> = history
            .filter { !it.isError && !it.isStreaming && it.text.isNotBlank() }
            .takeLast(8)
            .map { msg ->
                content(if (msg.isFromUser) "user" else "model") {
                    text(msg.text)
                }
            }

        val fullPrompt = "${language.instruction}\n\nQuestion: $question"
        val chat = aniAssistantModel.startChat(history = chatHistory)
        val accumulated = StringBuilder()

        withTimeout(AI_TIMEOUT_MS) {
            chat.sendMessageStream(fullPrompt).collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    accumulated.append(piece)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("Ani returned an empty response. Please try again.")
        }
    }

    suspend fun getBudgetOptimizationTip(budget: FarmBudget): String {
        val prompt = """
            A Filipino farmer is planning a budget for ${budget.cropName} (${budget.farmAreaLabel}):
            - Seeds: ₱${budget.seedsCost.toInt()}
            - Fertilizer: ₱${budget.fertilizerCost.toInt()}
            - Pest Control: ₱${budget.pestControlCost.toInt()}
            - Labor: ₱${budget.laborCost.toInt()}
            - Other costs: ₱${budget.otherCost.toInt()}
            - Total Expense: ₱${budget.totalExpense.toInt()}
            - Expected Yield: ${budget.expectedYieldKg.toInt()} kg at ₱${budget.pricePerKg}/kg
            - Projected Net Profit: ₱${budget.netProfit.toInt()}

            Give 3 short, practical Philippine-context tips in simple English to lower farming costs or protect profit margin. Keep under 120 words.
        """.trimIndent()

        val response = withTimeout(AI_TIMEOUT_MS) {
            budgetTipModel.generateContent(prompt)
        }
        return response.text?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Empty budget tip returned.")
    }

    fun buildDemoAniReply(question: String, language: AniLanguage): String {
        val q = question.lowercase()
        val isTaglish = language != AniLanguage.SIMPLE_ENGLISH
        return when {
            "yellow" in q || "dilaw" in q || "rice" in q || "palay" in q -> {
                if (isTaglish) {
                    "[Demo Mode — Offline Sample]\n\nKapag naninilaw ang dahon ng palay:\n• Suriin muna kung kulang sa Nitrogen gamit ang Leaf Color Chart (LCC) bago magdagdag ng urea.\n• I-check ang drainage — ang sobrang babad na tubig ay nakakasira ng ugat.\n• Maghalo ng decomposed compost o Carbonized Rice Hull para mas gumanda ang pagsipsip ng sustansya."
                } else {
                    "[Demo Mode — Offline Sample]\n\nWhen rice leaves turn yellow:\n• Check with a Leaf Color Chart (LCC) before buying extra urea—sometimes it is waterlogging or root stress rather than lack of nitrogen.\n• Ensure steady drainage so roots can breathe.\n• Mix decomposed rice straw compost into soil between croppings to strengthen nutrient uptake."
                }
            }
            "compost" in q || "soil" in q || "lupa" in q -> {
                "[Demo Mode — Offline Sample]\n\nEasy 4-Week Farm Composting:\n• Layer 3 parts dried browns (rice straw, dry leaves) with 1 part greens (kakawate leaves, kitchen scraps, aged manure).\n• Keep moist like a wrung-out sponge and turn every 7–10 days.\n• This enriches soil microbes and cuts chemical fertilizer bills!"
            }
            "pest" in q || "insect" in q || "peste" in q || "aphid" in q -> {
                "[Demo Mode — Offline Sample]\n\nLow-Cost Natural Pest Patrol:\n• Scout 10 plants twice a week and inspect leaf undersides.\n• Hang yellow sticky traps for whiteflies and plant marigolds around borders.\n• For mild outbreaks, apply diluted chili-garlic-soap or neem oil spray late in the afternoon."
            }
            else -> {
                "[Demo Mode — Offline Sample]\n\nMagandang araw! Here is a smart farming tip:\n• Start with healthy, well-drained soil mixed with organic compost.\n• Track every peso spent on seeds, fertilizer, and labor in the Farm Budget Challenge.\n• Connect online with Firebase AI Logic active to ask Ani custom questions anytime!"
            }
        }
    }
}
