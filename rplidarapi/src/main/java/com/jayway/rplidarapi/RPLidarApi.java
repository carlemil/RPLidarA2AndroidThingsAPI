package com.jayway.rplidarapi;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.Map;

import static com.jayway.rplidarapi.ApiConstants.*;

/**
 * Created by CarlEmil on 29/07/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class RPLidarApi {

    private static final String TAG = RPLidarApi.class.getSimpleName();

    private UsbSerialDevice serial;

    public RPLidarApi(UsbManager usbManager) {
        Map<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
        for (UsbDevice device : connectedDevices.values()) {
            if (device.getVendorId() == 0x10c4 && device.getProductId() == 0xea60) {
                Log.i(TAG, "Device found: " + device.getDeviceName());
                startSerialConnection(usbManager, device);
                break;
            }
        }
    }

    private void startSerialConnection(UsbManager usbManager, UsbDevice device) {
        UsbDeviceConnection connection = usbManager.openDevice(device);
        serial = UsbSerialDevice.createUsbSerialDevice(device, connection);

        if (serial != null && serial.open()) {
            serial.setBaudRate(115200);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
        }
    }

    public void requestDeviceInfo(DeviceInfoCallback deviceInfoCallback) {
        byte[] buffer = new byte[]{SYNC_BYTE1, GET_INFO_BYTE};
        serial.write(buffer);
        serial.read((data) -> {
            DeviceInfo deviceInfo = new DeviceInfo(data);
            deviceInfoCallback.callback(deviceInfo);
        });
    }

}
