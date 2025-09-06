package numan.smart.super7.flashlight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private ToggleButton toggleButton;
    private boolean hasFlash;

    // Launcher for camera permission request
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Camera permission is required to use the flashlight.", Toast.LENGTH_LONG).show();
                    toggleButton.setEnabled(false);
                }
            });

    // Launcher for notification permission request (Android 13+)
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission is recommended for background control.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);

        // Check if the device has a camera flash
        hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) {
            Toast.makeText(this, "This device does not have a flashlight.", Toast.LENGTH_LONG).show();
            toggleButton.setEnabled(false);
            return;
        }

        // Request necessary permissions
        checkPermissions();

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Start or stop the service to toggle the flashlight
            Intent serviceIntent = new Intent(this, FlashlightService.class);
            serviceIntent.setAction(FlashlightService.ACTION_TOGGLE_FLASHLIGHT);
            startService(serviceIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync the toggle button state with the service's state
        if (hasFlash) {
            toggleButton.setChecked(FlashlightService.isFlashlightOn);
        }
    }

    private void checkPermissions() {
        // Check and request Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Check and request Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
