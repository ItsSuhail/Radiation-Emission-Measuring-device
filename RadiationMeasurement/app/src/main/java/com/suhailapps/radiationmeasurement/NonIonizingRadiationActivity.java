package com.suhailapps.radiationmeasurement;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NonIonizingRadiationActivity extends AppCompatActivity implements SensorEventListener {

    // Initializing Views
    ImageView startStopIv;
    TextView startStopLbl, measurementLbl;

    // Initializing start stop linear layout
    LinearLayout startStopLl;

    String TAG = "APP_MSG"; // Setting TAG

    boolean state = false;

    // Initializing Sensor Manager
    private static SensorManager manager;
    private Sensor sensor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_ionizing_radiation);
        // Getting views
        startStopIv = findViewById(R.id.ivStartStop2);
        startStopLbl = findViewById(R.id.lblStartStop2);
        startStopLl = findViewById(R.id.llStartStop2);
        measurementLbl = findViewById(R.id.lblMeasurement);

        startStopIv.setImageResource(R.drawable.radiofrequency_image);
        startStopLbl.setText(getString(R.string.start));
        startStopLl.setBackgroundResource(R.drawable.linear_layout_bg_round);
        state=false;

        // Defining Sensor Manager and Sensor
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        startStopLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state){
                    startStopIv.setImageResource(R.drawable.radiofrequency_image);
                    startStopLbl.setText(getString(R.string.start));
                    startStopLl.setBackgroundResource(R.drawable.linear_layout_bg_round);
                    measurementLbl.setText("");
                    state=false;
                    return;
                }
                measurementLbl.setText("-1μT");
                startStopIv.setImageResource(R.drawable.radiofrequency_image_off);
                startStopLbl.setText(getString(R.string.stop));
                startStopLl.setBackgroundResource(R.drawable.linear_layout_bg_round_off);
                state=true;
            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            float azimuth = (sensorEvent.values[0]);
            float pitch = (sensorEvent.values[1]);
            float roll = (sensorEvent.values[2]);

            double tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
            if (state) {
                String measure = String.format("%.0f", tesla);
                measurementLbl.setText(measure + "μT");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sensor!=null){
            manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else{
            Toast.makeText(this, "Error, Sensor not available/supported.", Toast.LENGTH_SHORT).show();
            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        manager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        manager.unregisterListener(this);
        // Heading to MainPage
        Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainPage);
        finish();
    }
}