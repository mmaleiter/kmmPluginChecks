package base

import android.view.View
import android.widget.ProgressBar
import androidx.test.espresso.IdlingResource
import com.check.kmm.pluginchecks.android.R
import com.check.kmm.pluginchecks.android.kable.check.ui.StartActivity

class ActivityIdleManager(private val activity: StartActivity?) : IdlingResource {
    @Volatile
    var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = "loginIdling"

    override fun isIdleNow(): Boolean {
        val iAmIdling =
            when(activity){
                null -> false
                else -> activity.findViewById<ProgressBar>(R.id.blockingProgress).visibility == View.GONE
            }
        if (iAmIdling) resourceCallback?.onTransitionToIdle()
        return iAmIdling
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

//    override fun onSuccess(response: Unit) {
//
//    }
//
//    override fun onError(e: Throwable) {
//        throw e
//    }

}