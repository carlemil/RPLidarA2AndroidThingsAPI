package se.kjellstrand.debugmodule

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import com.jayway.rplidarapi.RPLidarA2Api
import com.jayway.rplidarapi.ResponseHandler
import com.jayway.rplidarapi.ScanData
import java.util.concurrent.CountDownLatch

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {

    private val latch = CountDownLatch(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appContext = this

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
