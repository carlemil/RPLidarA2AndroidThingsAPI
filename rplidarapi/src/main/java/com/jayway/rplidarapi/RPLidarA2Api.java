package com.jayway.rplidarapi;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jayway.rplidarapi.ApiConstants.HEALTH_BYTE;
import static com.jayway.rplidarapi.ApiConstants.INFO_BYTE;
import static com.jayway.rplidarapi.ApiConstants.SCAN_BYTE;
import static com.jayway.rplidarapi.ApiConstants.SYNC_BYTE1;

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
    public static final int MAX_MOTOR_PWM = 1023;
    public static final int DEFAULT_MOTOR_PWM = 660;

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

    public void stopScan() {
        startMotor(0);
    }

    protected void startMotor(int speed) {
        sendPayLoad(START_MOTOR, speed);
    }

    public void startScan(ResponseHandler responseHandler) {
        startScan(responseHandler, DEFAULT_MOTOR_PWM);
    }

    public void startScan(ResponseHandler responseHandler, int speed) {
        startMotor(speed);
        byte[] buffer = new byte[]{SYNC_BYTE1, SCAN_BYTE};
        serial.setDTR(false);
        serial.write(buffer);
        List<ScanData> scanDataList = new ArrayList<>();
        serial.read((data) -> {
            int length = data.length / 5;
            for (int i = 0; i < length; i = i + 5) {
                int b0 = data[i + 0] & 255;
                int b1 = data[i + 1] & 255;
                int b2 = data[i + 2] & 255;
                int b3 = data[i + 3] & 255;
                int b4 = data[i + 4] & 255;
                ScanData scanData = new ScanData(b0, b1, b2, b3, b4);
                if (scanData.startBitSet) {
                    responseHandler.handleResponse(scanDataList);
                    scanDataList.clear();
                }
                scanDataList.add(scanData);
            }
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

    public boolean isLidarAvailable() {
        return serial != null && serial.open();
    }
}
