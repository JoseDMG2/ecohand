package com.example.ecohand.ml

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.*

/**
 * Validador de señas para vocales en lenguaje de señas
 * Analiza landmarks de MediaPipe para determinar si una seña es correcta
 */
class VowelSignValidator {

    companion object {
        private const val FINGER_THRESHOLD = 0.1f // Umbral para determinar si un dedo está extendido
        private const val ANGLE_THRESHOLD = 30f // Umbral para ángulos en grados
    }

    /**
     * Índices de landmarks en MediaPipe Hand Landmarker
     * 0: WRIST (muñeca)
     * 1-4: THUMB (pulgar) - tip: 4
     * 5-8: INDEX (índice) - tip: 8
     * 9-12: MIDDLE (medio) - tip: 12
     * 13-16: RING (anular) - tip: 16
     * 17-20: PINKY (meñique) - tip: 20
     */

    /**
     * Valida si la seña corresponde a la vocal A
     * Características: Pulgar extendido hacia afuera, demás dedos cerrados formando puño
     */
    fun validateLetterA(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0] // Primera mano detectada

        try {
            // Verificar que el pulgar esté extendido
            val isThumbExtended = isThumbExtended(landmarks)

            // Verificar que los demás dedos estén cerrados (formando puño)
            val isIndexClosed = !isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleClosed = !isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)
            val isPinkyClosed = !isFingerExtended(landmarks, FingerType.PINKY)

            return isThumbExtended && isIndexClosed && isMiddleClosed && isRingClosed && isPinkyClosed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde a la vocal E
     * Características: Todos los dedos curvados/flexionados hacia la palma, pulgar cubriendo puntas
     */
    fun validateLetterE(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que todos los dedos estén flexionados/curvados
            val allFingersCurved = !isFingerExtended(landmarks, FingerType.INDEX) &&
                                 !isFingerExtended(landmarks, FingerType.MIDDLE) &&
                                 !isFingerExtended(landmarks, FingerType.RING) &&
                                 !isFingerExtended(landmarks, FingerType.PINKY)

            // Verificar que el pulgar esté posicionado sobre los demás dedos
            val thumbPosition = isThumbCoveringFingers(landmarks)

            return allFingersCurved && thumbPosition
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde a la vocal I
     * Características: Solo meñique extendido hacia arriba, demás dedos cerrados
     */
    fun validateLetterI(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que solo el meñique esté extendido
            val isPinkyExtended = isFingerExtended(landmarks, FingerType.PINKY)
            val isThumbClosed = !isThumbExtended(landmarks)
            val isIndexClosed = !isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleClosed = !isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)

            return isPinkyExtended && isThumbClosed && isIndexClosed && isMiddleClosed && isRingClosed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde a la vocal O
     * Características: Todos los dedos curvados formando un círculo/óvalo
     */
    fun validateLetterO(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que los dedos formen una forma circular
            // El pulgar debe tocar aproximadamente las puntas de los demás dedos
            val fingertipsClose = areFingertipsFormingCircle(landmarks)

            // Los dedos no deben estar completamente extendidos ni completamente cerrados
            val fingersPartiallyExtended = areFingersCurvedForO(landmarks)

            return fingertipsClose && fingersPartiallyExtended
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde a la vocal U
     * Características: Índice y meñique extendidos hacia arriba, medio, anular y pulgar cerrados
     */
    fun validateLetterU(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que índice y meñique estén extendidos
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isPinkyExtended = isFingerExtended(landmarks, FingerType.PINKY)

            // Verificar que demás dedos estén cerrados
            val isThumbClosed = !isThumbExtended(landmarks)
            val isMiddleClosed = !isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)

            return isIndexExtended && isPinkyExtended &&
                   isThumbClosed && isMiddleClosed && isRingClosed
        } catch (e: Exception) {
            return false
        }
    }

    // Funciones auxiliares para análisis de landmarks

    private enum class FingerType(val tipIndex: Int, val pipIndex: Int, val mcpIndex: Int) {
        INDEX(8, 7, 5),
        MIDDLE(12, 11, 9),
        RING(16, 15, 13),
        PINKY(20, 19, 17)
    }

    /**
     * Determina si un dedo específico está extendido
     */
    private fun isFingerExtended(landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>, finger: FingerType): Boolean {
        val tipY = landmarks[finger.tipIndex].y()
        val pipY = landmarks[finger.pipIndex].y()
        val mcpY = landmarks[finger.mcpIndex].y()

        // Un dedo está extendido si la punta está más arriba que las articulaciones
        return tipY < pipY && pipY < mcpY
    }

    /**
     * Determina si el pulgar está extendido
     */
    private fun isThumbExtended(landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): Boolean {
        val thumbTip = landmarks[4]
        val thumbMcp = landmarks[2]
        val wrist = landmarks[0]

        // Calcular la distancia del pulgar desde la muñeca
        val tipDistance = distance(thumbTip.x(), thumbTip.y(), wrist.x(), wrist.y())
        val mcpDistance = distance(thumbMcp.x(), thumbMcp.y(), wrist.x(), wrist.y())

        return tipDistance > mcpDistance * 1.2f
    }

    /**
     * Verifica si el pulgar está cubriendo las puntas de los dedos (para letra E)
     */
    private fun isThumbCoveringFingers(landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): Boolean {
        val thumbTip = landmarks[4]
        val indexTip = landmarks[8]
        val middleTip = landmarks[12]

        // El pulgar debe estar cerca de las puntas de los otros dedos
        val distToIndex = distance(thumbTip.x(), thumbTip.y(), indexTip.x(), indexTip.y())
        val distToMiddle = distance(thumbTip.x(), thumbTip.y(), middleTip.x(), middleTip.y())

        return distToIndex < 0.05f || distToMiddle < 0.05f
    }

    /**
     * Verifica si las puntas de los dedos forman un círculo (para letra O)
     */
    private fun areFingertipsFormingCircle(landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): Boolean {
        val thumbTip = landmarks[4]
        val indexTip = landmarks[8]
        val middleTip = landmarks[12]

        // Las puntas deben estar relativamente cerca entre sí
        val thumbIndexDist = distance(thumbTip.x(), thumbTip.y(), indexTip.x(), indexTip.y())
        val thumbMiddleDist = distance(thumbTip.x(), thumbTip.y(), middleTip.x(), middleTip.y())

        return thumbIndexDist < 0.08f && thumbMiddleDist < 0.1f
    }

    /**
     * Verifica si los dedos están curvados apropiadamente para la letra O
     */
    private fun areFingersCurvedForO(landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): Boolean {
        // Los dedos no deben estar completamente extendidos
        val indexExtended = isFingerExtended(landmarks, FingerType.INDEX)
        val middleExtended = isFingerExtended(landmarks, FingerType.MIDDLE)

        return !indexExtended && !middleExtended
    }


    /**
     * Calcula la distancia euclidiana entre dos puntos
     */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}
