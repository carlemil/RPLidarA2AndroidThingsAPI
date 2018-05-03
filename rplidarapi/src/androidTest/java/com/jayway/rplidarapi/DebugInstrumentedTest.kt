package com.jayway.rplidarapi

import android.content.Context
import android.hardware.usb.UsbManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class DebugInstrumentedTest {

    private val latch = CountDownLatch(1)

    @Test
    fun debugLidarScanData() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        val usbManager = appContext.getSystemService(Context.USB_SERVICE)
        val rpLidar = RPLidarA2Api(usbManager as UsbManager?)
        rpLidar.stopScan()
        Log.d("TAG", "START SCAN")
        rpLidar.startScan(ResponseHandler {
            fun handleResponse(scanDataList: List<ScanData>) {
                scanDataList.forEach {
                    Log.d("TAG", "SCAN: ${it.distance}, ${it.angle}")
                }
                latch.countDown()
            }
        }, 200)

        latch.await();
        rpLidar.stopScan()
        Log.d("TAG", "STOP SCAN")
    }
}
