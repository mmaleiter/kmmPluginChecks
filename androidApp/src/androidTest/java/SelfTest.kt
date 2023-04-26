import android.Manifest
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import base.BaseTest
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelfTest : BaseTest() {

//    @JvmField
//    @Rule
//    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
//        Manifest.permission.BLUETOOTH,
//        Manifest.permission.BLUETOOTH_ADMIN,
//        Manifest.permission.BLUETOOTH_CONNECT,
//        Manifest.permission.BLUETOOTH_SCAN,
//        Manifest.permission.BLUETOOTH_ADVERTISE,
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION,
//        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,

//        )

    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH,
//        Manifest.permission.BLUETOOTH_ADMIN,
//        Manifest.permission.BLUETOOTH_CONNECT,
//        Manifest.permission.BLUETOOTH_SCAN,
//        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
//        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,


    )

    @Test
    fun control(){
//        showLoader(true)
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if(bluetoothAdapter.isEnabled) {
//            bluetoothAdapter.disable()
//            SystemClock.sleep(2000)
//            showLoader(false)
//            assert(!bluetoothAdapter.isEnabled)
//        } else {
//            bluetoothAdapter.isEnabled
//            SystemClock.sleep(2000)
//            showLoader(false)
//            assert(bluetoothAdapter.isEnabled)
//        }

    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.check.kmm.pluginchecks.android", appContext.getPackageName())
    }

}