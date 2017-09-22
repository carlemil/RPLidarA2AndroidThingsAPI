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
public class RPLidarA2Api {

    private static final String TAG = RPLidarA2Api.class.getSimpleName();

    private UsbSerialDevice serial;
    private int lastReceived;

    public static final byte SCAN = (byte) 0x20;
    public static final byte RCV_SCAN = (byte) 0x81;
    public static final byte SYNC_BYTE0 = (byte) 0xA5;
    public static final byte START_MOTOR = (byte) 0xF0;

    public RPLidarA2Api(UsbManager usbManager) {
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
            serial.setDTR(false);
        }
    }

    public void requestDeviceInfo(ResponseHandler responseHandler) {
        byte[] buffer = new byte[]{SYNC_BYTE1, INFO_BYTE};
        serial.write(buffer);
        serial.read((data) -> {
            DeviceInfo deviceInfo = new DeviceInfo(data);
            responseHandler.handleResponse(deviceInfo);
        });
    }

    public void requestDeviceHealthStatus(ResponseHandler responseHandler) {
        byte[] buffer = new byte[]{SYNC_BYTE1, HEALTH_BYTE};
        serial.write(buffer);
        serial.read((data) -> {
            DeviceHealthStatus deviceHealthStatus = new DeviceHealthStatus(data);
            responseHandler.handleResponse(deviceHealthStatus);
        });
    }

    protected void sendPayLoad(byte command, byte[] payLoad) {
        byte[] bytes = new byte[1024];

        bytes[0] = SYNC_BYTE0;
        bytes[1] = command;

        //add payLoad and calculate checksum
        bytes[2] = (byte) payLoad.length;
        int checksum = 0 ^ bytes[0] ^ bytes[1] ^ (bytes[2] & 0xFF);

        for (int i = 0; i < payLoad.length; i++) {
            bytes[3 + i] = payLoad[i];
            checksum ^= bytes[3 + i];
        }

        //add checksum - now total length is 3 + payLoad.length + 1
        bytes[3 + payLoad.length] = (byte) checksum;

        serial.write(bytes);
    }

    protected void sendPayLoad(byte command, int payLoadInt) {
        byte[] payLoad = new byte[2];

        //load payload little Endian
        payLoad[0] = (byte) payLoadInt;
        payLoad[1] = (byte) (payLoadInt >> 8);

        sendPayLoad(command, payLoad);
    }

    public void startMotor(int speed) {
        sendPayLoad(START_MOTOR, speed);
    }

    public void startScan(ResponseHandler responseHandler) {

        byte[] buffer = new byte[]{SYNC_BYTE1, SCAN_BYTE}; //(byte) 0xF0, 0x02, (byte) 0xC4, 0x02, (byte) 0x91}; //A5 F0 02 C4 02 91};
        serial.setDTR(false);
        serial.write(buffer);
        serial.read((data) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                sb.append(data[i]);
                if (i % 5 == 0)
                    sb.append("\n");
                else
                    sb.append(", ");
            }
            Log.d(TAG, sb.toString());
        });
    }

    protected boolean sendBlocking(byte command, byte expected, long timeout) {
        if (timeout <= 0) {
            sendNoPayLoad(command);
            return true;
        } else {
            lastReceived = 0;
            long endTime = System.currentTimeMillis() + timeout;
            do {
                sendNoPayLoad(command);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (endTime >= System.currentTimeMillis() && lastReceived != expected);
            return lastReceived == expected;
        }
    }

    protected void sendNoPayLoad(byte command) {
        if (true) {
            System.out.printf("Sending command 0x%02x\n", command & 0xFF);
        }

        byte[] dataOut = new byte[]{SYNC_BYTE0, command, 0, 2};
        serial.write(dataOut);
    }

}
