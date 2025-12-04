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

/**
 * Detector de manos utilizando MediaPipe Hand Landmarker
 * Detecta hasta 2 manos y retorna landmarks (21 puntos por mano)
 */
class HandDetector(
    private val context: Context,
    private val minHandDetectionConfidence: Float = 0.5f,
    private val minHandTrackingConfidence: Float = 0.5f,
    private val minHandPresenceConfidence: Float = 0.5f,
    private val maxNumHands: Int = 2,
    private val runningMode: RunningMode = RunningMode.LIVE_STREAM
) {
    private var handLandmarker: HandLandmarker? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "HandDetector"
        private const val MODEL_FILE = "hand_landmarker.task"
    }

    /**
     * Inicializa el detector de manos
     */
    fun initialize(onResultsListener: (HandLandmarkerResult, Long) -> Unit): Boolean {
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_FILE)
                .setDelegate(Delegate.GPU) // Usar GPU para mejor rendimiento
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setNumHands(maxNumHands)
                .setRunningMode(runningMode)
                .apply {
                    if (runningMode == RunningMode.LIVE_STREAM) {
                        setResultListener { result, input ->
                            onResultsListener(result, System.currentTimeMillis())
                        }
                        setErrorListener { error ->
                            Log.e(TAG, "Hand detection error: ${error.message}")
                        }
                    }
                }
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "Hand detector initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing hand detector: ${e.message}", e)
            false
        }
    }

    /**
     * Detecta manos en un frame (modo LIVE_STREAM)
     * @param bitmap Frame de la cámara
     * @param frameTime Timestamp del frame en millisegundos
     */
    fun detectAsync(bitmap: Bitmap, frameTime: Long) {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "Hand detector not initialized")
            return
        }

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            handLandmarker?.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting hands: ${e.message}", e)
        }
    }

    /**
     * Detecta manos en un frame (modo IMAGE)
     * @param bitmap Imagen a procesar
     * @return Resultado con landmarks detectados
     */
    fun detect(bitmap: Bitmap): HandLandmarkerResult? {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "Hand detector not initialized")
            return null
        }

        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            handLandmarker?.detect(mpImage)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting hands: ${e.message}", e)
            null
        }
    }

    /**
     * Libera recursos del detector
     */
    fun close() {
        try {
            handLandmarker?.close()
            handLandmarker = null
            isInitialized = false
            Log.d(TAG, "Hand detector closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing hand detector: ${e.message}", e)
        }
    }

    /**
     * Verifica si el detector está inicializado
     */
    fun isReady(): Boolean = isInitialized
}

