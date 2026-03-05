package com.morrislabs.fabs_store.ui.screens.posts.createflow

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.data.model.SoundDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreatePostFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val _draft = MutableStateFlow(CreatePostDraft())
    val draft: StateFlow<CreatePostDraft> = _draft.asStateFlow()

    private val _timerCountdown = MutableStateFlow<Int?>(null)
    val timerCountdown: StateFlow<Int?> = _timerCountdown.asStateFlow()

    fun setMediaUri(uri: Uri, type: PostType) {
        _draft.update { it.copy(mediaUri = uri, postType = type) }
    }

    fun setDurationMode(mode: DurationMode) {
        _draft.update { it.copy(durationMode = mode) }
    }

    fun setRecordingSpeed(speed: Float) {
        _draft.update { it.copy(recordingSpeed = speed) }
    }

    fun toggleFlash() {
        _draft.update { it.copy(flashEnabled = !it.flashEnabled) }
    }

    fun toggleCamera() {
        _draft.update { it.copy(useFrontCamera = !it.useFrontCamera) }
    }

    fun setTrimRange(startMs: Long, endMs: Long) {
        _draft.update { it.copy(trim = TrimSettings(startMs, endMs)) }
    }

    fun setAspectRatio(ratio: AspectRatioMode) {
        _draft.update { it.copy(crop = CropSettings(ratio)) }
    }

    fun setFilter(name: String, intensity: Float) {
        _draft.update { it.copy(filter = FilterSettings(name, intensity)) }
    }

    fun setSound(sound: SoundDTO, startMs: Long = 0, endMs: Long = sound.duration) {
        _draft.update { it.copy(soundSelection = SoundSelection(sound, startMs, endMs)) }
    }

    fun clearSound() {
        _draft.update { it.copy(soundSelection = null) }
    }

    fun addOverlay(item: OverlayItem) {
        _draft.update { it.copy(overlays = it.overlays + item) }
    }

    fun updateOverlay(updated: OverlayItem) {
        _draft.update { draft ->
            draft.copy(overlays = draft.overlays.map { if (it.id == updated.id) updated else it })
        }
    }

    fun removeOverlay(id: String) {
        _draft.update { it.copy(overlays = it.overlays.filter { o -> o.id != id }) }
    }

    fun setCaption(caption: String) {
        _draft.update { it.copy(caption = caption) }
    }

    fun setTaggedStore(info: TaggedStoreInfo?) {
        _draft.update { it.copy(taggedStore = info) }
    }

    fun setTimerCountdown(seconds: Int?) {
        _timerCountdown.value = seconds
    }

    fun resetDraft() {
        _draft.value = CreatePostDraft()
        _timerCountdown.value = null
    }
}
