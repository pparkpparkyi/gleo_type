package com.example.gleo_type.ui.gleohearts

/**
 * GleoHearts V13 UI-only contract.
 * This package intentionally does NOT import the current Media3, Pleos SDK,
 * event collectors, recommendation algorithms, JSONL storage or AppContainer.
 * Host integration must supply observed state and callbacks.
 */
data class GleoTrack(
    val musicId: String,
    val title: String,
    val artist: String,
    /** content://, file://, or android.resource:// URI supplied by the media source. */
    val artworkContentUri: String? = null,
    val durationMillis: Long? = null
)

data class GleoContext(
    val title: String,
    val detail: String?,
    /** True only for verified, actually observed signal. User utterance is not observation. */
    val observed: Boolean
)

enum class PlaybackStatus { IDLE, PLAYING, PAUSED, BUFFERING, ERROR }
enum class SyncStatus { LOCAL_ONLY, PENDING, CONFIRMED, ERROR }
enum class CarDestination { MUSIC, VOICE, REASONS, PIN, REVIEW, SAVING, SAVED, ERROR }
enum class MobileDestination { HOME, CALENDAR, MUSIC, PIN, MY, PLAYER, REASONS, REVIEW, PHOTO_EDITOR, HEARTS_DETAIL, ONBOARDING, LOGIN }
enum class GleoTab { HOME, CALENDAR, MUSIC, PIN, MY }

data class GleoDriveRecord(
    val driveSessionId: String,
    val title: String,
    val dateLabel: String,
    val locationLabel: String?,
    val listenedTracks: List<GleoTrack>,
    val photoContentUris: List<String> = emptyList(),
    val coverPhotoUri: String? = null,
    val memo: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)

/** No fake success: repository / MediaSession must explicitly update status. */
data class GleoCarUiState(
    val destination: CarDestination = CarDestination.MUSIC,
    val currentTrack: GleoTrack? = null,
    val playback: PlaybackStatus = PlaybackStatus.IDLE,
    val recommendationReason: String? = null,
    val context: List<GleoContext> = emptyList(),
    val voiceTranscript: String? = null,
    val voiceError: String? = null,
    val reviewTracks: List<GleoTrack> = emptyList(),
    val excludedMusicIds: Set<String> = emptySet(),
    val driveSessionId: String? = null,
    /** Only true when the host's live CarUxRestrictions allows parked review. */
    val canEditReview: Boolean = false,
    val pinSaveStatus: SyncStatus? = null
)

/**
 * Only call these after the host's driving restrictions permit the operation.
 * Never mark stored or synced merely because a callback was invoked.
 */
interface GleoCarActions {
    fun onPrevious()
    fun onPlayPause()
    fun onNext()
    fun onPinCurrent()
    fun onShowReasons()
    fun onShowMusic()
    fun onVoiceRequest()
    fun onOpenReview()
    fun onExcludeFromHearts(musicId: String)
    fun onUndoExclude(musicId: String)
    fun onRememberReviewedTracks(musicIds: List<String>)
}

data class GleoMobileUiState(
    val selectedTab: GleoTab = GleoTab.HOME,
    val selectedDestination: MobileDestination = MobileDestination.HOME,
    val recommendation: GleoTrack? = null,
    val playback: PlaybackStatus = PlaybackStatus.IDLE,
    val recommendationReason: String? = null,
    val records: List<GleoDriveRecord> = emptyList(),
    val upcomingLabel: String? = null,
    val upcomingMusicId: String? = null,
    val reviewTracks: List<GleoTrack> = emptyList(),
    val excludedMusicIds: Set<String> = emptySet(),
    val selectedRecordId: String? = null,
    val selectedDateLabel: String? = null,
    val canSavePhotos: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSignedIn: Boolean = false
)

interface GleoMobileActions {
    fun onSelectTab(tab: GleoTab)
    fun onPlayPause()
    fun onPrevious()
    fun onNext()
    fun onPinTrack(musicId: String)
    fun onSelectUpcomingTrack(musicId: String)
    fun onSelectDate(dateLabel: String)
    fun onSelectRecord(driveSessionId: String)
    fun onExcludeFromHearts(musicId: String)
    fun onUndoExclude(musicId: String)
    fun onSubmitReview(musicIds: List<String>)
    /**
     * Host must copy these Photo Picker URIs to app-private storage, then persist
     * metadata. URI access is not guaranteed to survive process death.
     * Does NOT imply successful save or cross-device sync.
     */
    fun onSaveHeartsPhotos(
        driveSessionId: String,
        photoUris: List<String>,
        coverPhotoUri: String?,
        memo: String
    )
    fun onLoginRequested()
    fun onLogoutRequested()
}

/** Hard gate: gear P or speed==0 is not a sufficient replacement for UXR. */
fun allowedReviewActions(state: GleoCarUiState): Boolean = state.canEditReview

fun tracksToRemember(
    tracks: List<GleoTrack>,
    excludedMusicIds: Set<String>
): List<String> = tracks.map { it.musicId }.filterNot { it in excludedMusicIds }
