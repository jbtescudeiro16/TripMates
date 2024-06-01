package com.example.carride;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class TripActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;

    private final BroadcastReceiver tripSavedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Recebido o broadcast de que os dados da viagem foram salvos com sucesso
            // Iniciar a próxima atividade
            Intent nextIntent = new Intent(TripActivity.this, ResultsActivity.class);
            startActivity(nextIntent);
            Toast.makeText(TripActivity.this, "Viagem Finalizada!", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isServiceRunning())
        {
            setContentView(R.layout.activity_trip);
            Intent intent = new Intent(TripActivity.this, TripTrackingService.class);
            startService(intent);
            chronometer = findViewById(R.id.chronometer);
            startChronometer(null);
            registerReceiver(tripSavedReceiver, new IntentFilter(TripTrackingService.ACTION_TRIP_SAVED));
        }
        else
        {
            setContentView(R.layout.activity_trip2);
            registerReceiver(tripSavedReceiver, new IntentFilter(TripTrackingService.ACTION_TRIP_SAVED));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(tripSavedReceiver);
    }

    public void startChronometer(android.view.View view) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    public void pauseChronometer(android.view.View view) {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    public void finish(android.view.View view) {
        if (running) {
            pauseChronometer(view);
        }

        Intent intent = new Intent(TripActivity.this, TripTrackingService.class);
        stopService(intent);
    }

    private boolean isServiceRunning() {
        Activity activity = this;
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (TripTrackingService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
