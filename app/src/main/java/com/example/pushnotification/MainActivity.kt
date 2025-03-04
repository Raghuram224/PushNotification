package com.example.pushnotification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.pushnotification.ui.theme.PushNotificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PushNotificationTheme {
//               Scaffold {
                   NotificationPermissionScreen()
//               }
            }
        }
    }
}




@Composable
fun NotificationPermissionScreen() {

    val context = LocalContext.current
    val notificationService = NotificationService(context = context)
    var showRationaleDialog by remember { mutableStateOf(false) }

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted
                Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
                // Proceed with showing notifications
            } else {
                // Permission denied
                Toast.makeText(context, "Notification permission denied!", Toast.LENGTH_SHORT).show()
                // Handle the lack of permission
            }
        }
    )

    // Check if permission is required (Android 13+)
    val isPermissionRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    // Check current permission status
    val permissionCheckResult = if (isPermissionRequired) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        PackageManager.PERMISSION_GRANTED
    }

    // Should show rationale dialog
    val shouldShowRationale = if (isPermissionRequired && context is ComponentActivity) {
        context.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Notification Permission Demo",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    !isPermissionRequired -> {
                        Toast.makeText(
                            context,
                            "Notifications are automatically enabled for this Android version",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    permissionCheckResult == PackageManager.PERMISSION_GRANTED -> {
                        Toast.makeText(
                            context,
                            "Notification permission already granted",
                            Toast.LENGTH_SHORT
                        ).show()
                        notificationService.showBasicNotification()
//                        notificationService.
                    }
                    shouldShowRationale -> {
                        showRationaleDialog = true
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        ) {
            Text("send notification")
        }
        Button(
            onClick = {
                notificationService.showExpandableNotification()
            }
        ) {
            Text("Send expandable notification")
        }
    }

    // Show rationale dialog if needed
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Notification Permission Required") },
            text = { Text("This app needs notification permission to alert you about important events.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Request Permission")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showRationaleDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}