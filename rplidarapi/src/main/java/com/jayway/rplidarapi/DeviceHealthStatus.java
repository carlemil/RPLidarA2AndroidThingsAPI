package com.jayway.rplidarapi;

/**
 * Created by CarlEmil on 29/07/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class DeviceHealthStatus extends Response {
    private int status;
    private int errorcode;

    public static final int GOOD = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    DeviceHealthStatus(byte[] data) {
        if (data.length == 10) {
            status = data[7];
            errorcode = (data[9] << 8) + data[8];
        }
    }

    @Override
    public String toString() {
        return "DeviceHealthStatus{" +
                "status=" + status +
                ", errorcode=" + errorcode +
                '}';
    }
}
