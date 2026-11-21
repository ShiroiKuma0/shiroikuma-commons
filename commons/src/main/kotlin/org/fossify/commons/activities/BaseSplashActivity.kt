package org.fossify.commons.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.fossify.commons.R
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.SIDELOADING_TRUE
import org.fossify.commons.helpers.SIDELOADING_UNCHECKED

abstract class BaseSplashActivity : AppCompatActivity() {
    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (baseConfig.appSideloadingStatus == SIDELOADING_UNCHECKED) {
            if (checkAppSideloading()) {
                return
            }
        } else if (baseConfig.appSideloadingStatus == SIDELOADING_TRUE) {
            showSideloadingDialog()
            return
        }

        syncGlobalConfig {
            initActivity()
        }
    }
}
