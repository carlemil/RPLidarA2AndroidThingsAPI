package com.jayway.rplidarapi;

/**
 * Created by CarlEmil on 29/07/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class DeviceInfo extends Response {
    private String model;
    private String firmwareVersion;
    private String hardware;
    private String serialnumber;

    DeviceInfo(byte[] data) {
        if (data.length == ApiConstants.DESCRIPTOR_LEN + ApiConstants.INFO_LEN) {
            model = Util.bytesToHex(data[7]);
            firmwareVersion = Util.bytesToHex(data[9]) + "." + Util.bytesToHex(data[8]);
            hardware = Util.bytesToHex(data[10]);
            byte[] serial = new byte[data.length - 11];
            System.arraycopy(data, 11, serial, 0, serial.length);
            this.serialnumber = Util.bytesToHex(serial);
        }
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "model=" + model +
                ", firmwareVersion=" + firmwareVersion +
                ", hardware=" + hardware +
                ", serialnumber='" + serialnumber + '\'' +
                '}';
    }
}
