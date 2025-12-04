package com.example.ecohand.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

/**
 * Detector de rostro utilizando MediaPipe Face Landmarker
 * Detecta rostros y retorna landmarks (468 puntos por rostro)
 */
class FaceDetector(
    private val context: Context,
    private val minFaceDetectionConfidence: Float = 0.5f,
    private val minFacePresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val maxNumFaces: Int = 1,
    private val runningMode: RunningMode = RunningMode.LIVE_STREAM
) {
    private var faceLandmarker: FaceLandmarker? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "FaceDetector"
        private const val MODEL_FILE = "face_landmarker.task"
    }

    /**
     * Inicializa el detector de rostro
     */
    fun initialize(onResultsListener: (FaceLandmarkerResult, Long) -> Unit): Boolean {
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_FILE)
                .setDelegate(Delegate.GPU) // Usar GPU para mejor rendimiento
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinFaceDetectionConfidence(minFaceDetectionConfidence)
                .setMinFacePresenceConfidence(minFacePresenceConfidence)
                .setMinTrackingConfidence(minTrackingConfidence)
                .setNumFaces(maxNumFaces)
                .setRunningMode(runningMode)
                .setOutputFaceBlendshapes(false) // No necesitamos blendshapes por ahora
                .setOutputFacialTransformationMatrixes(false)
                .apply {
                    if (runningMode == RunningMode.LIVE_STREAM) {
                        setResultListener { result, input ->
                            onResultsListener(result, System.currentTimeMillis())
                        }
                        setErrorListener { error ->
                            Log.e(TAG, "Face detection error: ${error.message}")
                        }
                    }
                }
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "Face detector initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing face detector: ${e.message}", e)
            false
        }
    }

    /**
     * Detecta rostro en un frame (modo LIVE_STREAM)
     * @param bitmap Frame de la cámara
     * @param frameTime Timestamp del frame en millisegundos
     */
    fun detectAsync(bitmap: Bitmap, frameTime: Long) {
        if (!isInitialized || faceLandmarker == null) {
            Log.w(TAG, "Face detector not initialized")
            return
        }

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            faceLandmarker?.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting face: ${e.message}", e)
        }
    }

    /**
     * Detecta rostro en un frame (modo IMAGE)
     * @param bitmap Imagen a procesar
     * @return Resultado con landmarks detectados
     */
    fun detect(bitmap: Bitmap): FaceLandmarkerResult? {
        if (!isInitialized || faceLandmarker == null) {
            Log.w(TAG, "Face detector not initialized")
            return null
        }

        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            faceLandmarker?.detect(mpImage)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting face: ${e.message}", e)
            null
        }
    }

    /**
     * Libera recursos del detector
     */
    fun close() {
        try {
            faceLandmarker?.close()
            faceLandmarker = null
            isInitialized = false
            Log.d(TAG, "Face detector closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing face detector: ${e.message}", e)
        }
    }

    /**
     * Verifica si el detector está inicializado
     */
    fun isReady(): Boolean = isInitialized
}

