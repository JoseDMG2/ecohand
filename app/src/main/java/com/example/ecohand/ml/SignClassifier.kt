package com.example.ecohand.ml

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * SignClassifier uses geometric rules based on hand landmarks to classify
 * Peruvian sign language signs. This approach doesn't require a separate
 * ML model and works by analyzing relative positions and angles of fingers.
 */
class SignClassifier {
    
    companion object {
        private const val TAG = "SignClassifier"
        
        // Threshold for considering a finger as extended
        private const val FINGER_EXTENDED_THRESHOLD = 0.15f
        
        // Threshold for considering fingers as touching
        private const val TOUCH_DISTANCE_THRESHOLD = 0.08f
    }
    
    /**
     * Classify a sign based on hand landmarks.
     * 
     * @param landmarks The detected hand landmarks
     * @return The name of the recognized sign or null if no match
     */
    fun classifySign(landmarks: HandLandmarks): Pair<String?, Float> {
        val fingerStates = analyzeFingerStates(landmarks)
        
        // Check for known signs based on finger states
        return when {
            isHolaSign(fingerStates, landmarks) -> "hola" to 0.85f
            isGraciasSign(fingerStates, landmarks) -> "gracias" to 0.80f
            isAmorSign(fingerStates, landmarks) -> "amor" to 0.80f
            isMamaSign(fingerStates, landmarks) -> "mama" to 0.75f
            else -> null to 0f
        }
    }
    
    /**
     * Compare detected landmarks with reference landmarks for a specific sign.
     * 
     * @param detected The detected landmarks
     * @param reference The reference landmarks for the expected sign
     * @return Similarity score between 0 and 1
     */
    fun compareLandmarks(detected: HandLandmarks, reference: HandLandmarks): Float {
        if (detected.landmarks.size != reference.landmarks.size) {
            return 0f
        }
        
        // Normalize landmarks to make comparison position-invariant
        val normalizedDetected = normalizeLandmarks(detected.landmarks)
        val normalizedReference = normalizeLandmarks(reference.landmarks)
        
        // Calculate average distance between corresponding landmarks
        var totalDistance = 0f
        for (i in normalizedDetected.indices) {
            val d = normalizedDetected[i]
            val r = normalizedReference[i]
            val distance = sqrt(
                (d.x - r.x).pow(2) + 
                (d.y - r.y).pow(2) + 
                (d.z - r.z).pow(2)
            )
            totalDistance += distance
        }
        
        val averageDistance = totalDistance / normalizedDetected.size
        
        // Convert distance to similarity score (0-1)
        // Lower distance = higher similarity
        val similarity = maxOf(0f, 1f - (averageDistance * 2f))
        
        return similarity
    }
    
    /**
     * Analyze finger states (extended, bent, touching, etc.)
     */
    private fun analyzeFingerStates(landmarks: HandLandmarks): FingerStates {
        val points = landmarks.landmarks
        if (points.size < HandLandmarks.NUM_LANDMARKS) {
            return FingerStates()
        }
        
        val wrist = points[HandLandmarks.WRIST]
        
        // Check if each finger is extended
        val thumbExtended = isFingerExtended(
            points[HandLandmarks.THUMB_CMC],
            points[HandLandmarks.THUMB_MCP],
            points[HandLandmarks.THUMB_IP],
            points[HandLandmarks.THUMB_TIP],
            isThumb = true
        )
        
        val indexExtended = isFingerExtended(
            points[HandLandmarks.INDEX_FINGER_MCP],
            points[HandLandmarks.INDEX_FINGER_PIP],
            points[HandLandmarks.INDEX_FINGER_DIP],
            points[HandLandmarks.INDEX_FINGER_TIP]
        )
        
        val middleExtended = isFingerExtended(
            points[HandLandmarks.MIDDLE_FINGER_MCP],
            points[HandLandmarks.MIDDLE_FINGER_PIP],
            points[HandLandmarks.MIDDLE_FINGER_DIP],
            points[HandLandmarks.MIDDLE_FINGER_TIP]
        )
        
        val ringExtended = isFingerExtended(
            points[HandLandmarks.RING_FINGER_MCP],
            points[HandLandmarks.RING_FINGER_PIP],
            points[HandLandmarks.RING_FINGER_DIP],
            points[HandLandmarks.RING_FINGER_TIP]
        )
        
        val pinkyExtended = isFingerExtended(
            points[HandLandmarks.PINKY_MCP],
            points[HandLandmarks.PINKY_PIP],
            points[HandLandmarks.PINKY_DIP],
            points[HandLandmarks.PINKY_TIP]
        )
        
        // Check for touches
        val thumbIndexTouch = arePointsTouching(
            points[HandLandmarks.THUMB_TIP],
            points[HandLandmarks.INDEX_FINGER_TIP]
        )
        
        val thumbMiddleTouch = arePointsTouching(
            points[HandLandmarks.THUMB_TIP],
            points[HandLandmarks.MIDDLE_FINGER_TIP]
        )
        
        return FingerStates(
            thumbExtended = thumbExtended,
            indexExtended = indexExtended,
            middleExtended = middleExtended,
            ringExtended = ringExtended,
            pinkyExtended = pinkyExtended,
            thumbIndexTouching = thumbIndexTouch,
            thumbMiddleTouching = thumbMiddleTouch
        )
    }
    
    /**
     * Check if a finger is extended based on landmark positions.
     */
    private fun isFingerExtended(
        mcp: HandLandmarkPoint,
        pip: HandLandmarkPoint,
        dip: HandLandmarkPoint,
        tip: HandLandmarkPoint,
        isThumb: Boolean = false
    ): Boolean {
        // For non-thumb fingers, check if tip is above (lower y) than pip
        // For thumb, we need to check horizontal extension
        return if (isThumb) {
            // Thumb is extended if tip is far from palm center horizontally
            val horizontalDist = abs(tip.x - mcp.x)
            horizontalDist > FINGER_EXTENDED_THRESHOLD
        } else {
            // Finger is extended if tip is higher than pip
            // In normalized coordinates, lower y = higher on screen
            tip.y < pip.y - FINGER_EXTENDED_THRESHOLD * 0.5f
        }
    }
    
    /**
     * Check if two landmark points are touching (close to each other).
     */
    private fun arePointsTouching(p1: HandLandmarkPoint, p2: HandLandmarkPoint): Boolean {
        val distance = sqrt(
            (p1.x - p2.x).pow(2) +
            (p1.y - p2.y).pow(2) +
            (p1.z - p2.z).pow(2)
        )
        return distance < TOUCH_DISTANCE_THRESHOLD
    }
    
    /**
     * Normalize landmarks to be centered at wrist with unit scale.
     */
    private fun normalizeLandmarks(landmarks: List<HandLandmarkPoint>): List<HandLandmarkPoint> {
        if (landmarks.isEmpty()) return landmarks
        
        val wrist = landmarks[0]
        
        // Find scale based on distance from wrist to middle finger mcp
        val middleMcp = landmarks.getOrNull(HandLandmarks.MIDDLE_FINGER_MCP) ?: return landmarks
        val scale = sqrt(
            (middleMcp.x - wrist.x).pow(2) +
            (middleMcp.y - wrist.y).pow(2)
        ).coerceAtLeast(0.001f)
        
        // Normalize each landmark
        return landmarks.map { point ->
            HandLandmarkPoint(
                x = (point.x - wrist.x) / scale,
                y = (point.y - wrist.y) / scale,
                z = point.z / scale
            )
        }
    }
    
    // Sign recognition functions based on finger states
    
    /**
     * "Hola" (Hello) - Open palm with all fingers extended, waving motion
     */
    private fun isHolaSign(states: FingerStates, landmarks: HandLandmarks): Boolean {
        return states.thumbExtended &&
               states.indexExtended &&
               states.middleExtended &&
               states.ringExtended &&
               states.pinkyExtended
    }
    
    /**
     * "Gracias" (Thank you) - Flat hand moving from chin downward
     */
    private fun isGraciasSign(states: FingerStates, landmarks: HandLandmarks): Boolean {
        // All fingers extended, palm facing forward
        val allExtended = states.indexExtended &&
                         states.middleExtended &&
                         states.ringExtended &&
                         states.pinkyExtended
        
        // Hand is relatively flat (all fingertips at similar y level)
        val tips = listOf(
            landmarks.landmarks.getOrNull(HandLandmarks.INDEX_FINGER_TIP),
            landmarks.landmarks.getOrNull(HandLandmarks.MIDDLE_FINGER_TIP),
            landmarks.landmarks.getOrNull(HandLandmarks.RING_FINGER_TIP),
            landmarks.landmarks.getOrNull(HandLandmarks.PINKY_TIP)
        ).filterNotNull()
        
        val yRange = if (tips.isNotEmpty()) {
            tips.maxOf { it.y } - tips.minOf { it.y }
        } else 0f
        
        return allExtended && yRange < 0.1f
    }
    
    /**
     * "Amor" (Love) - ILY sign (index, pinky, thumb extended)
     */
    private fun isAmorSign(states: FingerStates, landmarks: HandLandmarks): Boolean {
        return states.thumbExtended &&
               states.indexExtended &&
               !states.middleExtended &&
               !states.ringExtended &&
               states.pinkyExtended
    }
    
    /**
     * "Mama" (Mother) - Thumb on chin, open hand
     */
    private fun isMamaSign(states: FingerStates, landmarks: HandLandmarks): Boolean {
        // Open hand with all fingers extended
        return states.thumbExtended &&
               states.indexExtended &&
               states.middleExtended &&
               states.ringExtended &&
               states.pinkyExtended
    }
}

/**
 * Data class representing the state of each finger.
 */
data class FingerStates(
    val thumbExtended: Boolean = false,
    val indexExtended: Boolean = false,
    val middleExtended: Boolean = false,
    val ringExtended: Boolean = false,
    val pinkyExtended: Boolean = false,
    val thumbIndexTouching: Boolean = false,
    val thumbMiddleTouching: Boolean = false
) {
    val allExtended: Boolean
        get() = thumbExtended && indexExtended && middleExtended && ringExtended && pinkyExtended
    
    val allClosed: Boolean
        get() = !thumbExtended && !indexExtended && !middleExtended && !ringExtended && !pinkyExtended
    
    val extendedCount: Int
        get() = listOf(thumbExtended, indexExtended, middleExtended, ringExtended, pinkyExtended)
            .count { it }
}
