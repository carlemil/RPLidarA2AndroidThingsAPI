package com.example.carlemil.myapplication;

import android.app.Activity;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import com.jayway.rplidarapi.RPLidarA2Api;

/**
 * adb connect 192.168.1.247
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UsbManager usbManager = getSystemService(UsbManager.class);

        RPLidarA2Api api = new RPLidarA2Api(usbManager);

        try {
            api.startMotor();

            Thread.sleep(4000);
            api.startScan(response -> {
                Log.d(TAG, String.valueOf(response));
            });

            Thread.sleep(2000);
            api.stopMotor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
