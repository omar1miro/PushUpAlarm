package com.pushup.alarm.ui.challenge

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.pushup.alarm.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeUiState(
    val currentCount: Int = 0,
    val targetCount: Int = 20,
    val isComplete: Boolean = false,
    val useMathFallback: Boolean = false,
    val pushUpState: PushUpState = PushUpState.UP,
    val lowLightWarning: Boolean = false,
    val label: String = "",
    val alarmId: Long = -1L
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val application: Application,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengeUiState())
    val state: StateFlow<ChallengeUiState> = _state.asStateFlow()

    private val pushUpDetector = PushUpDetector(
        onRepCounted = { onRepCounted() },
        onStateChanged = { newState ->
            _state.value = _state.value.copy(pushUpState = newState)
        }
    )

    val mathChallenge = MathChallengeFallback()

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        processImage(imageProxy)
    }

    fun initialize(alarmId: Long, targetCount: Int, label: String) {
        _state.value = ChallengeUiState(
            targetCount = targetCount,
            label = label,
            alarmId = alarmId
        )

        val hasCamera = application.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY
        )
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            application,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCamera || !hasPermission) {
            _state.value = _state.value.copy(useMathFallback = true)
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                pushUpDetector.processPose(pose, System.currentTimeMillis())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun onRepCounted() {
        val newCount = _state.value.currentCount + 1
        vibrate()

        if (newCount >= _state.value.targetCount) {
            _state.value = _state.value.copy(
                currentCount = newCount,
                isComplete = true
            )
            completeChallenge()
        } else {
            _state.value = _state.value.copy(currentCount = newCount)
        }
    }

    fun checkMathAnswer(answer: String) {
        val num = answer.toIntOrNull() ?: return
        if (mathChallenge.checkAnswer(num)) {
            if (mathChallenge.isComplete()) {
                _state.value = _state.value.copy(
                    currentCount = _state.value.targetCount,
                    isComplete = true
                )
                completeChallenge()
            } else {
                val (current, total) = mathChallenge.getProgress()
                _state.value = _state.value.copy(currentCount = current)
            }
        }
    }

    private fun completeChallenge() {
        viewModelScope.launch {
            statsRepository.recordAlarmCompleted(_state.value.targetCount)
        }
    }

    private fun vibrate() {
        val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    override fun onCleared() {
        super.onCleared()
        poseDetector.close()
    }
}
