package org.fossify.commons.compose.theme

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import org.fossify.commons.R
import org.fossify.commons.compose.extensions.getActivity
import org.fossify.commons.helpers.APP_ICON_IDS
import org.fossify.commons.helpers.APP_LAUNCHER_NAME
import org.fossify.commons.helpers.BaseConfig

fun Activity.getAppIconIds(): ArrayList<Int> = ArrayList(intent.getIntegerArrayListExtra(APP_ICON_IDS).orEmpty())
fun Activity.getAppLauncherName(): String = intent.getStringExtra(APP_LAUNCHER_NAME).orEmpty()
// sk18: the launcher icon's colour is no longer switched, so the recents entry keeps the app's own icon.
@Suppress("UNUSED_PARAMETER")
internal fun updateRecentsAppIcon(baseConfig: BaseConfig, context: Context) {}

private fun Context.getAppIconIds(): List<Int> = getActivity().getAppIconIds()

private fun Context.getAppLauncherName(): String = getActivity().getAppLauncherName()

