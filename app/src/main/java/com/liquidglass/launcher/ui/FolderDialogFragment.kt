package com.liquidglass.launcher.ui

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liquidglass.launcher.R
import com.liquidglass.launcher.adapter.AppGridAdapter
import com.liquidglass.launcher.model.AppInfo
import com.liquidglass.launcher.view.GlassPanel

class FolderDialogFragment(
    private val folderName: String,
    private val apps: List<AppInfo>,
    private val blurredWallpaper: Bitmap?,
    private val onAppClick: (AppInfo) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_folder, null)

        val glass = view.findViewById<GlassPanel>(R.id.folderGlassPanel)
        glass.cornerRadiusPx = 56f
        glass.blurredWallpaper = blurredWallpaper

        view.findViewById<android.widget.TextView>(R.id.folderTitle).text = folderName

        val recycler = view.findViewById<RecyclerView>(R.id.folderGrid)
        recycler.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler.adapter = AppGridAdapter(
            apps,
            onClick = { app ->
                onAppClick(app)
                dismiss()
            },
            onLongClick = {}
        )

        return AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
            .setView(view)
            .create()
    }
}
