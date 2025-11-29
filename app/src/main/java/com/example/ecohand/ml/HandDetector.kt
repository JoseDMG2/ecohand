package com.example.ecohand.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * HandDetector wraps MediaPipe Hands for detecting hand landmarks in images.
 * It provides a simplified interface for hand detection operations.
 */
class HandDetector(
    private val context: Context,
    private val runningMode: RunningMode = RunningMode.IMAGE,
    private val minHandDetectionConfidence: Float = 0.5f,
    private val minHandTrackingConfidence: Float = 0.5f,
    private val minHandPresenceConfidence: Float = 0.5f,
    private val maxNumHands: Int = 2,
    private val resultListener: ((HandDetectionResult) -> Unit)? = null,
    private val errorListener: ((String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "HandDetector"
        private const val MODEL_ASSET_PATH = "hand_landmarker.task"
    }
    
    private var handLandmarker: HandLandmarker? = null
    private var isInitialized = false
    
    /**
     * Initialize the hand detector with MediaPipe model.
     * Must be called before any detection operations.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath(MODEL_ASSET_PATH)
            
            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setNumHands(maxNumHands)
                .setRunningMode(runningMode)
            
            // Add listeners for live stream mode
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener { result, _ ->
                        val detectionResult = convertToDetectionResult(result, 0, 0)
                        resultListener?.invoke(detectionResult)
                    }
                    .setErrorListener { error ->
                        Log.e(TAG, "MediaPipe error: ${error.message}")
                        errorListener?.invoke(error.message ?: "Unknown error")
                    }
            }
            
            handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
            isInitialized = true
            Log.d(TAG, "HandDetector initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize HandDetector: ${e.message}", e)
            errorListener?.invoke("Failed to initialize: ${e.message}")
            false
        }
    }
    
    /**
     * Detect hands in a bitmap image.
     * 
     * @param bitmap The input image
     * @return HandDetectionResult containing detected landmarks
     */
    suspend fun detectHands(bitmap: Bitmap): HandDetectionResult = withContext(Dispatchers.Default) {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "HandDetector not initialized")
            return@withContext HandDetectionResult(
                hands = emptyList(),
                inferenceTimeMs = 0,
                imageWidth = bitmap.width,
                imageHeight = bitmap.height
            )
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = handLandmarker?.detect(mpImage)
            val inferenceTime = System.currentTimeMillis() - startTime
            
            if (result != null) {
                convertToDetectionResult(result, bitmap.width, bitmap.height, inferenceTime)
            } else {
                HandDetectionResult(
                    hands = emptyList(),
                    inferenceTimeMs = inferenceTime,
                    imageWidth = bitmap.width,
                    imageHeight = bitmap.height
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Detection failed: ${e.message}", e)
            HandDetectionResult(
                hands = emptyList(),
                inferenceTimeMs = 0,
                imageWidth = bitmap.width,
                imageHeight = bitmap.height
            )
        }
    }
    
    /**
     * Detect hands in a live stream frame.
     * Results are delivered via the resultListener callback.
     * 
     * @param bitmap The input frame
     * @param frameTimestampMs The timestamp of the frame in milliseconds
     */
    fun detectAsync(bitmap: Bitmap, frameTimestampMs: Long) {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "HandDetector not initialized")
            return
        }
        
        if (runningMode != RunningMode.LIVE_STREAM) {
            Log.w(TAG, "detectAsync requires LIVE_STREAM running mode")
            return
        }
        
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            handLandmarker?.detectAsync(mpImage, frameTimestampMs)
        } catch (e: Exception) {
            Log.e(TAG, "Async detection failed: ${e.message}", e)
            errorListener?.invoke("Detection failed: ${e.message}")
        }
    }
    
    /**
     * Convert MediaPipe result to our HandDetectionResult format.
     */
    private fun convertToDetectionResult(
        result: HandLandmarkerResult,
        imageWidth: Int,
        imageHeight: Int,
        inferenceTimeMs: Long = 0
    ): HandDetectionResult {
        val hands = mutableListOf<HandLandmarks>()
        
        val landmarks = result.landmarks()
        val handednesses = result.handednesses()
        
        for (i in landmarks.indices) {
            val handLandmarkList = landmarks[i]
            val handednessCategories = if (i < handednesses.size) handednesses[i] else emptyList()
            
            // Get handedness
            val handedness = if (handednessCategories.isNotEmpty()) {
                val category = handednessCategories[0]
                when (category.categoryName().lowercase()) {
                    "left" -> Handedness.LEFT
                    "right" -> Handedness.RIGHT
                    else -> Handedness.UNKNOWN
                }
            } else {
                Handedness.UNKNOWN
            }
            
            // Get confidence
            val confidence = if (handednessCategories.isNotEmpty()) {
                handednessCategories[0].score()
            } else {
                0f
            }
            
            // Convert landmarks
            val points = handLandmarkList.map { landmark ->
                HandLandmarkPoint(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z()
                )
            }
            
            if (points.size == HandLandmarks.NUM_LANDMARKS) {
                hands.add(
                    HandLandmarks(
                        landmarks = points,
                        handedness = handedness,
                        confidence = confidence
                    )
                )
            }
        }
        
        return HandDetectionResult(
            hands = hands,
            inferenceTimeMs = inferenceTimeMs,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )
    }
    
    /**
     * Check if the detector is ready for use.
     */
    fun isReady(): Boolean = isInitialized && handLandmarker != null
    
    /**
     * Release resources. Should be called when the detector is no longer needed.
     */
    fun close() {
        try {
            handLandmarker?.close()
            handLandmarker = null
            isInitialized = false
            Log.d(TAG, "HandDetector closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing HandDetector: ${e.message}", e)
        }
    }
}
