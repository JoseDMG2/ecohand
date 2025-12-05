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
        private const val MOVEMENT_THRESHOLD = 0.15f // Umbral mínimo de movimiento para detectar el arco
        private const val MIN_TRAJECTORY_POINTS = 5 // Mínimo de puntos para validar trayectoria
    }

    // Variables para rastrear el movimiento de la letra J
    private val trajectoryPoints = mutableListOf<TrajectoryPoint>()
    private var lastTrajectoryTime = 0L
    private var arcDetectionStarted = false

    // Variables para rastrear el movimiento de la letra Z
    private val zTrajectoryPoints = mutableListOf<TrajectoryPoint>()
    private var zDetectionStarted = false

    data class TrajectoryPoint(
        val x: Float,
        val y: Float,
        val timestamp: Long
    )

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

    /**
     * Valida si la seña corresponde a la letra J
     * Características: Solo meñique extendido, mano se mueve formando un arco
     * Requiere detección de movimiento continuo
     */
    fun validateLetterJ(handResult: HandLandmarkerResult): LetterJValidationResult {
        if (handResult.landmarks().isEmpty()) {
            resetJTrajectory()
            return LetterJValidationResult(false, "No se detecta mano", 0f)
        }

        val landmarks = handResult.landmarks()[0]

        try {
            // 1. Verificar que solo el meñique esté extendido (posición inicial de J)
            val isPinkyExtended = isFingerExtended(landmarks, FingerType.PINKY)
            val isThumbClosed = !isThumbExtended(landmarks)
            val isIndexClosed = !isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleClosed = !isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)

            val correctHandShape = isPinkyExtended && isThumbClosed &&
                                  isIndexClosed && isMiddleClosed && isRingClosed

            if (!correctHandShape) {
                resetJTrajectory()
                return LetterJValidationResult(false, "Forma de mano incorrecta", 0f)
            }

            // 2. Rastrear el movimiento del meñique
            val pinkyTip = landmarks[20] // Punta del meñique
            val currentTime = System.currentTimeMillis()

            // Añadir punto a la trayectoria
            trajectoryPoints.add(TrajectoryPoint(pinkyTip.x(), pinkyTip.y(), currentTime))

            // Limpiar puntos antiguos (más de 2 segundos)
            trajectoryPoints.removeAll { currentTime - it.timestamp > 2000 }

            // 3. Verificar si la trayectoria forma un arco
            if (trajectoryPoints.size >= MIN_TRAJECTORY_POINTS) {
                val arcResult = detectArcMovement(trajectoryPoints)

                if (arcResult.isArc) {
                    // 4. Verificar que la palma haya cambiado de orientación
                    val palmOrientationChanged = checkPalmOrientationChange(landmarks)

                    if (palmOrientationChanged) {
                        val confidence = arcResult.confidence
                        return LetterJValidationResult(true, "¡Arco completado!", confidence)
                    } else {
                        return LetterJValidationResult(false, "Continúa el movimiento en arco", arcResult.confidence)
                    }
                }

                return LetterJValidationResult(false, "Mueve tu mano formando un arco", arcResult.confidence)
            }

            return LetterJValidationResult(false, "Mantén la posición y realiza el movimiento", 0.3f)

        } catch (e: Exception) {
            resetJTrajectory()
            return LetterJValidationResult(false, "Error en validación", 0f)
        }
    }

    /**
     * Detecta si el conjunto de puntos forma un arco
     */
    private fun detectArcMovement(points: List<TrajectoryPoint>): ArcDetectionResult {
        if (points.size < MIN_TRAJECTORY_POINTS) {
            return ArcDetectionResult(false, 0f)
        }

        // Obtener puntos inicial, medio y final
        val startPoint = points.first()
        val endPoint = points.last()
        val midIndex = points.size / 2
        val midPoint = points[midIndex]

        // Calcular el movimiento total
        val totalMovement = distance(startPoint.x, startPoint.y, endPoint.x, endPoint.y)

        if (totalMovement < MOVEMENT_THRESHOLD) {
            return ArcDetectionResult(false, 0.2f)
        }

        // Verificar que el punto medio esté por encima (o desplazado) de la línea recta
        // entre inicio y fin, característico de un arco
        val arcHeight = calculateArcHeight(startPoint, midPoint, endPoint)

        // Un arco debe tener una altura significativa
        val isArc = arcHeight > 0.05f && totalMovement > MOVEMENT_THRESHOLD

        // Calcular confianza basada en la suavidad del arco
        val confidence = if (isArc) {
            min(1.0f, (arcHeight * 10f + totalMovement * 3f))
        } else {
            min(0.5f, totalMovement / MOVEMENT_THRESHOLD * 0.3f)
        }

        return ArcDetectionResult(isArc, confidence)
    }

    /**
     * Calcula la altura del arco respecto a la línea base
     */
    private fun calculateArcHeight(
        start: TrajectoryPoint,
        mid: TrajectoryPoint,
        end: TrajectoryPoint
    ): Float {
        // Calcular la distancia perpendicular del punto medio a la línea inicio-fin
        val lineLength = distance(start.x, start.y, end.x, end.y)

        if (lineLength < 0.01f) return 0f

        // Fórmula de distancia punto a línea
        val numerator = abs(
            (end.y - start.y) * mid.x -
            (end.x - start.x) * mid.y +
            end.x * start.y -
            end.y * start.x
        )

        return numerator / lineLength
    }

    /**
     * Verifica si la orientación de la palma ha cambiado durante el movimiento
     */
    private fun checkPalmOrientationChange(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>
    ): Boolean {
        // Para simplificar, verificamos la posición relativa de la muñeca respecto a los dedos
        val wrist = landmarks[0]
        val middleMcp = landmarks[9]

        // Si la muñeca está significativamente más arriba que el MCP del dedo medio,
        // significa que la mano ha rotado
        return wrist.y() < middleMcp.y() - 0.05f
    }

    /**
     * Resetea el rastreo de trayectoria para la letra J
     */
    fun resetJTrajectory() {
        trajectoryPoints.clear()
        arcDetectionStarted = false
        lastTrajectoryTime = 0L
    }

    /**
     * Valida si la seña corresponde a la letra Z
     * Características: Solo índice extendido, mano se mueve trazando una Z
     * Requiere detección de movimiento continuo formando una Z
     */
    fun validateLetterZ(handResult: HandLandmarkerResult): LetterZValidationResult {
        if (handResult.landmarks().isEmpty()) {
            resetZTrajectory()
            return LetterZValidationResult(false, "No se detecta mano", 0f)
        }

        val landmarks = handResult.landmarks()[0]

        try {
            // 1. Verificar que solo el índice esté extendido (posición de Z)
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isThumbClosed = !isThumbExtended(landmarks)
            val isMiddleClosed = !isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)
            val isPinkyClosed = !isFingerExtended(landmarks, FingerType.PINKY)

            val correctHandShape = isIndexExtended && isThumbClosed &&
                                  isMiddleClosed && isRingClosed && isPinkyClosed

            if (!correctHandShape) {
                resetZTrajectory()
                return LetterZValidationResult(false, "Forma de mano incorrecta - solo índice extendido", 0f)
            }

            // 2. Rastrear el movimiento del índice
            val indexTip = landmarks[8] // Punta del índice
            val currentTime = System.currentTimeMillis()

            // Añadir punto a la trayectoria
            zTrajectoryPoints.add(TrajectoryPoint(indexTip.x(), indexTip.y(), currentTime))

            // Limpiar puntos antiguos (más de 3 segundos para dar tiempo de trazar la Z)
            zTrajectoryPoints.removeAll { currentTime - it.timestamp > 3000 }

            // 3. Verificar si la trayectoria forma una Z
            if (zTrajectoryPoints.size >= MIN_TRAJECTORY_POINTS * 2) { // Necesitamos más puntos para una Z
                val zResult = detectZMovement(zTrajectoryPoints)

                if (zResult.isZ) {
                    val confidence = zResult.confidence
                    return LetterZValidationResult(true, "¡Z completada!", confidence)
                }

                return LetterZValidationResult(false, "Traza una Z: línea horizontal → diagonal → línea horizontal", zResult.confidence)
            }

            return LetterZValidationResult(false, "Mantén la posición y traza una Z con tu dedo", 0.3f)

        } catch (e: Exception) {
            resetZTrajectory()
            return LetterZValidationResult(false, "Error en validación", 0f)
        }
    }

    /**
     * Detecta si el conjunto de puntos forma una Z
     * Una Z consiste en: línea horizontal superior → diagonal descendente → línea horizontal inferior
     */
    private fun detectZMovement(points: List<TrajectoryPoint>): ZDetectionResult {
        if (points.size < MIN_TRAJECTORY_POINTS * 2) {
            return ZDetectionResult(false, 0f)
        }

        // Dividir la trayectoria en tres segmentos
        val segmentSize = points.size / 3
        if (segmentSize < 2) {
            return ZDetectionResult(false, 0.1f)
        }

        val segment1 = points.subList(0, segmentSize) // Línea horizontal superior
        val segment2 = points.subList(segmentSize, segmentSize * 2) // Diagonal
        val segment3 = points.subList(segmentSize * 2, points.size) // Línea horizontal inferior

        // Verificar características de cada segmento
        val horizontalTop = isHorizontalMovement(segment1)
        val diagonal = isDiagonalMovement(segment2)
        val horizontalBottom = isHorizontalMovement(segment3)

        // Verificar dirección general: de arriba-izquierda a abajo-derecha
        val overallDirection = checkZDirection(points.first(), points.last())

        // Calcular confianza
        var confidence = 0f
        if (horizontalTop) confidence += 0.3f
        if (diagonal) confidence += 0.3f
        if (horizontalBottom) confidence += 0.3f
        if (overallDirection) confidence += 0.1f

        val isZ = confidence >= 0.7f

        return ZDetectionResult(isZ, confidence)
    }

    /**
     * Verifica si un segmento de puntos representa un movimiento horizontal
     */
    private fun isHorizontalMovement(segment: List<TrajectoryPoint>): Boolean {
        if (segment.size < 2) return false

        val start = segment.first()
        val end = segment.last()

        // Movimiento horizontal: cambio en X mayor que cambio en Y
        val deltaX = abs(end.x - start.x)
        val deltaY = abs(end.y - start.y)

        return deltaX > deltaY && deltaX > 0.05f
    }

    /**
     * Verifica si un segmento de puntos representa un movimiento diagonal
     */
    private fun isDiagonalMovement(segment: List<TrajectoryPoint>): Boolean {
        if (segment.size < 2) return false

        val start = segment.first()
        val end = segment.last()

        val deltaX = abs(end.x - start.x)
        val deltaY = abs(end.y - start.y)

        // Movimiento diagonal: cambios en X e Y similares
        val ratio = if (deltaX > deltaY) deltaY / deltaX else deltaX / deltaY

        return ratio > 0.5f && (deltaX > 0.05f || deltaY > 0.05f)
    }

    /**
     * Verifica la dirección general de la Z (debe ir de izquierda-arriba a derecha-abajo en cámara frontal)
     */
    private fun checkZDirection(start: TrajectoryPoint, end: TrajectoryPoint): Boolean {
        // En cámara frontal, Y aumenta hacia abajo, X aumenta hacia la derecha
        // Para una Z típica, el punto final debe estar más abajo que el inicial
        return end.y > start.y
    }

    /**
     * Resetea el rastreo de trayectoria para la letra Z
     */
    fun resetZTrajectory() {
        zTrajectoryPoints.clear()
        zDetectionStarted = false
    }

    // Clases de datos para resultados
    data class LetterJValidationResult(
        val isValid: Boolean,
        val message: String,
        val confidence: Float
    )

    data class LetterZValidationResult(
        val isValid: Boolean,
        val message: String,
        val confidence: Float
    )

    private data class ArcDetectionResult(
        val isArc: Boolean,
        val confidence: Float
    )

    private data class ZDetectionResult(
        val isZ: Boolean,
        val confidence: Float
    )

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
