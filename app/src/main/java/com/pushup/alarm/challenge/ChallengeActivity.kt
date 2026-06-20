package com.pushup.alarm.challenge

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pushup.alarm.ui.challenge.ChallengeScreen
import com.pushup.alarm.ui.challenge.ChallengeViewModel
import com.pushup.alarm.ui.theme.PushUpAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengeActivity : ComponentActivity() {

    private val viewModel: ChallengeViewModel by viewModels()
    private var isLocked = false
    private var ringtone: android.media.Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemBars()
        keepScreenOn()
        startAlarmSound()

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val pushUpCount = intent.getIntExtra(EXTRA_PUSH_UP_COUNT, 20)
        val label = intent.getStringExtra(EXTRA_LABEL) ?: ""

        viewModel.initialize(alarmId, pushUpCount, label)

        setContent {
            PushUpAlarmTheme {
                ChallengeScreen(
                    viewModel = viewModel,
                    onChallengeComplete = { finishChallenge() }
                )
            }
        }

        startLockTaskMode()
    }

    private fun startAlarmSound() {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone?.isLooping = true
        ringtone?.play()

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopAlarmSound() {
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onResume() {
        super.onResume()
        if (isLocked) {
            startLockTaskMode()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isLocked) {
            val intent = Intent(this, ChallengeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Do nothing - prevent exiting
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isLocked) {
            val intent = Intent(this, ChallengeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
            startActivity(intent)
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startLockTaskMode() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, LockTaskAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(packageName) || dpm.isProfileOwnerApp(packageName)) {
            try {
                startLockTask()
                isLocked = true
            } catch (_: SecurityException) {
                isLocked = false
            }
        } else {
            isLocked = false
        }
    }

    private fun finishChallenge() {
        stopAlarmSound()
        if (isLocked) {
            try {
                stopLockTask()
            } catch (_: Exception) {}
        }
        finish()
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_PUSH_UP_COUNT = "push_up_count"
        const val EXTRA_LABEL = "label"
    }
}
