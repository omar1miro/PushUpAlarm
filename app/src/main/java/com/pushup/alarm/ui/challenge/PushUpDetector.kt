package com.pushup.alarm.ui.challenge

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

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
    private val cooldownMs = 1500L
    private val minConfidence = 0.5f

    fun processPose(pose: Pose, timestampMs: Long) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null
        ) return

        if (leftShoulder.inFrameLikelihood < minConfidence ||
            rightShoulder.inFrameLikelihood < minConfidence ||
            leftHip.inFrameLikelihood < minConfidence ||
            rightHip.inFrameLikelihood < minConfidence
        ) return

        val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2f
        val hipY = (leftHip.position.y + rightHip.position.y) / 2f

        val verticalDistance = shoulderY - hipY

        when (state) {
            PushUpState.UP -> {
                if (verticalDistance < DOWN_THRESHOLD) {
                    state = PushUpState.DOWN
                    onStateChanged(PushUpState.DOWN)
                }
            }
            PushUpState.DOWN -> {
                if (verticalDistance > UP_THRESHOLD) {
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

    fun reset() {
        state = PushUpState.UP
        lastRepTime = 0L
    }

    companion object {
        private const val DOWN_THRESHOLD = -20f
        private const val UP_THRESHOLD = 10f
    }
}
