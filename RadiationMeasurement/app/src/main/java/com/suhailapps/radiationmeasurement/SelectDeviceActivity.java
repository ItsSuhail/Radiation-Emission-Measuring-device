package com.suhailapps.radiationmeasurement;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {

    // Initializing Recycler View
    RecyclerView bluetoothListRv;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        // Defining recycler view
        bluetoothListRv = findViewById(R.id.rvBluetoothList);


        // Setting up bluetooth

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Getting list of paired bluetooth devices
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SelectDeviceActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();
        if(pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName, deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
            }


            bluetoothListRv.setLayoutManager(new LinearLayoutManager(this));
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this, deviceList);
            bluetoothListRv.setAdapter(deviceListAdapter);
            bluetoothListRv.setItemAnimator(new DefaultItemAnimator());
        }
        else{
            Snackbar.make(bluetoothListRv, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Heading to MainPage
                    Intent IonizingPage = new Intent(getApplicationContext(), IonizingRadiationActivity.class);
                    IonizingPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(IonizingPage);
                    finish();
                }
            }).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Heading to MainPage
        Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainPage);
        finish();
    }
}