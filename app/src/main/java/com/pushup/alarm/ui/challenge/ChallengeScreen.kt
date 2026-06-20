package com.pushup.alarm.ui.challenge

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pushup.alarm.ui.theme.AccentOrange
import com.pushup.alarm.ui.theme.AlarmRed
import com.pushup.alarm.ui.theme.DarkBackground
import com.pushup.alarm.ui.theme.SuccessGreen
import java.util.concurrent.Executors

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
    onChallengeComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            // Fallback to math will be triggered by ViewModel observing permission
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !state.useMathFallback) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onChallengeComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (state.useMathFallback) {
            MathChallengeContent(
                viewModel = viewModel,
                state = state
            )
        } else {
            CameraChallengeContent(
                viewModel = viewModel,
                state = state,
                lifecycleOwner = lifecycleOwner,
                hasCameraPermission = hasCameraPermission,
                onPermissionRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }
    }
}

@Composable
fun CameraChallengeContent(
    viewModel: ChallengeViewModel,
    state: ChallengeUiState,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    hasCameraPermission: Boolean,
    onPermissionRequest: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = state.currentCount.toFloat() / state.targetCount.toFloat(),
        animationSpec = tween(300),
        label = "progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = androidx.camera.core.Preview.Builder()
                            .build()
                            .also { it.surfaceProvider = previewView.surfaceProvider }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    viewModel.imageAnalyzer.analyze(imageProxy)
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AlarmRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Camera permission required",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onPermissionRequest) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Counter overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.label.isNotBlank()) {
                Text(
                    state.label,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                "${state.currentCount} / ${state.targetCount}",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(80.dp),
                color = if (state.isComplete) SuccessGreen else AccentOrange,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
        }

        // State indicator at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (state.pushUpState) {
                            PushUpState.UP -> SuccessGreen.copy(alpha = 0.3f)
                            PushUpState.DOWN -> AccentOrange.copy(alpha = 0.3f)
                        }
                    )
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (state.pushUpState) {
                        PushUpState.UP -> "UP"
                        PushUpState.DOWN -> "DOWN"
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MathChallengeContent(
    viewModel: ChallengeViewModel,
    state: ChallengeUiState
) {
    var answer by remember { mutableStateOf("") }
    val problem = viewModel.mathChallenge.getCurrentProblem()
    val progress by animateFloatAsState(
        targetValue = state.currentCount.toFloat() / viewModel.mathChallenge.getProgress().second.toFloat(),
        animationSpec = tween(300),
        label = "mathProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AccentOrange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No Camera Available",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Solve these math problems to dismiss the alarm",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(60.dp),
            color = AccentOrange,
            trackColor = Color.White.copy(alpha = 0.2f),
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (problem != null) {
            Text(
                problem.question,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.White
                ),
                placeholder = {
                    Text(
                        "Your answer",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.checkMathAnswer(answer)
                    answer = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Problem ${state.currentCount + 1} of ${viewModel.mathChallenge.getProgress().second}",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp
        )
    }
}
