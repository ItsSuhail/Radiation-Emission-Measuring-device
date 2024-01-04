package com.suhailapps.radiationmeasurement;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.UUID;

public class IonizingRadiationActivity extends AppCompatActivity {

    // Initializing Views
    ImageView startStopIv;
    TextView startStopLbl;
    Button connectBtn;
    ProgressBar btcPb;
    TextView geigerCountLbl;

    // Initializing start stop linear layout
    LinearLayout startStopLl;

    static String TAG = "APP_MSG"; // Setting TAG

    boolean state = false;
    boolean connectionState = false;

    private String deviceName = null;
    private String deviceAddress = null;
    public static Handler handler;
    public static BluetoothSocket btSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ionizing_radiation);

        /*
        Basic Logic ->
        connect to bluetooth
        after connection ->
            when pressed on start ->
                send message to geiger counter (arduino)
            when pressed on stop ->
                send message to geiger counter to stop

            connection interrupts ->
                close socket
                set button to state 0


         */

        // Getting views
        startStopIv = findViewById(R.id.ivStartStop);
        startStopLbl = findViewById(R.id.lblStartStop);
        startStopLl = findViewById(R.id.llStartStop);
        connectBtn = findViewById(R.id.btnConnect);
        geigerCountLbl = findViewById(R.id.lblGeigerCount);
        btcPb = findViewById(R.id.pbBtc);

        startStopIv.setImageResource(R.drawable.nuclear_image);
        startStopLbl.setText(getString(R.string.start));
        startStopLl.setBackgroundResource(R.drawable.linear_layout_bg_round);
        state = false;
        connectionState = false;

        connectBtn.setEnabled(true);
        startStopLl.setEnabled(false);

        // Changing Pb's color
        btcPb.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.yellow1), PorterDuff.Mode.SRC_IN);


        // Getting extra from intent (if aint null)
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            deviceAddress = getIntent().getStringExtra("deviceAddress");

            // Make Pb visible
            Log.d(TAG, "Establishing Connection: " + deviceName + "<|>" + deviceAddress);
            btcPb.setVisibility(View.VISIBLE);
            connectBtn.setEnabled(false);
            Toast.makeText(this, "Establishing Connection", Toast.LENGTH_SHORT).show();

            // Setting up connected, creating thread
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }


        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch (msg.arg1){
                            case 1:
                                Toast.makeText(IonizingRadiationActivity.this, "Connected to device: "+deviceName, Toast.LENGTH_SHORT).show();
                                connectionState = true;
                                changeConnectState();
                                geigerCountLbl.setText(getString(R.string.connected));

                                btcPb.setVisibility(View.GONE);
                                connectBtn.setEnabled(true);
                                startStopLl.setEnabled(true);
                                buttonStateOff();

                                break;

                            case -1:
                                switch (msg.arg2){
                                    case -1:
                                        Toast.makeText(IonizingRadiationActivity.this, "Failed to connect to: "+deviceName, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Toast.makeText(IonizingRadiationActivity.this, "Force closing the connection.", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                connectionState = false;
                                changeConnectState();
                                btcPb.setVisibility(View.GONE);
                                connectBtn.setEnabled(true);

                                buttonStateOff();
                                startStopLl.setEnabled(false);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String geigerReading = msg.obj.toString().trim();
                        int countsPerMinute = Integer.parseInt(geigerReading);
                        double microSievertPerHour = countsPerMinute * 0.00812;
                        DecimalFormat decimalFormat = new DecimalFormat("0.000");

                        String reading = decimalFormat.format(microSievertPerHour) + " μSv/h";
                        geigerCountLbl.setText(reading);
                        break;
                }
            }
        };

        // When pressed on connect button
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!connectionState) {
                    // Heading to SelectDevicePage
                    Intent SelectDevicePage = new Intent(getApplicationContext(), SelectDeviceActivity.class);
                    SelectDevicePage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(SelectDevicePage);
                    finish();
                }
                else{
                    // Asking if the user surely wants to disconnect
                    AlertDialog.Builder builder = new AlertDialog.Builder(IonizingRadiationActivity.this);
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setMessage("Are you sure you want to Disconnect?");
                    builder.setTitle("Disconnect");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(connectedThread!=null) {connectedThread.write("0"); connectedThread.cancel();}
                            if(createConnectThread!=null) {createConnectThread.cancel();}

                            geigerCountLbl.setText("");
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        // When pressed anywhere on ll
        startStopLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state) {
                    buttonStateOff();
                    connectedThread.write("0");
                    geigerCountLbl.setText("");
                    return;
                }

                buttonStateOn();
                Toast.makeText(IonizingRadiationActivity.this, "Please wait at least 10 seconds before when you stop the measuring. ", Toast.LENGTH_SHORT).show();
                connectedThread.write("1");
                geigerCountLbl.setText(getString(R.string.reading));

            }
        });
    }

    public void buttonStateOff(){
        startStopIv.setImageResource(R.drawable.nuclear_image);
        startStopLbl.setText(getString(R.string.start));
        startStopLl.setBackgroundResource(R.drawable.linear_layout_bg_round);
        state = false;
    }

    public void buttonStateOn(){
        startStopIv.setImageResource(R.drawable.nuclear_image_off);
        startStopLbl.setText(getString(R.string.stop));
        startStopLl.setBackgroundResource(R.drawable.linear_layout_bg_round_off);
        state = true;
    }

    public void changeConnectState(){
        if(connectionState) {
            connectBtn.setText(getString(R.string.disconnect));
        }
        else{
            connectBtn.setText(getString(R.string.connect));
        }
    }

    public class CreateConnectThread extends Thread {
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            // Using a temporary object, it is later assigned to the btSocket
            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket temp = null;
            if (ActivityCompat.checkSelfPermission(IonizingRadiationActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 31) {
                    ActivityCompat.requestPermissions(IonizingRadiationActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                }
            }
            UUID uuid = btDevice.getUuids()[0].getUuid();

            try {
                temp = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Error on setting up socket (CreateConnectThread)", e);
            }
            btSocket = temp;

        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        public void run() {
            // Cancel discovery for more efficiency
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (ActivityCompat.checkSelfPermission(IonizingRadiationActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(IonizingRadiationActivity.this, "Please enable bluetooth scan permission", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(IonizingRadiationActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
            }

            bluetoothAdapter.cancelDiscovery();

            // Trying to set up connection
            try{
                btSocket.connect();
                Log.d(TAG, "Connected to device");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            }
            catch (IOException conE){
                Log.e(TAG, "Error occurred while connecting to the client", conE);
                try{
                    btSocket.close();
                    Log.e(TAG, "Closing the connection");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                }
                catch (IOException cloE){
                    Log.e(TAG, "Error on Closing the connection", cloE);
                }

                return;
            }

            connectedThread = new ConnectedThread(btSocket);
            connectedThread.run();
        }

        // For closing the socket
        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "IOException, unable to get In, Out Stream (ConnectedThread)", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.d("Geiger Reading", readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(CONNECTING_STATUS, -1, 1).sendToTarget();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mSocket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Unable to cancel(close) the socket. ", e);
            }
        }
    }


    // Called when back button is pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(connectedThread != null){
            connectedThread.cancel();
        }
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        // Heading to MainPage
        Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainPage);
        finish();
    }

}
