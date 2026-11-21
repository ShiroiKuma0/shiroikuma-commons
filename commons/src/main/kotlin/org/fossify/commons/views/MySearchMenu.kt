package org.fossify.commons.views

import org.fossify.commons.extensions.applyFieldCursorRoles
import android.graphics.drawable.GradientDrawable
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.appbar.MaterialToolbar
import org.fossify.commons.R
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.databinding.MenuSearchBinding
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.extensions.onTextChangeListener
import org.fossify.commons.extensions.removeBit
import org.fossify.commons.extensions.showKeyboard
import org.fossify.commons.helpers.LOWER_ALPHA
import org.fossify.commons.helpers.MEDIUM_ALPHA

open class MySearchMenu(context: Context, attrs: AttributeSet) : MyAppBarLayout(context, attrs) {
    var isSearchOpen = false
    var useArrowIcon = false
    var onSearchOpenListener: (() -> Unit)? = null
    var onSearchClosedListener: (() -> Unit)? = null
    var onSearchTextChangedListener: ((text: String) -> Unit)? = null
    var onNavigateBackClickListener: (() -> Unit)? = null

    val binding = MenuSearchBinding.inflate(LayoutInflater.from(context), this)

    override val toolbar: MaterialToolbar?
        get() = binding.topToolbar

    fun setupMenu() {
        binding.topToolbarSearchIcon.setOnClickListener {
            if (isSearchOpen) {
                closeSearch()
            } else if (useArrowIcon && onNavigateBackClickListener != null) {
                onNavigateBackClickListener!!()
            } else {
                binding.topToolbarSearch.requestFocus()
                (context as? Activity)?.showKeyboard(binding.topToolbarSearch)
            }
        }

        post {
            binding.topToolbarSearch.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    openSearch()
                }
            }
        }

        binding.topToolbarSearch.onTextChangeListener { text ->
            onSearchTextChangedListener?.invoke(text)
        }
    }

    fun focusView() {
        binding.topToolbarSearch.requestFocus()
    }

    private fun openSearch() {
        isSearchOpen = true
        onSearchOpenListener?.invoke()
        binding.topToolbarSearchIcon.setImageResource(R.drawable.ic_arrow_left_vector)
        binding.topToolbarSearchIcon.contentDescription = resources.getString(R.string.back)
    }

    fun closeSearch() {
        isSearchOpen = false
        onSearchClosedListener?.invoke()
        binding.topToolbarSearch.setText("")
        if (!useArrowIcon) {
            binding.topToolbarSearchIcon.setImageResource(R.drawable.ic_search_vector)
            binding.topToolbarSearchIcon.contentDescription = resources.getString(R.string.search)
        }
        (context as? Activity)?.hideKeyboard()
    }

    fun getCurrentQuery() = binding.topToolbarSearch.text.toString()

    fun updateHintText(text: String) {
        binding.topToolbarSearch.hint = text
    }

    @Suppress("unused", "EmptyFunctionBlock")
    @Deprecated("This feature is broken for now.")
    fun toggleHideOnScroll(hideOnScroll: Boolean) {}

    fun toggleForceArrowBackIcon(useArrowBack: Boolean) {
        this.useArrowIcon = useArrowBack
        val (icon, accessibilityString) = if (useArrowBack) {
            Pair(R.drawable.ic_arrow_left_vector, R.string.back)
        } else {
            Pair(R.drawable.ic_search_vector, R.string.search)
        }

        binding.topToolbarSearchIcon.setImageResource(icon)
        binding.topToolbarSearchIcon.contentDescription = resources.getString(accessibilityString)
    }

    fun updateColors() {
        // sk18: ground, frame, text, hint and icons of the search bar are roles; its shape is built here
        val backgroundColor = context.uiColor(UiRole.TOOLBAR_GROUND)
        val density = resources.displayMetrics.density

        setBackgroundColor(backgroundColor)
        binding.topToolbarSearchIcon.applyColorFilter(context.uiColor(UiRole.SEARCH_ICON))
        binding.toolbarContainer.background = GradientDrawable().apply {
            cornerRadius = resources.getDimension(R.dimen.material_dialog_corner_radius)
            setColor(context.uiColor(UiRole.SEARCH_GROUND))
            setStroke((SEARCH_FRAME_DP * density).toInt().coerceAtLeast(1), context.uiColor(UiRole.SEARCH_FRAME))
        }
        binding.topToolbarSearch.setTextColor(context.uiColor(UiRole.SEARCH_TEXT))
        binding.topToolbarSearch.setHintTextColor(context.uiColor(UiRole.SEARCH_HINT))
        binding.topToolbarSearch.applyFieldCursorRoles()
        (context as? BaseSimpleActivity)?.updateTopBarColors(this, backgroundColor)
    }
}

private const val SEARCH_FRAME_DP = 1.25f
