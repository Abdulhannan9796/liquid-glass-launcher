package com.liquidglass.launcher.ui

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.liquidglass.launcher.R
import com.liquidglass.launcher.data.PrefsManager
import com.liquidglass.launcher.view.GlassPanel

class TodayWidgetsFragment : Fragment() {

    companion object {
        private const val HOST_ID = 1024
        private const val REQUEST_PICK_WIDGET = 9001
        private const val REQUEST_CREATE_WIDGET = 9002
    }

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var widgetContainer: LinearLayout
    private lateinit var glassPanel: GlassPanel

    var blurredWallpaper: Bitmap? = null
        set(value) {
            field = value
            if (::glassPanel.isInitialized) glassPanel.blurredWallpaper = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_today_widgets, container, false)

        glassPanel = root.findViewById(R.id.todayGlassPanel)
        glassPanel.cornerRadiusPx = 56f
        glassPanel.blurredWallpaper = blurredWallpaper
        widgetContainer = root.findViewById(R.id.widgetContainer)

        appWidgetManager = AppWidgetManager.getInstance(requireContext())
        appWidgetHost = AppWidgetHost(requireContext(), HOST_ID)

        root.findViewById<View>(R.id.addWidgetButton).setOnClickListener { pickWidget() }

        restoreSavedWidgets()
        return root
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    private fun pickWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        startActivityForResult(pickIntent, REQUEST_PICK_WIDGET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return
        if (appWidgetId == -1) return

        when (requestCode) {
            REQUEST_PICK_WIDGET -> {
                val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
                if (info?.configure != null) {
                    val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                        component = info.configure
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    startActivityForResult(configIntent, REQUEST_CREATE_WIDGET)
                } else {
                    addWidgetView(appWidgetId)
                }
            }
            REQUEST_CREATE_WIDGET -> addWidgetView(appWidgetId)
        }
    }

    private fun addWidgetView(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        val hostView = appWidgetHost.createView(requireContext(), appWidgetId, info)
        val density = resources.displayMetrics.density
        val heightPx = (info.minHeight * density).toInt()
        widgetContainer.addView(
            hostView,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
        )
        saveWidgetId(appWidgetId)
    }

    private fun saveWidgetId(id: Int) {
        val ids = PrefsManager.getWidgetIds(requireContext()).toMutableList()
        if (!ids.contains(id)) ids.add(id)
        PrefsManager.saveWidgetIds(requireContext(), ids)
    }

    private fun restoreSavedWidgets() {
        PrefsManager.getWidgetIds(requireContext()).forEach { id ->
            if (appWidgetManager.getAppWidgetInfo(id) != null) {
                addWidgetView(id)
            }
        }
    }
}
