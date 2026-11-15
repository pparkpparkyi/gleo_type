package com.example.gleo_type.ui.gleohearts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Android Studio design-only previews; never used as a production data source. */
private object PreviewCarActions : GleoCarActions {
    override fun onPrevious() = Unit
    override fun onPlayPause() = Unit
    override fun onNext() = Unit
    override fun onPinCurrent() = Unit
    override fun onShowReasons() = Unit
    override fun onShowMusic() = Unit
    override fun onVoiceRequest() = Unit
    override fun onOpenReview() = Unit
    override fun onExcludeFromHearts(musicId: String) = Unit
    override fun onUndoExclude(musicId: String) = Unit
    override fun onRememberReviewedTracks(musicIds: List<String>) = Unit
}

private object PreviewMobileActions : GleoMobileActions {
    override fun onSelectTab(tab: GleoTab) = Unit
    override fun onPlayPause() = Unit
    override fun onPrevious() = Unit
    override fun onNext() = Unit
    override fun onPinTrack(musicId: String) = Unit
    override fun onSelectUpcomingTrack(musicId: String) = Unit
    override fun onSelectDate(dateLabel: String) = Unit
    override fun onSelectRecord(driveSessionId: String) = Unit
    override fun onExcludeFromHearts(musicId: String) = Unit
    override fun onUndoExclude(musicId: String) = Unit
    override fun onSubmitReview(musicIds: List<String>) = Unit
    override fun onSaveHeartsPhotos(driveSessionId: String, photoUris: List<String>, coverPhotoUri: String?, memo: String) = Unit
    override fun onLoginRequested() = Unit
    override fun onLogoutRequested() = Unit
}

@Preview(name = "GleoHearts Pleos app window", showBackground = true, widthDp = 850, heightDp = 580)
@Composable
private fun CarDesignPreview() {
    GleoCarRoot(
        state = GleoCarUiState(
            currentTrack = GleoTrack("preview-track-1", "Super Shy", "NewJeans"),
            recommendationReason = "기분 좋은 드라이브를 위한 음악",
            playback = PlaybackStatus.PLAYING
        ),
        actions = PreviewCarActions,
        modifier = Modifier
    )
}

@Preview(name = "GleoHearts Mobile", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun MobileDesignPreview() {
    GleoMobileRoot(
        state = GleoMobileUiState(),
        actions = PreviewMobileActions,
        modifier = Modifier
    )
}
