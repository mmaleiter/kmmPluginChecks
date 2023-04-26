import android.Manifest
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import base.BaseTest
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestHappyPath : BaseTest(){
    @JvmField
    @Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,

    )


    fun stopScan() {
        Espresso.onView(ViewMatchers.withId(
            com.check.kmm.pluginchecks.android.R.id.stop_scan))
            .perform(ViewActions.click())
    }


    fun startScan() {
        Espresso.onView(ViewMatchers.withId(com.check.kmm.pluginchecks.android.R.id.start_scan))
            .perform(ViewActions.click())
    }

}