package numan.smart.super7.flashlight;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FlashlightService extends Service {

    private CameraManager cameraManager;
    private String cameraId;
    public static boolean isFlashlightOn = false;

    private static final String CHANNEL_ID = "FlashlightServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_TOGGLE_FLASHLIGHT = "numan.smart.super7.flashlight.TOGGLE_FLASHLIGHT";


    @Override
    public void onCreate() {
        super.onCreate();
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_TOGGLE_FLASHLIGHT.equals(intent.getAction())) {
            toggleFlashlight();
        }
        return START_STICKY;
    }

    private void toggleFlashlight() {
        if (isFlashlightOn) {
            turnOffFlashlight();
        } else {
            turnOnFlashlight();
        }
    }

    private void turnOnFlashlight() {
        try {
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, true);
                isFlashlightOn = true;
                startForeground(NOTIFICATION_ID, createNotification());
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Failed to turn on flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOffFlashlight() {
        try {
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashlightOn = false;
                stopForeground(true);
                stopSelf(); // Stop the service when flashlight is off
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Failed to turn off flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    private Notification createNotification() {
        // Intent to open the app when notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Intent for the toggle action in the notification
        Intent toggleIntent = new Intent(this, FlashlightService.class);
        toggleIntent.setAction(ACTION_TOGGLE_FLASHLIGHT);
        PendingIntent togglePendingIntent = PendingIntent.getService(this, 0, toggleIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Flashlight is On")
                .setContentText("Tap to open the app and turn off.")
                .setSmallIcon(R.drawable.flashlight_on_24) // Make sure you have this drawable
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.flashlight_off_24, "Turn Off", togglePendingIntent) // And this one
                .setOngoing(true) // Makes the notification non-dismissable
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Flashlight Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Ensure flashlight is off when service is destroyed
        if (isFlashlightOn) {
            turnOffFlashlight();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

