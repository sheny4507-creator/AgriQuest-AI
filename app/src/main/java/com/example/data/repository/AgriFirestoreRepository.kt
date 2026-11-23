package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.FarmBudget
import com.example.data.model.FarmPlot
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.PlantScan
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AgriFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeBudgets(userId: String): Flow<List<FarmBudget>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val colRef = db.collection("users").document(userId).collection("budgets")
        emitAll(
            colRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FarmBudget::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeFarmPlots(userId: String): Flow<List<FarmPlot>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val colRef = db.collection("users").document(userId).collection("farmPlots")
        emitAll(
            colRef.orderBy("plotIndex", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FarmPlot::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observePlantScans(userId: String): Flow<List<PlantScan>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val colRef = db.collection("users").document(userId).collection("plantScans")
        emitAll(
            colRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PlantScan::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureInitialized(userId: String, displayName: String?): Result<Unit> {
        val userPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }

            val userRef = db.collection("users").document(uid)
            val existingUser = userRef.get().await()
            if (!existingUser.exists()) {
                val safeName = displayName?.trim()?.take(80)?.takeIf { it.isNotEmpty() } ?: "Kaibigan Farmer"
                userRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to safeName,
                        "xp" to 50,
                        "coins" to 150,
                        "streakDays" to 1,
                        "preferredRegion" to "Nueva Ecija",
                        "completedChallenges" to emptyList<String>(),
                        "earnedBadges" to listOf("seedling"),
                        "studiedGuides" to emptyList<String>(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Ensure 4 virtual farm plots exist
            val plotsCol = userRef.collection("farmPlots")
            val existingPlots = plotsCol.get().await()
            if (existingPlots.isEmpty) {
                for (i in 0..3) {
                    val plotId = "plot_$i"
                    val isStarter = i == 0
                    plotsCol.document(plotId).set(
                        mapOf(
                            "userId" to uid,
                            "plotIndex" to i,
                            "cropId" to if (isStarter) "palay" else "none",
                            "cropName" to if (isStarter) "Palay (Rice)" else "Empty Plot",
                            "growthStage" to if (isStarter) 1 else 0,
                            "waterLevel" to if (isStarter) 70 else 60,
                            "soilHealth" to 85,
                            "pestProtected" to false,
                            "harvestCount" to 0,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }

    suspend fun updatePreferredRegion(userId: String, regionName: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            val safeRegion = regionName.trim().take(60)
            require(safeRegion.isNotEmpty()) { "Region name cannot be empty" }
            db.collection("users").document(uid).update(
                mapOf(
                    "preferredRegion" to safeRegion,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun awardProgress(
        userId: String,
        xpDelta: Int,
        coinDelta: Int,
        newCompletedChallengeId: String? = null,
        newBadgeId: String? = null,
        newStudiedGuideId: String? = null
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            val userRef = db.collection("users").document(uid)
            db.runTransaction { tx ->
                val snap = tx.get(userRef)
                val currentXp = (snap.getLong("xp") ?: 0L).toInt()
                val currentCoins = (snap.getLong("coins") ?: 0L).toInt()
                val challenges = (snap.get("completedChallenges") as? List<*>)
                    ?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                val badges = (snap.get("earnedBadges") as? List<*>)
                    ?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                val guides = (snap.get("studiedGuides") as? List<*>)
                    ?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()

                if (newCompletedChallengeId != null && !challenges.contains(newCompletedChallengeId) && challenges.size < 50) {
                    challenges.add(newCompletedChallengeId)
                }
                if (newBadgeId != null && !badges.contains(newBadgeId) && badges.size < 30) {
                    badges.add(newBadgeId)
                }
                if (newStudiedGuideId != null && !guides.contains(newStudiedGuideId) && guides.size < 50) {
                    guides.add(newStudiedGuideId)
                }
                if (challenges.size >= 2 && !badges.contains("quest_champion") && badges.size < 30) {
                    badges.add("quest_champion")
                }
                if (guides.size >= 2 && !badges.contains("soil_scientist") && badges.size < 30) {
                    badges.add("soil_scientist")
                }

                val nextXp = (currentXp + xpDelta).coerceIn(0, 1_000_000)
                val nextCoins = (currentCoins + coinDelta).coerceIn(0, 1_000_000)

                tx.update(
                    userRef,
                    mapOf(
                        "xp" to nextXp,
                        "coins" to nextCoins,
                        "completedChallenges" to challenges,
                        "earnedBadges" to badges,
                        "studiedGuides" to guides,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveBudget(userId: String, budget: FarmBudget): Result<Unit> {
        var path = "users/$userId/budgets"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            val cropName = budget.cropName.trim().take(60)
            val areaLabel = budget.farmAreaLabel.trim().take(40)
            require(cropName.isNotEmpty() && areaLabel.isNotEmpty()) { "Crop name and farm area are required." }

            val docRef = db.collection("users").document(uid).collection("budgets").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "cropName" to cropName,
                    "farmAreaLabel" to areaLabel,
                    "seedsCost" to budget.seedsCost.coerceIn(0.0, 100_000_000.0),
                    "fertilizerCost" to budget.fertilizerCost.coerceIn(0.0, 100_000_000.0),
                    "pestControlCost" to budget.pestControlCost.coerceIn(0.0, 100_000_000.0),
                    "laborCost" to budget.laborCost.coerceIn(0.0, 100_000_000.0),
                    "otherCost" to budget.otherCost.coerceIn(0.0, 100_000_000.0),
                    "expectedYieldKg" to budget.expectedYieldKg.coerceIn(0.0, 10_000_000.0),
                    "pricePerKg" to budget.pricePerKg.coerceIn(0.0, 1_000_000.0),
                    "totalExpense" to budget.totalExpense.coerceIn(0.0, 500_000_000.0),
                    "grossIncome" to budget.grossIncome.coerceIn(0.0, 1_000_000_000.0),
                    "netProfit" to budget.netProfit.coerceIn(-500_000_000.0, 1_000_000_000.0),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            awardProgress(userId = uid, xpDelta = 20, coinDelta = 15, newBadgeId = "wise_budgeter")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteBudget(userId: String, budgetId: String): Result<Unit> {
        val path = "users/$userId/budgets/$budgetId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            require(budgetId.isNotBlank()) { "Budget ID cannot be blank" }
            db.collection("users").document(uid).collection("budgets").document(budgetId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun updateFarmPlot(
        userId: String,
        plotId: String,
        cropId: String,
        cropName: String,
        growthStage: Int,
        waterLevel: Int,
        soilHealth: Int,
        pestProtected: Boolean,
        harvestCount: Int
    ): Result<Unit> {
        val path = "users/$userId/farmPlots/$plotId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            require(plotId.isNotBlank()) { "Plot ID cannot be blank" }
            db.collection("users").document(uid).collection("farmPlots").document(plotId).update(
                mapOf(
                    "cropId" to cropId.trim().take(40),
                    "cropName" to cropName.trim().take(60),
                    "growthStage" to growthStage.coerceIn(0, 3),
                    "waterLevel" to waterLevel.coerceIn(0, 100),
                    "soilHealth" to soilHealth.coerceIn(0, 100),
                    "pestProtected" to pestProtected,
                    "harvestCount" to harvestCount.coerceIn(0, 100_000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun savePlantScan(userId: String, result: PlantDiagnosisResult): Result<Unit> {
        var path = "users/$userId/plantScans"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            val docRef = db.collection("users").document(uid).collection("plantScans").document()
            path = docRef.path
            val safeSeverity = when (result.severity) {
                "Low", "Moderate", "High" -> result.severity
                else -> "Moderate"
            }
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "cropName" to result.cropName.trim().ifEmpty { "Crop" }.take(60),
                    "conditionName" to result.conditionName.trim().ifEmpty { "Observation" }.take(120),
                    "severity" to safeSeverity,
                    "summary" to result.summary.trim().ifEmpty { "AI visual assessment." }.take(800),
                    "organicSolutions" to result.organicSolutions.take(10),
                    "preventionTips" to result.preventionTips.take(10),
                    "demoMode" to result.isDemoMode,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            awardProgress(userId = uid, xpDelta = 25, coinDelta = 20, newBadgeId = "plant_doctor")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }
}
