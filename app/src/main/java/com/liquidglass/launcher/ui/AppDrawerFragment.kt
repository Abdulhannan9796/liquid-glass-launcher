package com.liquidglass.launcher.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liquidglass.launcher.R
import com.liquidglass.launcher.adapter.AppGridAdapter
import com.liquidglass.launcher.data.AppRepository
import com.liquidglass.launcher.data.PrefsManager
import com.liquidglass.launcher.model.AppInfo
import com.liquidglass.launcher.view.GlassPanel

class AppDrawerFragment : Fragment() {

    private lateinit var glassSearchBar: GlassPanel
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var foldersStrip: android.widget.LinearLayout
    private lateinit var adapter: AppGridAdapter

    private var allApps: List<AppInfo> = emptyList()

    var blurredWallpaper: Bitmap? = null
        set(value) {
            field = value
            if (::glassSearchBar.isInitialized) glassSearchBar.blurredWallpaper = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_app_drawer, container, false)

        glassSearchBar = root.findViewById(R.id.searchGlassPanel)
        glassSearchBar.cornerRadiusPx = 40f
        glassSearchBar.blurredWallpaper = blurredWallpaper

        searchEditText = root.findViewById(R.id.searchEditText)
        recyclerView = root.findViewById(R.id.appGrid)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        foldersStrip = root.findViewById(R.id.foldersStrip)

        allApps = AppRepository.loadAllApps(requireContext())
        adapter = AppGridAdapter(
            allApps,
            onClick = { app -> AppRepository.launch(requireContext(), app) },
            onLongClick = { app -> showAppOptions(app) }
        )
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                val filtered = if (query.isBlank()) {
                    allApps
                } else {
                    allApps.filter { it.label.contains(query, ignoreCase = true) }
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        refreshFolders()
        return root
    }

    private fun refreshFolders() {
        foldersStrip.removeAllViews()
        val folders = PrefsManager.getFolders(requireContext())
        folders.forEach { (name, keys) ->
            val chip = TextView(requireContext()).apply {
                text = "\uD83D\uDCC1 $name"
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(28, 16, 28, 16)
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 40f
                    setColor(0x33FFFFFF)
                }
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = 16
                layoutParams = lp
                setOnClickListener {
                    val folderApps = allApps.filter { keys.contains(it.componentKey) }
                    FolderDialogFragment(name, folderApps, blurredWallpaper) { app ->
                        AppRepository.launch(requireContext(), app)
                    }.show(childFragmentManager, "folder_$name")
                }
            }
            foldersStrip.addView(chip)
        }
    }

    private fun showAppOptions(app: AppInfo) {
        val options = arrayOf("Pin to Dock", "Add to Folder", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle(app.label)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pinToDock(app)
                    1 -> addToFolder(app)
                }
            }
            .show()
    }

    private fun pinToDock(app: AppInfo) {
        val keys = PrefsManager.getDockKeys(requireContext())
        if (!keys.contains(app.componentKey)) {
            keys.add(app.componentKey)
            PrefsManager.saveDockKeys(requireContext(), keys)
        }
        (activity as? com.liquidglass.launcher.LauncherActivity)?.refreshDock()
    }

    private fun addToFolder(app: AppInfo) {
        val folders = PrefsManager.getFolders(requireContext())
        val names = folders.keys.toMutableList()
        names.add("+ New Folder")
        AlertDialog.Builder(requireContext())
            .setTitle("Add to folder")
            .setItems(names.toTypedArray()) { _, which ->
                if (which == names.size - 1) {
                    promptNewFolderName(app)
                } else {
                    val folderName = names[which]
                    val list = folders[folderName] ?: mutableListOf()
                    if (!list.contains(app.componentKey)) list.add(app.componentKey)
                    folders[folderName] = list
                    PrefsManager.saveFolders(requireContext(), folders)
                    refreshFolders()
                }
            }
            .show()
    }

    private fun promptNewFolderName(app: AppInfo) {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("New folder name")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().ifBlank { "New Folder" }
                val folders = PrefsManager.getFolders(requireContext())
                val list = folders[name] ?: mutableListOf()
                if (!list.contains(app.componentKey)) list.add(app.componentKey)
                folders[name] = list
                PrefsManager.saveFolders(requireContext(), folders)
                refreshFolders()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
