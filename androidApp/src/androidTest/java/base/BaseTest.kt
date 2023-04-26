package base

import androidx.test.espresso.IdlingRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import android.Manifest
import android.util.Log
import com.check.kmm.pluginchecks.android.kable.check.ui.NotThrownException
import com.check.kmm.pluginchecks.android.kable.check.ui.StartActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseTest {

    var grantPermissions: List<String> = emptyList()

    @Volatile
    lateinit var waitForit: ActivityIdleManager

    @get:Rule
    var activityRule = ActivityTestRule(StartActivity::class.java, true)



    @Before
    fun setup() {
        waitForit = ActivityIdleManager(activityRule.activity)
        IdlingRegistry.getInstance().register(waitForit)
    }

    @After
    fun close() {
        IdlingRegistry.getInstance().unregister(waitForit)
    }

fun showLoader(show: Boolean){
    activityRule.activity.showLoader(show)
}



    fun isExpectedException(exception: Throwable) : Boolean {
//        // todo improve on parameter maybe use inline construct
        val isIt = exception.javaClass == activityRule.activity.testedException.javaClass
        if(isIt) return true
        Log.e(this.javaClass.simpleName,"isExpectedException  setup issue or false positive" ,exception)
        // todo  currently you have to assert true. later we should
        //       throw activityRule.activity.testedException
        // if you don't assert you risc false possitive
        return false
    }

    fun runWrapped(exception: Throwable = NotThrownException(), lambda: () -> Unit) {
        IdlingRegistry.getInstance().register(waitForit)
        lambda.invoke()
        isExpectedException(exception)
        IdlingRegistry.getInstance().unregister(waitForit)
    }
}

inline fun <reified EX : Throwable> isExpectedException(activity: StartActivity , throwMe: Boolean= false) : Boolean {
    val itIs = activity.testedException.javaClass is EX
    if(itIs) return true
    Log.v(activity.javaClass.simpleName,"" , activity.testedException)
    if(throwMe) throw activity.testedException
    return false
}

