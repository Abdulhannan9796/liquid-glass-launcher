package com.liquidglass.launcher

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.liquidglass.launcher.adapter.DockAdapter
import com.liquidglass.launcher.data.AppRepository
import com.liquidglass.launcher.data.PrefsManager
import com.liquidglass.launcher.model.AppInfo
import com.liquidglass.launcher.ui.AppDrawerFragment
import com.liquidglass.launcher.ui.TodayWidgetsFragment
import com.liquidglass.launcher.util.BlurUtil
import com.liquidglass.launcher.view.GlassPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dockGlassPanel: GlassPanel
    private lateinit var dockRecycler: RecyclerView
    private lateinit var dockAdapter: DockAdapter

    private var blurredWallpaper: Bitmap? = null
    private var allApps: List<AppInfo> = emptyList()

    private val todayFragment = TodayWidgetsFragment()
    private val drawerFragment = AppDrawerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NOTE: deliberately no fullscreen / immersive / nav-bar-hiding flags
        // here. The status bar, nav bar, and buttons/gestures behave exactly
        // as they normally would on this device — this activity never asks
        // the system to change them.
        setContentView(R.layout.activity_launcher)

        val wallpaperView = findViewById<ImageView>(R.id.wallpaperBackground)
        try {
            wallpaperView.setImageDrawable(WallpaperManager.getInstance(this).drawable)
        } catch (e: Exception) {
            wallpaperView.setBackgroundColor(0xFF1C1C1E.toInt())
        }

        viewPager = findViewById(R.id.viewPager)
        dockGlassPanel = findViewById(R.id.dockGlassPanel)
        dockGlassPanel.cornerRadiusPx = 64f
        dockRecycler = findViewById(R.id.dockRecycler)

        dockRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dockAdapter = DockAdapter(
            emptyList(),
            onClick = { app -> AppRepository.launch(this, app) },
            onLongClick = { app -> removeFromDock(app) }
        )
        dockRecycler.adapter = dockAdapter

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment =
                if (position == 0) todayFragment else drawerFragment
        }
        viewPager.setCurrentItem(1, false)

        loadAppsAndWallpaper()
    }

    private fun loadAppsAndWallpaper() {
        CoroutineScope(Dispatchers.Main).launch {
            allApps = withContext(Dispatchers.Default) {
                AppRepository.loadAllApps(this@LauncherActivity)
            }
            val metrics = resources.displayMetrics
            blurredWallpaper = withContext(Dispatchers.Default) {
                BlurUtil.blurredWallpaper(this@LauncherActivity, metrics.widthPixels, metrics.heightPixels)
            }
            dockGlassPanel.blurredWallpaper = blurredWallpaper
            todayFragment.blurredWallpaper = blurredWallpaper
            drawerFragment.blurredWallpaper = blurredWallpaper
            refreshDock()
        }
    }

    fun refreshDock() {
        val keys = PrefsManager.getDockKeys(this).toSet()
        dockAdapter.submitList(allApps.filter { keys.contains(it.componentKey) })
    }

    private fun removeFromDock(app: AppInfo) {
        val keys = PrefsManager.getDockKeys(this).toMutableList()
        keys.remove(app.componentKey)
        PrefsManager.saveDockKeys(this, keys)
        refreshDock()
    }
}
