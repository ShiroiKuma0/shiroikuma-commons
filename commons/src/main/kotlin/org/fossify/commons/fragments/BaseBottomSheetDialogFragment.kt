package org.fossify.commons.fragments

import org.fossify.commons.extensions.baseConfig
import android.graphics.drawable.GradientDrawable
import org.fossify.commons.helpers.uiColor
import org.fossify.commons.helpers.UiRole
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.fossify.commons.R
import org.fossify.commons.databinding.DialogBottomSheetBinding
import org.fossify.commons.extensions.*

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DialogBottomSheetBinding.inflate(inflater, container, false)
        val context = requireContext()

        // sk18: a sheet is a dialog — its ground and frame are the dialog roles, the shape is built here
        val radius = context.resources.getDimension(R.dimen.bottom_sheet_corner_radius)
        val density = context.resources.displayMetrics.density
        view.root.background = GradientDrawable().apply {
            cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            setColor(context.uiColor(UiRole.DIALOG_GROUND))
            setStroke(
                (context.baseConfig.dialogBorderWidth * density).toInt(),
                context.uiColor(UiRole.DIALOG_FRAME),
            )
        }
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getInt(BOTTOM_SHEET_TITLE).takeIf { it != 0 }
        DialogBottomSheetBinding.bind(view).apply {
            bottomSheetTitle.setTextColor(view.context.uiColor(UiRole.DIALOG_TITLE))
            bottomSheetTitle.setTextOrBeGone(title)
            setupContentView(bottomSheetContentHolder)
        }
    }

    abstract fun setupContentView(parent: ViewGroup)

    companion object {
        const val BOTTOM_SHEET_TITLE = "title_string"
    }
}
