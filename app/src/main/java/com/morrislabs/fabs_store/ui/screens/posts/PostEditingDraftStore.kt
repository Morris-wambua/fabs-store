package com.morrislabs.fabs_store.ui.screens.posts

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PostEditingState(
    val videoTrimStartMs: Long = 0L,
    val videoTrimEndMs: Long = 15_000L,
    val videoSpeed: Float = 1.0f,
    val previewPositionMs: Long = 0L,
    val filterName: String = "None",
    val textOverlays: List<String> = emptyList(),
    val emojiOverlays: List<String> = emptyList()
)

object PostEditingDraftStore {
    private const val PREF_NAME = "post_editing_drafts"
    private const val KEY_DRAFT = "draft_store"

    fun save(context: Context, state: PostEditingState) {
        val json = JSONObject().apply {
            put("videoTrimStartMs", state.videoTrimStartMs)
            put("videoTrimEndMs", state.videoTrimEndMs)
            put("videoSpeed", state.videoSpeed.toDouble())
            put("previewPositionMs", state.previewPositionMs)
            put("filterName", state.filterName)
            put("textOverlays", JSONArray(state.textOverlays))
            put("emojiOverlays", JSONArray(state.emojiOverlays))
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DRAFT, json.toString())
            .apply()
    }

    fun load(context: Context): PostEditingState? {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DRAFT, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            PostEditingState(
                videoTrimStartMs = json.optLong("videoTrimStartMs", 0L),
                videoTrimEndMs = json.optLong("videoTrimEndMs", 15_000L),
                videoSpeed = json.optDouble("videoSpeed", 1.0).toFloat(),
                previewPositionMs = json.optLong("previewPositionMs", 0L),
                filterName = json.optString("filterName", "None"),
                textOverlays = jsonArrayToList(json.optJSONArray("textOverlays")),
                emojiOverlays = jsonArrayToList(json.optJSONArray("emojiOverlays"))
            )
        }.getOrNull()
    }

    private fun jsonArrayToList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return List(array.length()) { index -> array.optString(index) }.filter { it.isNotBlank() }
    }
}
