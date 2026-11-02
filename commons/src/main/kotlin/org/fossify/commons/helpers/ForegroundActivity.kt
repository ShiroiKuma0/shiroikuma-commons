package org.fossify.commons.helpers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Fork addition (sk9): the activity currently resumed, if any — so a toast raised from a receiver or
 * service while the app is on screen can still use the theme-styled toast, which needs an activity
 * context. Registered once by FossifyApp; holds the activity weakly and clears it on pause.
 */
object ForegroundActivity : Application.ActivityLifecycleCallbacks {
    private var current: WeakReference<Activity>? = null

    val activity: Activity?
        get() = current?.get()?.takeIf { !it.isFinishing && !it.isDestroyed }

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (current?.get() === activity) {
            current = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
