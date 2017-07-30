package com.example.carlemil.myapplication;

import android.app.Activity;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import com.jayway.rplidarapi.RPLidarApi;

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

        RPLidarApi api = new RPLidarApi(usbManager);

        api.requestDeviceInfo(deviceInfo -> Log.d(TAG, "device info : " + deviceInfo));

        try {
            Thread.sleep(500);
            api.requestDeviceHealthStatus(deviceHealth -> Log.d(TAG, "deviceHealth" + deviceHealth));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
