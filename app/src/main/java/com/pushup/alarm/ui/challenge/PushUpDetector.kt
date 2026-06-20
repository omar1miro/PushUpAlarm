package com.pushup.alarm.ui.challenge

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.sqrt

enum class PushUpState {
    UP,
    DOWN
}

class PushUpDetector(
    private val onRepCounted: () -> Unit,
    private val onStateChanged: (PushUpState) -> Unit
) {
    private var state = PushUpState.UP
    private var lastRepTime = 0L
    private val cooldownMs = 1200L
    private val minConfidence = 0.5f

    fun processPose(pose: Pose, timestampMs: Long) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (leftShoulder == null || rightShoulder == null ||
            leftElbow == null || rightElbow == null ||
            leftWrist == null || rightWrist == null ||
            leftHip == null || rightHip == null
        ) return

        if (leftShoulder.inFrameLikelihood < minConfidence ||
            rightShoulder.inFrameLikelihood < minConfidence ||
            leftElbow.inFrameLikelihood < minConfidence ||
            rightElbow.inFrameLikelihood < minConfidence
        ) return

        val leftAngle = calculateAngle(
            leftShoulder.position.x, leftShoulder.position.y,
            leftElbow.position.x, leftElbow.position.y,
            leftWrist.position.x, leftWrist.position.y
        )
        val rightAngle = calculateAngle(
            rightShoulder.position.x, rightShoulder.position.y,
            rightElbow.position.x, rightElbow.position.y,
            rightWrist.position.x, rightWrist.position.y
        )

        val avgAngle = (leftAngle + rightAngle) / 2f

        val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2f
        val hipY = (leftHip.position.y + rightHip.position.y) / 2f
        val bodyTilt = shoulderY - hipY

        when (state) {
            PushUpState.UP -> {
                if (avgAngle < DOWN_ANGLE_THRESHOLD || bodyTilt < DOWN_TILT_THRESHOLD) {
                    state = PushUpState.DOWN
                    onStateChanged(PushUpState.DOWN)
                }
            }
            PushUpState.DOWN -> {
                if (avgAngle > UP_ANGLE_THRESHOLD && bodyTilt > UP_TILT_THRESHOLD) {
                    if (timestampMs - lastRepTime >= cooldownMs) {
                        state = PushUpState.UP
                        lastRepTime = timestampMs
                        onStateChanged(PushUpState.UP)
                        onRepCounted()
                    }
                }
            }
        }
    }

    private fun calculateAngle(
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        x3: Float, y3: Float
    ): Float {
        val v1x = x1 - x2
        val v1y = y1 - y2
        val v2x = x3 - x2
        val v2y = y3 - y2

        val dot = v1x * v2x + v1y * v2y
        val mag1 = sqrt(v1x * v1x + v1y * v1y)
        val mag2 = sqrt(v2x * v2x + v2y * v2y)

        if (mag1 == 0f || mag2 == 0f) return 180f

        val cosAngle = (dot / (mag1 * mag2)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosAngle.toDouble())).toFloat()
    }

    fun reset() {
        state = PushUpState.UP
        lastRepTime = 0L
    }

    companion object {
        private const val DOWN_ANGLE_THRESHOLD = 100f
        private const val UP_ANGLE_THRESHOLD = 140f
        private const val DOWN_TILT_THRESHOLD = -30f
        private const val UP_TILT_THRESHOLD = 0f
    }
}
