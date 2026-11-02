package org.fossify.commons

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import org.fossify.commons.extensions.appLockManager
import org.fossify.commons.extensions.checkUseEnglish

open class FossifyApp : Application() {

    open val isAppLockFeatureAvailable = false

    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        setupAppLockManager()
        // Fork (sk9): know the resumed activity, for theme-styled toasts raised from non-activity contexts.
        registerActivityLifecycleCallbacks(org.fossify.commons.helpers.ForegroundActivity)
    }

    private fun setupAppLockManager() {
        if (isAppLockFeatureAvailable) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(appLockManager)
        }
    }
}
