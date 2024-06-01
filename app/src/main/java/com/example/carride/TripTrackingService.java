package com.example.carride;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TripTrackingService extends Service implements LocationListener, SensorEventListener {

    private static final String TAG = "TripForegroundService";
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "TripForegroundServiceChannel";

    public static final String ACTION_TRIP_SAVED = "com.example.carride.TRIP_SAVED";

    private LocationManager locationManager;
    private Location lastLocation;
    private double totalDistance = 0;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final long SHAKE_INTERVAL = 5000;
    private long lastShakeTime = 0;
    private int lightJolts = 0;
    private int normalJolts = 0;
    private int strongJolts = 0;

    private BroadcastReceiver screenReceiver;
    private int phoneAccesses = 0;

    private String userId;
    private String userName;
    private long tripStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = getUserId();
        getUserName();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    registerPhoneAccess();
                }
            }
        };

        registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

        tripStartTime = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        startForeground(NOTIFICATION_ID, getNotification());
        startLocationUpdates();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        sensorManager.unregisterListener(this);
        unregisterReceiver(screenReceiver);
        saveTripData();
        sendTripSavedBroadcast();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);

            if (distance > 3) {
                totalDistance += distance;
            }
        }
        lastLocation = location;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double acceleration = Math.sqrt(x * x + y * y + z * z);

        if (acceleration > 18 && System.currentTimeMillis() - lastShakeTime > SHAKE_INTERVAL) {
            lastShakeTime = System.currentTimeMillis();
            registerShake(acceleration);
        }
    }

    private void registerShake(double acceleration) {
        if (acceleration >= 18 && acceleration < 22) {
            lightJolts++;
        } else if (acceleration >= 22 && acceleration < 25) {
            normalJolts++;
        } else if (acceleration >= 25) {
            strongJolts++;
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private Notification getNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Trip Tracking Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        String notificationText = "Boa Viagem";
        if (userName != null) {
            notificationText += " " + userName;
        }

        Intent intent = new Intent(this, TripActivity.class) ;
        PendingIntent contentIntent = PendingIntent. getActivity (this, 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_MUTABLE) ;

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TripMates")
                .setContentText(notificationText)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }


    private void registerPhoneAccess() {
        phoneAccesses++;
    }

    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private void getUserName() {
        if (userId != null) {
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                userName = documentSnapshot.getString("name");
                                updateNotification();
                            } else {
                                Log.e(TAG, "User document does not exist");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error getting user name: " + e.getMessage());
                        }
                    });
        } else {
            Log.e(TAG, "User ID is null");
        }
    }


    private void saveTripData() {
        if (userId != null) {
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.update("tripsCount", FieldValue.increment(1))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Trip count incremented");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating trip counter: " + e.getMessage());
                        }
                    });

            Map<String, Object> tripData = new HashMap<>();
            tripData.put("userId", userId);
            tripData.put("distance", totalDistance);
            tripData.put("light_jolts", lightJolts);
            tripData.put("normal_jolts", normalJolts);
            tripData.put("strong_jolts", strongJolts);
            tripData.put("phoneAccesses", phoneAccesses);

            // Utilize SimpleDateFormat para formatar o timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());
            tripData.put("timestamp", timestamp);

            // Calcular o tempo decorrido da viagem em segundos
            long elapsedTime = (System.currentTimeMillis() - tripStartTime) / 1000;

            // Adicionar o tempo decorrido da viagem aos dados salvos
            tripData.put("elapsedTime", elapsedTime);

            db.collection("trips").add(tripData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Trip data saved successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error saving trip data: " + e.getMessage());
                        }
                    });
        }
    }


    private void sendTripSavedBroadcast() {
        Intent intent = new Intent(ACTION_TRIP_SAVED);
        sendBroadcast(intent);
    }

    private void updateNotification() {
        Notification notification = getNotification();
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
