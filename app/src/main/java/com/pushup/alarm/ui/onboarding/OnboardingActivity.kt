package com.pushup.alarm.ui.onboarding

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushup.alarm.MainActivity
import com.pushup.alarm.challenge.LockTaskAdminReceiver
import com.pushup.alarm.ui.theme.AccentOrange
import com.pushup.alarm.ui.theme.DarkBackground
import com.pushup.alarm.ui.theme.PushUpAlarmTheme
import com.pushup.alarm.ui.theme.SuccessGreen

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PushUpAlarmTheme {
                OnboardingScreen(
                    onRequestAdmin = { requestDeviceAdmin() },
                    onSkip = { goToMain() },
                    onContinue = { goToMain() }
                )
            }
        }
    }

    private fun requestDeviceAdmin() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, LockTaskAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(packageName)) {
            goToMain()
            return
        }

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Device admin permission is needed to prevent you from skipping the push-up challenge. " +
                        "This ensures you complete your exercises when the alarm goes off."
            )
        }
        startActivityForResult(intent, REQUEST_ADMIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADMIN) {
            goToMain()
        }
    }

    private fun goToMain() {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_complete", true)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val REQUEST_ADMIN = 1001
    }
}

@Composable
fun OnboardingScreen(
    onRequestAdmin: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = AccentOrange
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Push-Up Alarm",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Wake up and exercise!\nAlarms can only be dismissed by completing push-ups.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SuccessGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Device Admin Permission",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "To prevent you from skipping the challenge, the app needs device admin permission. " +
                    "This enables lock task mode during push-ups.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestAdmin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                "Enable Device Admin",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Skip for now",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}
