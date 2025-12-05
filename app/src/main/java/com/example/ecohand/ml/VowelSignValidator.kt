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

    /**
     * Valida si la seña corresponde al número 0 (CERO)
     * Características: Todos los dedos extendidos pero curvados formando un círculo/óvalo,
     * con las puntas de los dedos tocando la punta del pulgar (similar a la letra O)
     */
    fun validateNumber0(handResult: HandLandmarkerResult): Boolean {
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
     * Valida si la seña corresponde al número 1 (UNO)
     * Características: Solo el dedo índice extendido hacia arriba, demás dedos cerrados
     */
    fun validateNumber1(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que solo el índice esté extendido
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isThumbClosed = !isThumbExtended(landmarks)
            val isMiddleClosed = !isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)
            val isPinkyClosed = !isFingerExtended(landmarks, FingerType.PINKY)

            return isIndexExtended && isThumbClosed && isMiddleClosed && isRingClosed && isPinkyClosed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde al número 2 (DOS)
     * Características: Dedos índice y medio extendidos hacia arriba formando una V
     */
    fun validateNumber2(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que índice y medio estén extendidos
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleExtended = isFingerExtended(landmarks, FingerType.MIDDLE)

            // Verificar que los demás dedos estén cerrados
            val isThumbClosed = !isThumbExtended(landmarks)
            val isRingClosed = !isFingerExtended(landmarks, FingerType.RING)
            val isPinkyClosed = !isFingerExtended(landmarks, FingerType.PINKY)

            return isIndexExtended && isMiddleExtended &&
                   isThumbClosed && isRingClosed && isPinkyClosed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde al número 3 (TRES)
     * Características: Dedos índice, medio y anular extendidos hacia arriba
     */
    fun validateNumber3(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que índice, medio y anular estén extendidos
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleExtended = isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingExtended = isFingerExtended(landmarks, FingerType.RING)

            // Verificar que pulgar y meñique estén cerrados
            val isThumbClosed = !isThumbExtended(landmarks)
            val isPinkyClosed = !isFingerExtended(landmarks, FingerType.PINKY)

            return isIndexExtended && isMiddleExtended && isRingExtended &&
                   isThumbClosed && isPinkyClosed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde al número 4 (CUATRO)
     * Características: Los cuatro dedos extendidos hacia arriba, pulgar doblado hacia la palma
     */
    fun validateNumber4(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que los cuatro dedos (índice, medio, anular, meñique) estén extendidos
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleExtended = isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingExtended = isFingerExtended(landmarks, FingerType.RING)
            val isPinkyExtended = isFingerExtended(landmarks, FingerType.PINKY)

            // Verificar que el pulgar esté cerrado/doblado
            val isThumbClosed = !isThumbExtended(landmarks)

            return isIndexExtended && isMiddleExtended && isRingExtended &&
                   isPinkyExtended && isThumbClosed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde al número 5 (CINCO)
     * Características: Todos los dedos extendidos y separados, mano abierta completa
     */
    fun validateNumber5(handResult: HandLandmarkerResult): Boolean {
        if (handResult.landmarks().isEmpty()) return false

        val landmarks = handResult.landmarks()[0]

        try {
            // Verificar que TODOS los dedos estén extendidos
            val isThumbExtended = isThumbExtended(landmarks)
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleExtended = isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingExtended = isFingerExtended(landmarks, FingerType.RING)
            val isPinkyExtended = isFingerExtended(landmarks, FingerType.PINKY)

            return isThumbExtended && isIndexExtended && isMiddleExtended &&
                   isRingExtended && isPinkyExtended
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida si la seña corresponde a "Amigo"
     * Características SIMPLIFICADAS: Una mano arriba y otra abajo, cerca una de la otra
     */
    fun validateSignAmigo(handResult: HandLandmarkerResult): SignAmigoValidationResult {
        // Verificar que se detecten exactamente DOS manos
        if (handResult.landmarks().size != 2) {
            return SignAmigoValidationResult(
                false,
                if (handResult.landmarks().isEmpty()) {
                    "Muestra ambas manos a la cámara"
                } else {
                    "Muestra AMBAS manos para la seña de 'Amigo'"
                },
                0f
            )
        }

        try {
            val hand1 = handResult.landmarks()[0]
            val hand2 = handResult.landmarks()[1]

            // Obtener las muñecas de ambas manos
            val wrist1 = hand1[0]
            val wrist2 = hand2[0]

            // 1. SOLO verificar que una mano esté arriba y otra abajo (diferencia en Y)
            val verticalDistance = abs(wrist1.y() - wrist2.y())
            val oneAboveOther = verticalDistance > 0.03f // Mínima diferencia de altura

            if (!oneAboveOther) {
                return SignAmigoValidationResult(
                    false,
                    "Coloca una mano arriba y otra abajo",
                    0.3f
                )
            }

            // 2. SOLO verificar que las manos estén CERCA horizontalmente (aproximadamente alineadas)
            val horizontalDistance = abs(wrist1.x() - wrist2.x())
            val areClose = horizontalDistance < 0.25f // MUY permisivo

            if (!areClose) {
                return SignAmigoValidationResult(
                    false,
                    "Acerca más las manos",
                    0.6f
                )
            }

            // ¡Eso es todo! Si llegó aquí, la seña es válida
            return SignAmigoValidationResult(
                true,
                "¡Perfecto! Seña de 'Amigo' completada",
                1.0f
            )

        } catch (e: Exception) {
            return SignAmigoValidationResult(
                false,
                "Error en la validación",
                0f
            )
        }
    }

    data class SignAmigoValidationResult(
        val isValid: Boolean,
        val message: String,
        val confidence: Float
    )

    // Variables para rastrear el movimiento de "Hola"
    private val holaTrajectoryPoints = mutableListOf<TrajectoryPoint>()
    private var holaDetectionStarted = false

    /**
     * Valida si la seña corresponde a "Hola"
     * Características: Mano con todos los dedos extendidos, movimiento lateral de izquierda a derecha
     */
    fun validateSignHola(handResult: HandLandmarkerResult): SignHolaValidationResult {
        if (handResult.landmarks().isEmpty()) {
            resetHolaTrajectory()
            return SignHolaValidationResult(false, "Muestra tu mano a la cámara", 0f)
        }

        val landmarks = handResult.landmarks()[0]

        try {
            // 1. Verificar que todos los dedos estén extendidos (mano abierta)
            val isThumbExtended = isThumbExtended(landmarks)
            val isIndexExtended = isFingerExtended(landmarks, FingerType.INDEX)
            val isMiddleExtended = isFingerExtended(landmarks, FingerType.MIDDLE)
            val isRingExtended = isFingerExtended(landmarks, FingerType.RING)
            val isPinkyExtended = isFingerExtended(landmarks, FingerType.PINKY)

            val allFingersExtended = isThumbExtended && isIndexExtended &&
                                    isMiddleExtended && isRingExtended && isPinkyExtended

            if (!allFingersExtended) {
                resetHolaTrajectory()
                return SignHolaValidationResult(
                    false,
                    "Extiende todos los dedos (mano abierta)",
                    0f
                )
            }

            // 2. Rastrear el movimiento de la mano (usamos la muñeca)
            val wrist = landmarks[0] // Muñeca
            val currentTime = System.currentTimeMillis()

            // Añadir punto a la trayectoria
            holaTrajectoryPoints.add(TrajectoryPoint(wrist.x(), wrist.y(), currentTime))

            // Limpiar puntos antiguos (más de 2 segundos)
            holaTrajectoryPoints.removeAll { currentTime - it.timestamp > 2000 }

            // 3. Verificar si hay suficientes puntos para validar el movimiento
            if (holaTrajectoryPoints.size >= MIN_TRAJECTORY_POINTS) {
                val holaResult = detectLateralMovement(holaTrajectoryPoints)

                if (holaResult.isLateral) {
                    return SignHolaValidationResult(
                        true,
                        "¡Perfecto! Seña de 'Hola' completada",
                        holaResult.confidence
                    )
                }

                return SignHolaValidationResult(
                    false,
                    "Mueve la mano de izquierda a derecha",
                    holaResult.confidence
                )
            }

            return SignHolaValidationResult(
                false,
                "Mantén la mano abierta y muévela lateralmente",
                0.3f
            )

        } catch (e: Exception) {
            resetHolaTrajectory()
            return SignHolaValidationResult(false, "Error en validación", 0f)
        }
    }

    /**
     * Detecta si el conjunto de puntos representa un movimiento lateral
     * (de izquierda a derecha o derecha a izquierda)
     */
    private fun detectLateralMovement(points: List<TrajectoryPoint>): LateralMovementResult {
        if (points.size < MIN_TRAJECTORY_POINTS) {
            return LateralMovementResult(false, 0f)
        }

        val startPoint = points.first()
        val endPoint = points.last()

        // Calcular desplazamiento horizontal y vertical
        val horizontalMovement = abs(endPoint.x - startPoint.x)
        val verticalMovement = abs(endPoint.y - startPoint.y)

        // El movimiento debe ser predominantemente horizontal
        val isLateral = horizontalMovement > verticalMovement && horizontalMovement > 0.15f

        // Calcular confianza basada en qué tan horizontal es el movimiento
        var confidence = 0f
        if (isLateral) {
            val ratio = horizontalMovement / (verticalMovement + 0.01f) // Evitar división por 0
            confidence = min(1.0f, ratio / 3.0f) // Normalizar
        } else {
            // Dar confianza parcial si hay algo de movimiento horizontal
            confidence = min(0.6f, horizontalMovement / 0.15f)
        }

        return LateralMovementResult(isLateral, confidence)
    }

    /**
     * Resetea el rastreo de trayectoria para la seña "Hola"
     */
    fun resetHolaTrajectory() {
        holaTrajectoryPoints.clear()
        holaDetectionStarted = false
    }

    data class SignHolaValidationResult(
        val isValid: Boolean,
        val message: String,
        val confidence: Float
    )

    private data class LateralMovementResult(
        val isLateral: Boolean,
        val confidence: Float
    )

    // Clases de datos para resultados
    data class LetterZValidationResult(
        val isValid: Boolean,
        val message: String,
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
