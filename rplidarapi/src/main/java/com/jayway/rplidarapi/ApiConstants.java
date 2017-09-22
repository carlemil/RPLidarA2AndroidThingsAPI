package com.jayway.rplidarapi;

/**
 * Created by CarlEmil on 29/07/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
class ApiConstants {
    static final byte SYNC_BYTE1 = (byte) 0xA5;
    // --Commented out by Inspection (29/07/2017 18:34):static final byte SYNC_BYTE2 = 0x5A;

    static final byte INFO_BYTE = 0x50;
    static final byte HEALTH_BYTE = 0x52;
    static final byte SCAN_BYTE = 0x20;
    //
//    STOP_BYTE = b'\x25'
//    RESET_BYTE = b'\x40'
//
//    SCAN_BYTE = b'\x20'
//    FORCE_SCAN_BYTE = b'\x21'
//
    static final byte DESCRIPTOR_LEN = 7;
    static final byte INFO_LEN = 20;
    static final byte HEALTH_LEN = 3;
//
//    INFO_TYPE = 4
//    HEALTH_TYPE = 6
//    SCAN_TYPE = 129
//
//            #Constants & Command to start A2 motor
//            MAX_MOTOR_PWM = 1023
//    DEFAULT_MOTOR_PWM = 660
//    SET_PWM_BYTE = b'\xF0'
//
//    _HEALTH_STATUSES = {
//        0: 'Good',
//                1: 'Warning',
//                2: 'Error',
//    }


}
