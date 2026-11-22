package org.fossify.commons.extensions

import android.app.Activity

/**
 * shiroikuma fork: Commons 7 replaced the old launch-count prompts with a timed one — a rating
 * prompt after 5 days on Google Play builds, a donation prompt after 10 days everywhere else.
 * The fork never nags, so this does nothing; the call sites stay as upstream has them.
 */
@Suppress("UnusedReceiverParameter")
internal fun Activity.showAutomaticSupportPromptIfEligible() = Unit
