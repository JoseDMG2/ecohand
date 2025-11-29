package com.example.ecohand.ml

/**
 * Represents a single landmark point detected on a hand.
 * MediaPipe detects 21 landmarks per hand.
 * 
 * @param x X coordinate normalized to [0, 1] relative to image width
 * @param y Y coordinate normalized to [0, 1] relative to image height
 * @param z Z coordinate (depth) normalized to wrist depth
 */
data class HandLandmarkPoint(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Represents the complete set of 21 hand landmarks detected by MediaPipe.
 * 
 * Landmark indices:
 * 0 - WRIST
 * 1 - THUMB_CMC, 2 - THUMB_MCP, 3 - THUMB_IP, 4 - THUMB_TIP
 * 5 - INDEX_FINGER_MCP, 6 - INDEX_FINGER_PIP, 7 - INDEX_FINGER_DIP, 8 - INDEX_FINGER_TIP
 * 9 - MIDDLE_FINGER_MCP, 10 - MIDDLE_FINGER_PIP, 11 - MIDDLE_FINGER_DIP, 12 - MIDDLE_FINGER_TIP
 * 13 - RING_FINGER_MCP, 14 - RING_FINGER_PIP, 15 - RING_FINGER_DIP, 16 - RING_FINGER_TIP
 * 17 - PINKY_MCP, 18 - PINKY_PIP, 19 - PINKY_DIP, 20 - PINKY_TIP
 */
data class HandLandmarks(
    val landmarks: List<HandLandmarkPoint>,
    val handedness: Handedness,
    val confidence: Float
) {
    companion object {
        const val WRIST = 0
        const val THUMB_CMC = 1
        const val THUMB_MCP = 2
        const val THUMB_IP = 3
        const val THUMB_TIP = 4
        const val INDEX_FINGER_MCP = 5
        const val INDEX_FINGER_PIP = 6
        const val INDEX_FINGER_DIP = 7
        const val INDEX_FINGER_TIP = 8
        const val MIDDLE_FINGER_MCP = 9
        const val MIDDLE_FINGER_PIP = 10
        const val MIDDLE_FINGER_DIP = 11
        const val MIDDLE_FINGER_TIP = 12
        const val RING_FINGER_MCP = 13
        const val RING_FINGER_PIP = 14
        const val RING_FINGER_DIP = 15
        const val RING_FINGER_TIP = 16
        const val PINKY_MCP = 17
        const val PINKY_PIP = 18
        const val PINKY_DIP = 19
        const val PINKY_TIP = 20
        
        const val NUM_LANDMARKS = 21
    }
    
    fun getWrist() = landmarks.getOrNull(WRIST)
    fun getThumbTip() = landmarks.getOrNull(THUMB_TIP)
    fun getIndexTip() = landmarks.getOrNull(INDEX_FINGER_TIP)
    fun getMiddleTip() = landmarks.getOrNull(MIDDLE_FINGER_TIP)
    fun getRingTip() = landmarks.getOrNull(RING_FINGER_TIP)
    fun getPinkyTip() = landmarks.getOrNull(PINKY_TIP)
}

/**
 * Enumeration of hand types
 */
enum class Handedness {
    LEFT,
    RIGHT,
    UNKNOWN
}

/**
 * Result of hand detection containing all detected hands
 */
data class HandDetectionResult(
    val hands: List<HandLandmarks>,
    val inferenceTimeMs: Long,
    val imageWidth: Int,
    val imageHeight: Int
) {
    val hasHands: Boolean get() = hands.isNotEmpty()
    val handsCount: Int get() = hands.size
    
    fun getFirstHand(): HandLandmarks? = hands.firstOrNull()
}

/**
 * Result of sign recognition
 */
data class SignRecognitionResult(
    val signName: String?,
    val confidence: Float,
    val isCorrect: Boolean,
    val expectedSign: String,
    val landmarks: HandLandmarks?
)
