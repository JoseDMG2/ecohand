package com.example.ecohand.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

/**
 * Overlay para visualizar detecciones de MediaPipe
 * Dibuja líneas verdes conectando landmarks y puntos verdes en vértices
 */
@Composable
fun DetectionOverlay(
    handResults: HandLandmarkerResult?,
    faceResults: FaceLandmarkerResult?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Dibujar manos
        handResults?.let { results ->
            results.landmarks().forEachIndexed { handIndex, landmarks ->
                drawHandLandmarks(
                    landmarks = landmarks.map { landmark ->
                        Offset(
                            x = landmark.x() * canvasWidth,
                            y = landmark.y() * canvasHeight
                        )
                    },
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }
        }

        // Dibujar rostro
        faceResults?.let { results ->
            results.faceLandmarks().forEach { landmarks ->
                drawFaceLandmarks(
                    landmarks = landmarks.map { landmark ->
                        Offset(
                            x = landmark.x() * canvasWidth,
                            y = landmark.y() * canvasHeight
                        )
                    },
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }
        }
    }
}

/**
 * Dibuja landmarks de la mano con conexiones
 */
private fun DrawScope.drawHandLandmarks(
    landmarks: List<Offset>,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (landmarks.size != 21) return

    val lineColor = Color(0xFF00FF00) // Verde brillante
    val pointColor = Color(0xFF00FF00)
    val lineWidth = 3f
    val pointRadius = 6f

    // Conexiones de la mano (según MediaPipe Hand Landmarker)
    val connections = listOf(
        // Muñeca a palma
        0 to 1, 0 to 5, 0 to 9, 0 to 13, 0 to 17,
        // Pulgar
        1 to 2, 2 to 3, 3 to 4,
        // Índice
        5 to 6, 6 to 7, 7 to 8,
        // Medio
        9 to 10, 10 to 11, 11 to 12,
        // Anular
        13 to 14, 14 to 15, 15 to 16,
        // Meñique
        17 to 18, 18 to 19, 19 to 20,
        // Palma
        5 to 9, 9 to 13, 13 to 17
    )

    // Dibujar líneas de conexión
    connections.forEach { (start, end) ->
        if (start < landmarks.size && end < landmarks.size) {
            drawLine(
                color = lineColor,
                start = landmarks[start],
                end = landmarks[end],
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }
    }

    // Dibujar puntos de landmarks
    landmarks.forEach { point ->
        if (point.x >= 0 && point.x <= canvasWidth &&
            point.y >= 0 && point.y <= canvasHeight) {
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = point
            )
        }
    }
}

/**
 * Dibuja landmarks del rostro (versión simplificada)
 * Dibuja contorno facial, ojos, cejas, nariz y boca
 */
private fun DrawScope.drawFaceLandmarks(
    landmarks: List<Offset>,
    canvasWidth: Float,
    canvasHeight: Float
) {
    if (landmarks.isEmpty()) return

    val lineColor = Color(0xFF00FF00) // Verde brillante
    val pointColor = Color(0xFF00FF00)
    val lineWidth = 2f
    val pointRadius = 3f

    // Conexiones principales del rostro (simplificado para visualización clara)
    // MediaPipe Face Landmarker tiene 468 puntos, aquí mostramos los principales
    val faceOvalConnections = listOf(
        // Contorno facial (10 a 338, pasando por varios puntos clave)
        10 to 338, 338 to 297, 297 to 332, 332 to 284,
        284 to 251, 251 to 389, 389 to 356, 356 to 454,
        454 to 323, 323 to 361, 361 to 288, 288 to 397,
        397 to 365, 365 to 379, 379 to 378, 378 to 400,
        400 to 377, 377 to 152, 152 to 148, 148 to 176,
        176 to 149, 149 to 150, 150 to 136, 136 to 172,
        172 to 58, 58 to 132, 132 to 93, 93 to 234,
        234 to 127, 127 to 162, 162 to 21, 21 to 54,
        54 to 103, 103 to 67, 67 to 109, 109 to 10
    )

    // Ojo izquierdo
    val leftEyeConnections = listOf(
        33 to 7, 7 to 163, 163 to 144, 144 to 145,
        145 to 153, 153 to 154, 154 to 155, 155 to 133,
        133 to 173, 173 to 157, 157 to 158, 158 to 159,
        159 to 160, 160 to 161, 161 to 246, 246 to 33
    )

    // Ojo derecho
    val rightEyeConnections = listOf(
        263 to 249, 249 to 390, 390 to 373, 373 to 374,
        374 to 380, 380 to 381, 381 to 382, 382 to 362,
        362 to 398, 398 to 384, 384 to 385, 385 to 386,
        386 to 387, 387 to 388, 388 to 466, 466 to 263
    )

    // Labios (exterior)
    val lipsConnections = listOf(
        61 to 146, 146 to 91, 91 to 181, 181 to 84,
        84 to 17, 17 to 314, 314 to 405, 405 to 321,
        321 to 375, 375 to 291, 291 to 409, 409 to 270,
        270 to 269, 269 to 267, 267 to 0, 0 to 37,
        37 to 39, 39 to 40, 40 to 185, 185 to 61
    )

    val allConnections = faceOvalConnections + leftEyeConnections + rightEyeConnections + lipsConnections

    // Dibujar líneas de conexión
    allConnections.forEach { (start, end) ->
        if (start < landmarks.size && end < landmarks.size) {
            val startPoint = landmarks[start]
            val endPoint = landmarks[end]

            if (isPointInBounds(startPoint, canvasWidth, canvasHeight) &&
                isPointInBounds(endPoint, canvasWidth, canvasHeight)) {
                drawLine(
                    color = lineColor,
                    start = startPoint,
                    end = endPoint,
                    strokeWidth = lineWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // Dibujar solo puntos clave (no los 468 para no saturar)
    val keyPoints = setOf(
        10, 338, 297, 332, 284, 251, 389, 454, 323, 361, 288, 397,
        152, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162,
        33, 133, 263, 362, // Ojos
        61, 291, 0, 17 // Boca
    )

    keyPoints.forEach { index ->
        if (index < landmarks.size) {
            val point = landmarks[index]
            if (isPointInBounds(point, canvasWidth, canvasHeight)) {
                drawCircle(
                    color = pointColor,
                    radius = pointRadius,
                    center = point
                )
            }
        }
    }
}

/**
 * Verifica si un punto está dentro de los límites del canvas
 */
private fun isPointInBounds(point: Offset, width: Float, height: Float): Boolean {
    return point.x >= 0 && point.x <= width && point.y >= 0 && point.y <= height
}

