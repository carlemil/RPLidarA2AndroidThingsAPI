package com.jayway.rplidarapi;

import android.util.Log;

/**
 * Created by carlemil on 9/25/17.
 */

class ScanData {

    public final int quality;
    public final boolean startBitSet;
    public final double angle;
    public final double distance;

    ScanData(int b0, int b1, int b2, int b3, int b4) {
        quality = b0 >> 2;
        startBitSet = (b0 & 0x1) == 0x1;
        angle = ((b1 >> 1) + (b2 << 7)) / 64;
        distance = (b3 + (b4 << 8)) / 4;
        Log.d("TAG", "sb: " + startBitSet + " an: " + angle + " di: " + distance + " --- " + b0 + " " + b1 + " " + b2 + " " + b3 + " " + b4);
    }
}
