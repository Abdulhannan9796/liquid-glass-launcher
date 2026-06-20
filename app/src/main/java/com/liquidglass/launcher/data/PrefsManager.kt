package com.liquidglass.launcher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PrefsManager {
    private const val PREFS = "liquid_glass_prefs"
    private const val KEY_DOCK = "dock_apps"
    private const val KEY_FOLDERS = "folders"
    private const val KEY_WIDGETS = "widget_ids"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---------- Dock ----------

    fun getDockKeys(context: Context): MutableList<String> {
        val raw = prefs(context).getString(KEY_DOCK, null) ?: return mutableListOf()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getString(it) }.toMutableList()
    }

    fun saveDockKeys(context: Context, keys: List<String>) {
        val arr = JSONArray()
        keys.forEach { arr.put(it) }
        prefs(context).edit().putString(KEY_DOCK, arr.toString()).apply()
    }

    // ---------- Folders ----------

    fun getFolders(context: Context): MutableMap<String, MutableList<String>> {
        val raw = prefs(context).getString(KEY_FOLDERS, null) ?: return mutableMapOf()
        val obj = JSONObject(raw)
        val result = mutableMapOf<String, MutableList<String>>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val name = keys.next()
            val arr = obj.getJSONArray(name)
            result[name] = (0 until arr.length()).map { arr.getString(it) }.toMutableList()
        }
        return result
    }

    fun saveFolders(context: Context, folders: Map<String, List<String>>) {
        val obj = JSONObject()
        folders.forEach { (name, keys) ->
            val arr = JSONArray()
            keys.forEach { arr.put(it) }
            obj.put(name, arr)
        }
        prefs(context).edit().putString(KEY_FOLDERS, obj.toString()).apply()
    }

    // ---------- Widgets ----------

    fun getWidgetIds(context: Context): List<Int> {
        val raw = prefs(context).getString(KEY_WIDGETS, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getInt(it) }
    }

    fun saveWidgetIds(context: Context, ids: List<Int>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs(context).edit().putString(KEY_WIDGETS, arr.toString()).apply()
    }
}
