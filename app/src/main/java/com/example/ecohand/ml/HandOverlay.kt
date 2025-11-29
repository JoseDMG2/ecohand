package com.example.ecohand.ml

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Composable that draws hand landmarks and skeleton on top of camera preview.
 */
@Composable
fun HandOverlay(
    detectionResult: HandDetectionResult?,
    modifier: Modifier = Modifier,
    landmarkColor: Color = Color.Green,
    connectionColor: Color = Color.Cyan,
    landmarkRadius: Float = 8f,
    connectionWidth: Float = 4f,
    showConnections: Boolean = true,
    isMirrored: Boolean = true // For front camera
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        detectionResult?.hands?.forEach { hand ->
            drawHandLandmarks(
                hand = hand,
                landmarkColor = landmarkColor,
                connectionColor = connectionColor,
                landmarkRadius = landmarkRadius,
                connectionWidth = connectionWidth,
                showConnections = showConnections,
                isMirrored = isMirrored
            )
        }
    }
}

/**
 * Draw landmarks and connections for a single hand.
 */
private fun DrawScope.drawHandLandmarks(
    hand: HandLandmarks,
    landmarkColor: Color,
    connectionColor: Color,
    landmarkRadius: Float,
    connectionWidth: Float,
    showConnections: Boolean,
    isMirrored: Boolean
) {
    val points = hand.landmarks
    if (points.size < HandLandmarks.NUM_LANDMARKS) return
    
    // Convert normalized coordinates to canvas coordinates
    val canvasPoints = points.map { point ->
        val x = if (isMirrored) {
            (1f - point.x) * size.width
        } else {
            point.x * size.width
        }
        val y = point.y * size.height
        Offset(x, y)
    }
    
    // Draw connections first (so landmarks appear on top)
    if (showConnections) {
        HAND_CONNECTIONS.forEach { (start, end) ->
            if (start < canvasPoints.size && end < canvasPoints.size) {
                drawLine(
                    color = connectionColor,
                    start = canvasPoints[start],
                    end = canvasPoints[end],
                    strokeWidth = connectionWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
    
    // Draw landmarks
    canvasPoints.forEachIndexed { index, point ->
        // Use different colors for fingertips
        val color = when (index) {
            HandLandmarks.THUMB_TIP,
            HandLandmarks.INDEX_FINGER_TIP,
            HandLandmarks.MIDDLE_FINGER_TIP,
            HandLandmarks.RING_FINGER_TIP,
            HandLandmarks.PINKY_TIP -> Color.Red
            HandLandmarks.WRIST -> Color.Yellow
            else -> landmarkColor
        }
        
        drawCircle(
            color = color,
            radius = landmarkRadius,
            center = point
        )
    }
}

/**
 * Hand landmark connections for drawing the skeleton.
 * Each pair represents (start_landmark_index, end_landmark_index)
 */
private val HAND_CONNECTIONS = listOf(
    // Thumb
    Pair(HandLandmarks.WRIST, HandLandmarks.THUMB_CMC),
    Pair(HandLandmarks.THUMB_CMC, HandLandmarks.THUMB_MCP),
    Pair(HandLandmarks.THUMB_MCP, HandLandmarks.THUMB_IP),
    Pair(HandLandmarks.THUMB_IP, HandLandmarks.THUMB_TIP),
    
    // Index finger
    Pair(HandLandmarks.WRIST, HandLandmarks.INDEX_FINGER_MCP),
    Pair(HandLandmarks.INDEX_FINGER_MCP, HandLandmarks.INDEX_FINGER_PIP),
    Pair(HandLandmarks.INDEX_FINGER_PIP, HandLandmarks.INDEX_FINGER_DIP),
    Pair(HandLandmarks.INDEX_FINGER_DIP, HandLandmarks.INDEX_FINGER_TIP),
    
    // Middle finger
    Pair(HandLandmarks.WRIST, HandLandmarks.MIDDLE_FINGER_MCP),
    Pair(HandLandmarks.MIDDLE_FINGER_MCP, HandLandmarks.MIDDLE_FINGER_PIP),
    Pair(HandLandmarks.MIDDLE_FINGER_PIP, HandLandmarks.MIDDLE_FINGER_DIP),
    Pair(HandLandmarks.MIDDLE_FINGER_DIP, HandLandmarks.MIDDLE_FINGER_TIP),
    
    // Ring finger
    Pair(HandLandmarks.WRIST, HandLandmarks.RING_FINGER_MCP),
    Pair(HandLandmarks.RING_FINGER_MCP, HandLandmarks.RING_FINGER_PIP),
    Pair(HandLandmarks.RING_FINGER_PIP, HandLandmarks.RING_FINGER_DIP),
    Pair(HandLandmarks.RING_FINGER_DIP, HandLandmarks.RING_FINGER_TIP),
    
    // Pinky
    Pair(HandLandmarks.WRIST, HandLandmarks.PINKY_MCP),
    Pair(HandLandmarks.PINKY_MCP, HandLandmarks.PINKY_PIP),
    Pair(HandLandmarks.PINKY_PIP, HandLandmarks.PINKY_DIP),
    Pair(HandLandmarks.PINKY_DIP, HandLandmarks.PINKY_TIP),
    
    // Palm connections
    Pair(HandLandmarks.INDEX_FINGER_MCP, HandLandmarks.MIDDLE_FINGER_MCP),
    Pair(HandLandmarks.MIDDLE_FINGER_MCP, HandLandmarks.RING_FINGER_MCP),
    Pair(HandLandmarks.RING_FINGER_MCP, HandLandmarks.PINKY_MCP)
)

/**
 * Color scheme based on handedness
 */
fun getHandColor(handedness: Handedness): Color {
    return when (handedness) {
        Handedness.LEFT -> Color(0xFF4CAF50)  // Green
        Handedness.RIGHT -> Color(0xFF2196F3) // Blue
        Handedness.UNKNOWN -> Color(0xFFFF9800) // Orange
    }
}
