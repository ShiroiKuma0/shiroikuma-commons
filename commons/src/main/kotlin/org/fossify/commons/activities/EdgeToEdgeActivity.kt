package org.fossify.commons.activities

import org.fossify.commons.extensions.setNavigationBarAppearance
import org.fossify.commons.extensions.setStatusBarAppearance
import org.fossify.commons.extensions.beVisibleIf
import android.widget.FrameLayout
import android.view.Gravity
import android.view.ViewGroup
import org.fossify.commons.helpers.isUiColorSet
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.get
import androidx.core.view.size
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.R
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getColoredDrawableWithColor
import org.fossify.commons.extensions.getColoredMaterialStatusBarColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.onApplyWindowInsets
import org.fossify.commons.extensions.setSystemBarsAppearance
import org.fossify.commons.extensions.updateMarginWithBase
import org.fossify.commons.extensions.updatePaddingWithBase
import org.fossify.commons.views.MyAppBarLayout

abstract class EdgeToEdgeActivity : AppCompatActivity() {
    open var isSearchBarEnabled = false
    open val padCutout: Boolean
        get() = true

    private var topAppBar: MyAppBarLayout? = null
    private var scrollingView: ScrollingView? = null
    private var materialScrollColorAnimation: ValueAnimator? = null
    private var currentScrollY = 0

    private val contentRoot by lazy { findViewById<View>(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
    }

    override fun onResume() {
        super.onResume()
        window.setSystemBarsAppearance(getProperBackgroundColor())
        applySystemBarRoles()
    }

    // shiroikuma (sk18): the status and navigation bars are roles. Edge to edge they are transparent and the
    // screen shows through, which stays the default; once a role is SET on the UI page, a strip of that
    // colour is drawn behind the bar's icons, as high as the bar's inset.
    private var statusBarStrip: View? = null
    private var navigationBarStrip: View? = null

    private fun applySystemBarRoles() {
        val decor = window.decorView as? ViewGroup ?: return
        val statusSet = isUiColorSet(UiRole.STATUS_BAR)
        val navigationSet = isUiColorSet(UiRole.NAVIGATION_BAR)
        if (!statusSet && !navigationSet && statusBarStrip == null && navigationBarStrip == null) return

        fun strip(existing: View?, gravity: Int): View = existing ?: View(this).also {
            decor.addView(it, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, gravity))
        }
        val status = strip(statusBarStrip, Gravity.TOP).also { statusBarStrip = it }
        val navigation = strip(navigationBarStrip, Gravity.BOTTOM).also { navigationBarStrip = it }
        status.beVisibleIf(statusSet)
        navigation.beVisibleIf(navigationSet)
        if (statusSet) {
            status.setBackgroundColor(uiColor(UiRole.STATUS_BAR))
            window.setStatusBarAppearance(uiColor(UiRole.STATUS_BAR))
        }
        if (navigationSet) {
            navigation.setBackgroundColor(uiColor(UiRole.NAVIGATION_BAR))
            window.setNavigationBarAppearance(uiColor(UiRole.NAVIGATION_BAR))
        }
        ViewCompat.getRootWindowInsets(decor)?.let { insets ->
            val bars = insets.getInsetsIgnoringVisibility(Type.systemBars())
            status.layoutParams = status.layoutParams.apply { height = bars.top }
            navigation.layoutParams = navigation.layoutParams.apply { height = bars.bottom }
        } ?: decor.post { if (!isDestroyed) applySystemBarRoles() }
    }

    /**
     * Helper for views that need to be edge to edge compatible.
     */
    fun setupEdgeToEdge(
        padTopSystem: List<View> = emptyList(),
        padBottomSystem: List<View> = emptyList(),
        padBottomImeAndSystem: List<View> = emptyList(),
        moveBottomSystem: List<View> = emptyList(),
        animateIme: Boolean = false,
    ) {
        onApplyWindowInsets { insets ->
            val system = insets.getInsetsIgnoringVisibility(Type.systemBars())
            val imeAndSystem = insets.getInsets(Type.ime() or Type.systemBars())

            padTopSystem.forEach { it.updatePaddingWithBase(top = system.top) }
            padBottomSystem.forEach { it.updatePaddingWithBase(bottom = system.bottom) }
            padBottomImeAndSystem.forEach { it.updatePaddingWithBase(bottom = imeAndSystem.bottom) }
            moveBottomSystem.forEach { it.updateMarginWithBase(bottom = system.bottom) }

            if (padCutout) {
                val cutout = insets.getInsets(Type.displayCutout())
                val sideLeft = maxOf(system.left, cutout.left)
                val sideRight = maxOf(system.right, cutout.right)
                contentRoot.updatePaddingWithBase(left = sideLeft, right = sideRight)
            }

            if (animateIme) {
                ViewCompat.setWindowInsetsAnimationCallback(
                    contentRoot,
                    object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                        override fun onProgress(
                            insets: WindowInsetsCompat,
                            runningAnimations: MutableList<WindowInsetsAnimationCompat>
                        ): WindowInsetsCompat {
                            val bottom = insets.getInsets(Type.systemBars() or Type.ime()).bottom
                            padBottomImeAndSystem.forEach {
                                it.updatePaddingWithBase(bottom = bottom)
                            }
                            return insets
                        }
                    }
                )
            }
        }
    }

    fun setupMaterialScrollListener(scrollingView: ScrollingView?, topAppBar: MyAppBarLayout) {
        this.scrollingView = scrollingView
        this.topAppBar = topAppBar
        if (scrollingView is RecyclerView) {
            scrollingView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                val newScrollY = scrollingView.computeVerticalScrollOffset()
                scrollingChanged(newScrollY, currentScrollY)
                currentScrollY = newScrollY
            }
        } else if (scrollingView is NestedScrollView) {
            scrollingView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                scrollingChanged(scrollY, oldScrollY)
            }
        }
    }

    private fun scrollingChanged(newScrollY: Int, oldScrollY: Int) {
        if (newScrollY > 0 && oldScrollY == 0) {
            animateTopBarColors(uiColor(UiRole.TOOLBAR_GROUND), getColoredMaterialStatusBarColor())
        } else if (newScrollY == 0 && oldScrollY > 0) {
            animateTopBarColors(getColoredMaterialStatusBarColor(), getRequiredTopBarColor())
        }
    }

    fun animateTopBarColors(colorFrom: Int, colorTo: Int) {
        if (topAppBar == null) {
            return
        }

        materialScrollColorAnimation?.end()
        materialScrollColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        materialScrollColorAnimation!!.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            if (topAppBar != null) {
                updateTopBarColors(topAppBar!!, color)
            }
        }

        materialScrollColorAnimation!!.start()
    }

    fun getRequiredTopBarColor(): Int {
        return if (
            (scrollingView is RecyclerView || scrollingView is NestedScrollView)
            && scrollingView?.computeVerticalScrollOffset() == 0
        ) {
            uiColor(UiRole.TOOLBAR_GROUND)
        } else {
            getColoredMaterialStatusBarColor()
        }
    }

    fun updateTopBarColors(topAppBar: MyAppBarLayout, color: Int) {
        // sk18: title and icons of the top bar are roles of their own, not the contrast of its ground
        val titleColor = uiColor(UiRole.TOOLBAR_TITLE)
        val contrastColor = uiColor(UiRole.TOOLBAR_ICON)

        window.setSystemBarsAppearance(color)
        if (!isSearchBarEnabled) {
            topAppBar.setBackgroundColor(color)
            topAppBar.toolbar?.setBackgroundColor(color)
            topAppBar.toolbar?.setTitleTextColor(titleColor)
            topAppBar.toolbar?.navigationIcon?.applyColorFilter(contrastColor)
            topAppBar.toolbar?.collapseIcon = resources.getColoredDrawableWithColor(
                drawableId = R.drawable.ic_arrow_left_vector,
                color = contrastColor
            )
        }

        topAppBar.toolbar?.overflowIcon =
            resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, contrastColor)

        val menu = topAppBar.toolbar?.menu ?: return
        for (i in 0 until menu.size) {
            try {
                menu[i].icon?.setTint(contrastColor)
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onDestroy() {
        materialScrollColorAnimation?.cancel()
        materialScrollColorAnimation = null
        super.onDestroy()
    }
}
