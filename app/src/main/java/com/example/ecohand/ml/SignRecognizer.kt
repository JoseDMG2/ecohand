package com.example.ecohand.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SignRecognizer coordinates hand detection and sign classification.
 * It provides a high-level interface for recognizing Peruvian sign language signs.
 */
class SignRecognizer(
    private val context: Context
) {
    companion object {
        private const val TAG = "SignRecognizer"
        
        // Precision threshold for sign recognition (85%)
        const val PRECISION_THRESHOLD = 0.85f
        
        // Minimum confidence for hand detection
        const val MIN_DETECTION_CONFIDENCE = 0.5f
    }
    
    private var handDetector: HandDetector? = null
    private val signClassifier = SignClassifier()
    private val gson = Gson()
    
    // Reference landmarks for each sign (loaded from database)
    private val referenceLandmarks = mutableMapOf<String, HandLandmarks>()
    
    private var isInitialized = false
    
    /**
     * Initialize the recognizer with MediaPipe model.
     */
    suspend fun initialize(): Boolean {
        return try {
            handDetector = HandDetector(
                context = context,
                runningMode = RunningMode.IMAGE,
                minHandDetectionConfidence = MIN_DETECTION_CONFIDENCE,
                minHandTrackingConfidence = MIN_DETECTION_CONFIDENCE,
                minHandPresenceConfidence = MIN_DETECTION_CONFIDENCE,
                maxNumHands = 2
            )
            
            val success = handDetector?.initialize() ?: false
            isInitialized = success
            
            Log.d(TAG, "SignRecognizer initialized: $success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SignRecognizer: ${e.message}", e)
            false
        }
    }
    
    /**
     * Load reference landmarks from database for comparison.
     */
    fun loadReferenceLandmarks(signName: String, landmarksJson: String?) {
        if (landmarksJson.isNullOrEmpty()) return
        
        try {
            val type = object : TypeToken<List<HandLandmarkPoint>>() {}.type
            val points: List<HandLandmarkPoint> = gson.fromJson(landmarksJson, type)
            
            if (points.size == HandLandmarks.NUM_LANDMARKS) {
                referenceLandmarks[signName.lowercase()] = HandLandmarks(
                    landmarks = points,
                    handedness = Handedness.UNKNOWN,
                    confidence = 1f
                )
                Log.d(TAG, "Loaded reference landmarks for: $signName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse landmarks for $signName: ${e.message}")
        }
    }
    
    /**
     * Recognize a sign from an image.
     * 
     * @param bitmap The input image
     * @param expectedSign The sign the user should be performing
     * @return Recognition result with confidence and correctness
     */
    suspend fun recognizeSign(
        bitmap: Bitmap,
        expectedSign: String
    ): SignRecognitionResult = withContext(Dispatchers.Default) {
        if (!isInitialized || handDetector == null) {
            Log.w(TAG, "SignRecognizer not initialized")
            return@withContext SignRecognitionResult(
                signName = null,
                confidence = 0f,
                isCorrect = false,
                expectedSign = expectedSign,
                landmarks = null
            )
        }
        
        // Detect hands
        val detectionResult = handDetector?.detectHands(bitmap)
        
        if (detectionResult == null || !detectionResult.hasHands) {
            return@withContext SignRecognitionResult(
                signName = null,
                confidence = 0f,
                isCorrect = false,
                expectedSign = expectedSign,
                landmarks = null
            )
        }
        
        val landmarks = detectionResult.getFirstHand()!!
        
        // Try to classify using geometric rules
        val (recognizedSign, ruleConfidence) = signClassifier.classifySign(landmarks)
        
        // Also compare with reference landmarks if available
        val expectedLower = expectedSign.lowercase()
        val referenceLandmark = referenceLandmarks[expectedLower]
        
        val comparisonConfidence = if (referenceLandmark != null) {
            signClassifier.compareLandmarks(landmarks, referenceLandmark)
        } else {
            0f
        }
        
        // Use the higher confidence between rule-based and comparison-based
        val finalConfidence = maxOf(
            if (recognizedSign?.lowercase() == expectedLower) ruleConfidence else 0f,
            comparisonConfidence
        )
        
        val isCorrect = finalConfidence >= PRECISION_THRESHOLD
        
        SignRecognitionResult(
            signName = recognizedSign ?: if (comparisonConfidence > 0.5f) expectedSign else null,
            confidence = finalConfidence,
            isCorrect = isCorrect,
            expectedSign = expectedSign,
            landmarks = landmarks
        )
    }
    
    /**
     * Detect hands and return landmarks for data collection.
     */
    suspend fun detectHandLandmarks(bitmap: Bitmap): HandDetectionResult? {
        return handDetector?.detectHands(bitmap)
    }
    
    /**
     * Convert landmarks to JSON for storage.
     */
    fun landmarksToJson(landmarks: HandLandmarks): String {
        return gson.toJson(landmarks.landmarks)
    }
    
    /**
     * Parse landmarks from JSON.
     */
    fun landmarksFromJson(json: String): HandLandmarks? {
        return try {
            val type = object : TypeToken<List<HandLandmarkPoint>>() {}.type
            val points: List<HandLandmarkPoint> = gson.fromJson(json, type)
            
            if (points.size == HandLandmarks.NUM_LANDMARKS) {
                HandLandmarks(
                    landmarks = points,
                    handedness = Handedness.UNKNOWN,
                    confidence = 1f
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse landmarks: ${e.message}")
            null
        }
    }
    
    /**
     * Check if the recognizer is ready.
     */
    fun isReady(): Boolean = isInitialized && handDetector?.isReady() == true
    
    /**
     * Release resources.
     */
    fun close() {
        handDetector?.close()
        handDetector = null
        referenceLandmarks.clear()
        isInitialized = false
    }
}
