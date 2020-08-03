package com.rcdevelopments.findbluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceScreen extends AppCompatActivity {

    private static final String TAG = "DeviceScreen";
    BluetoothAdapter mBluetoothAdapter;


    String rssi = "Out of range";


    TextView name;
    TextView address;
    ProgressBar bar;
    TextView message;
    TextView dec;
    TextView distance;
    private Handler mHandler;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private String macAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_screen);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final BluetoothDevice dev = getIntent().getExtras().getParcelable("device");
        macAdd = dev.getAddress();


        name = findViewById(R.id.deviceName);
        address = findViewById(R.id.deviceMAC);
        bar = findViewById(R.id.progressBar);
//        message = findViewById(R.id.tvmsg);
        dec = findViewById(R.id.tvdec);
        distance = findViewById(R.id.distance);

//        message.setText("If the bar does not update about every 5 seconds, your device may be out of range.");

        if (dev.getName() != null) {
            name.setText(dev.getName());
        } else {
            name.setText("Unnamed Device");
        }

        address.setText(dev.getAddress());

        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");




        mHandler = new Handler();
        startRepeatingTask();

    }

    BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);


                if (device.getAddress().equals(macAdd)) {
                    Log.d(TAG, "Found matching device");
                    rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                    Toast msg = Toast.makeText(context, rssi, Toast.LENGTH_SHORT);

                    dec.setText(rssi + " dBm");
                    int val = Integer.parseInt(rssi);
                    if (val < -95) {
                        bar.setProgress(10);
                        distance.setText(">50m");
                    }
                    else if (val < -90) {
                        bar.setProgress(20);
                        distance.setText(">40m");
                    } else if (val < -85) {
                        bar.setProgress(30);
                        distance.setText("<30m");
                    } else if (val < -80) {
                        bar.setProgress(35);
                        distance.setText("<16m");
                    } else if (val < -75) {
                        bar.setProgress(50);
                        distance.setText("<10m");
                    } else if (val < -70) {
                        bar.setProgress(60);
                        distance.setText("<8m");
                    } else if (val < -65 ) {
                        bar.setProgress(70);
                        distance.setText("<7m");
                    } else if (val < -60) {
                        bar.setProgress(80);
                        distance.setText("<7m");
                    } else if (val < -50) {
                        bar.setProgress(90);
                        distance.setText("<5m");
                    } else {
                        bar.setProgress(100);
                        distance.setText("<2m");
                    }
                }

                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                Toast debug = Toast.makeText(getApplicationContext(), "repeating task", Toast.LENGTH_SHORT);
                //debug.show();
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permissions in manifest
                    //checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
                if(!mBluetoothAdapter.isDiscovering()){

                    //check BT permissions in manifest
                    //checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

}