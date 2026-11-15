package com.example.gleo_type.ui.gleohearts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Optional mapContent must be provided by the host's approved maps integration.
 * We do not draw fake GPS routes or assume permission / location availability.
 * The same Kotlin screen can be hosted by a separate mobile Android target.
 */
@Composable
fun GleoMobileRoot(
    state: GleoMobileUiState,
    actions: GleoMobileActions,
    modifier: Modifier = Modifier,
    mapContent: (@Composable (Modifier) -> Unit)? = null
) {
    var tab by remember { mutableStateOf(state.selectedTab) }
    var destination by remember { mutableStateOf(state.selectedDestination) }
    var selectedRecordId by remember { mutableStateOf(state.selectedRecordId) }
    LaunchedEffect(state.selectedTab) { tab = state.selectedTab }
    LaunchedEffect(state.selectedDestination) { destination = state.selectedDestination }
    LaunchedEffect(state.selectedRecordId) {
        if (state.selectedRecordId != null) selectedRecordId = state.selectedRecordId
    }
    val selectedRecord = state.records.firstOrNull { it.driveSessionId == selectedRecordId }
        ?: state.records.firstOrNull()
    val navigateTab: (GleoTab) -> Unit = { next ->
        tab = next
        destination = when (next) {
            GleoTab.HOME -> MobileDestination.HOME
            GleoTab.CALENDAR -> MobileDestination.CALENDAR
            GleoTab.MUSIC -> MobileDestination.MUSIC
            GleoTab.PIN -> MobileDestination.PIN
            GleoTab.MY -> MobileDestination.MY
        }
        actions.onSelectTab(next)
    }
    GleoHeartsTheme {
        Column(modifier = modifier.fillMaxSize().background(Grad)) {
            Box(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 20.dp)) {
                    when (destination) {
                        MobileDestination.HOME -> MobileHome(state, mapContent,
                            onRecord = { record -> selectedRecordId = record.driveSessionId; actions.onSelectRecord(record.driveSessionId); destination = MobileDestination.HEARTS_DETAIL },
                            onReview = { destination = MobileDestination.REVIEW })
                        MobileDestination.CALENDAR -> MobileCalendar(state, actions,
                            onRecord = { record -> selectedRecordId = record.driveSessionId; actions.onSelectRecord(record.driveSessionId); destination = MobileDestination.HEARTS_DETAIL })
                        MobileDestination.MUSIC -> MobileMusic(state, actions,
                            onPlayer = { destination = MobileDestination.PLAYER },
                            onReasons = { destination = MobileDestination.REASONS })
                        MobileDestination.PIN -> MobilePin(state.records,
                            onRecord = { record -> selectedRecordId = record.driveSessionId; actions.onSelectRecord(record.driveSessionId); destination = MobileDestination.HEARTS_DETAIL })
                        MobileDestination.MY -> MobileMy(state, actions, onLogin = { destination = MobileDestination.LOGIN })
                        MobileDestination.PLAYER -> MobilePlayer(state, actions, onBack = { destination = MobileDestination.MUSIC })
                        MobileDestination.REASONS -> MobileReasons(state, onBack = { destination = MobileDestination.MUSIC })
                        MobileDestination.REVIEW -> MobileReview(state, actions, onPhotos = { destination = MobileDestination.PHOTO_EDITOR })
                        MobileDestination.PHOTO_EDITOR -> MobilePhotos(selectedRecord, state, actions,
                            onBack = { destination = MobileDestination.REVIEW })
                        MobileDestination.HEARTS_DETAIL -> HeartsDetail(selectedRecord,
                            onEditPhotos = { destination = MobileDestination.PHOTO_EDITOR },
                            onBack = { navigateTab(GleoTab.PIN) })
                        MobileDestination.ONBOARDING -> MobileOnboarding(onContinue = { destination = MobileDestination.LOGIN })
                        MobileDestination.LOGIN -> MobileLogin(actions, onGuest = { navigateTab(GleoTab.HOME) })
                    }
                }
            }
            MobileTabBar(selected = tab, onSelect = navigateTab)
        }
    }
}

@Composable
private fun MobileTabBar(selected: GleoTab, onSelect: (GleoTab) -> Unit) {
    val names = listOf(
        GleoTab.HOME to "홈", GleoTab.CALENDAR to "캘린더", GleoTab.MUSIC to "음악",
        GleoTab.PIN to "pin!", GleoTab.MY to "마이"
    )
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF242239), RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp))
            .padding(horizontal = 7.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        names.forEach { (tab, label) ->
            Column(
                modifier = Modifier.weight(1f).clickable { onSelect(tab) }.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when (tab) { GleoTab.HOME -> "⌂"; GleoTab.CALENDAR -> "▦"; GleoTab.MUSIC -> "♫"; GleoTab.PIN -> "♧"; GleoTab.MY -> "♙" },
                    color = if (selected == tab) Lavender else Subtle,
                    fontSize = 24.sp
                )
                Text(label, color = if (selected == tab) Pale else Subtle, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MobileHome(
    state: GleoMobileUiState,
    mapContent: (@Composable (Modifier) -> Unit)?,
    onRecord: (GleoDriveRecord) -> Unit,
    onReview: () -> Unit
) {
    GleoHeader("GleoHearts", "Your Drive. Your Music. Your Story.")
    Spacer(Modifier.height(15.dp))
    Box(Modifier.fillMaxWidth().height(270.dp).clip(RoundedCornerShape(22.dp)).background(Midnight)) {
        if (mapContent == null) {
            CenteredEmpty(if (state.records.isEmpty()) "저장된 장소가 없어요" else "저장된 장소 ${state.records.size}곳", Modifier.fillMaxSize())
        } else mapContent(Modifier.fillMaxSize())
    }
    Spacer(Modifier.height(16.dp))
    GleoHeader("오늘의 드라이브", "이동 중 만난 음악과 장소")
    Spacer(Modifier.height(10.dp))
    if (state.reviewTracks.isNotEmpty()) {
        GlassPanel(Modifier.fillMaxWidth().clickable(onClick = onReview)) {
            Text("주행 후 음악 정리", color = Pale, fontWeight = FontWeight.Bold)
            Text("오늘 들었던 ${state.reviewTracks.size}곡 확인하기", color = Subtle)
        }
        Spacer(Modifier.height(12.dp))
    }
    state.records.take(3).forEach { record ->
        RecordCard(record, onClick = { onRecord(record) }); Spacer(Modifier.height(10.dp))
    }
    if (state.records.isEmpty() && state.reviewTracks.isEmpty()) CenteredEmpty("드라이브를 시작하면 기록을 볼 수 있어요")
}

@Composable
private fun RecordCard(record: GleoDriveRecord, onClick: () -> Unit) {
    GlassPanel(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GleoArtwork(record.coverPhotoUri, record.title, Modifier.size(92.dp), placeholder = "♡")
            Column(Modifier.weight(1f)) {
                Text(record.title, color = Pale, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(record.dateLabel, color = Subtle, fontSize = 12.sp)
                Text("${record.listenedTracks.size}곡 · 사진 ${record.photoContentUris.size}장", color = Lavender, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MobileCalendar(
    state: GleoMobileUiState,
    actions: GleoMobileActions,
    onRecord: (GleoDriveRecord) -> Unit
) {
    GleoHeader("캘린더", "날짜에 담긴 음악과 드라이브")
    Spacer(Modifier.height(14.dp))
    if (state.upcomingLabel != null) {
        GlassPanel(Modifier.fillMaxWidth()) {
            Text(state.upcomingLabel, color = Lavender, fontWeight = FontWeight.Bold)
            Text("출발 전에 음악을 미리 선택해 보세요", color = Subtle)
            Spacer(Modifier.height(10.dp))
            val option = state.recommendation
            if (option == null) Text("선택 가능한 음악이 없어요", color = Subtle)
            else GlassButton("${option.title} 선택", onClick = { actions.onSelectUpcomingTrack(option.musicId) })
        }
        Spacer(Modifier.height(18.dp))
    }
    Text("지난 주행 기록", color = Pale, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    if (state.records.isEmpty()) CenteredEmpty("아직 주행 기록이 없어요")
    state.records.forEach { record ->
        RecordCard(record, onClick = { actions.onSelectDate(record.dateLabel); onRecord(record) })
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun MobileMusic(
    state: GleoMobileUiState,
    actions: GleoMobileActions,
    onPlayer: () -> Unit,
    onReasons: () -> Unit
) {
    GleoHeader("AI CURATED", "나에게 어울리는 드라이브 음악")
    Spacer(Modifier.height(17.dp))
    val track = state.recommendation
    if (track == null) {
        CenteredEmpty("추천할 음악이 아직 없어요")
        return
    }
    GleoArtwork(track.artworkContentUri, track.title, Modifier.fillMaxWidth().height(290.dp))
    Spacer(Modifier.height(15.dp))
    Text(track.title, color = Pale, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    Text(track.artist, color = Subtle)
    Spacer(Modifier.height(12.dp))
    if (!state.recommendationReason.isNullOrBlank()) Text(state.recommendationReason, color = Lavender)
    Spacer(Modifier.height(18.dp))
    GlassButton(if (state.playback == PlaybackStatus.PLAYING) "일시정지" else "재생", modifier = Modifier.fillMaxWidth(), onClick = actions::onPlayPause)
    Spacer(Modifier.height(8.dp))
    GlassButton("음악 자세히 보기", modifier = Modifier.fillMaxWidth(), onClick = onPlayer)
    Spacer(Modifier.height(8.dp))
    GlassButton("추천 이유", modifier = Modifier.fillMaxWidth(), onClick = onReasons)
    Spacer(Modifier.height(8.dp))
    GlassButton("♡ pin! 저장", modifier = Modifier.fillMaxWidth(), onClick = { actions.onPinTrack(track.musicId) })
}

@Composable
private fun MobilePlayer(state: GleoMobileUiState, actions: GleoMobileActions, onBack: () -> Unit) {
    GleoHeader("지금 재생 중", state.recommendation?.artist)
    Spacer(Modifier.height(18.dp))
    GleoArtwork(state.recommendation?.artworkContentUri, state.recommendation?.title ?: "음악", Modifier.fillMaxWidth().height(310.dp))
    Spacer(Modifier.height(20.dp))
    Text(state.recommendation?.title ?: "현재 음악이 없어요", color = Pale, fontSize = 24.sp)
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassButton("◀◀", modifier = Modifier.weight(1f), onClick = actions::onPrevious)
        GlassButton(if (state.playback == PlaybackStatus.PLAYING) "Ⅱ" else "▶", modifier = Modifier.weight(1f), onClick = actions::onPlayPause)
        GlassButton("▶▶", modifier = Modifier.weight(1f), onClick = actions::onNext)
    }
    Spacer(Modifier.height(14.dp))
    GlassButton("음악 목록", modifier = Modifier.fillMaxWidth(), onClick = onBack)
}

@Composable
private fun MobileReasons(state: GleoMobileUiState, onBack: () -> Unit) {
    GleoHeader("추천 이유", "음악을 제안한 이유")
    Spacer(Modifier.height(14.dp))
    GlassPanel(Modifier.fillMaxWidth()) { Text(state.recommendationReason ?: "추천 정보가 아직 없어요", color = Pale) }
    Spacer(Modifier.height(18.dp))
    GlassButton("음악 화면으로", onClick = onBack)
}

@Composable
private fun MobilePin(records: List<GleoDriveRecord>, onRecord: (GleoDriveRecord) -> Unit) {
    GleoHeader("pin!", "음악이 머문 순간들")
    Spacer(Modifier.height(16.dp))
    if (records.isEmpty()) CenteredEmpty("아직 저장한 Hearts가 없어요")
    records.forEach { record ->
        RecordCard(record) { onRecord(record) }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun HeartsDetail(record: GleoDriveRecord?, onEditPhotos: () -> Unit, onBack: () -> Unit) {
    GleoHeader(record?.title ?: "Hearts", record?.dateLabel)
    Spacer(Modifier.height(12.dp))
    if (record == null) { CenteredEmpty("기록이 없어요"); GlassButton("pin!으로", onClick = onBack); return }
    GleoArtwork(record.coverPhotoUri, record.title, Modifier.fillMaxWidth().height(275.dp), placeholder = "♡")
    Spacer(Modifier.height(14.dp))
    Text(record.locationLabel ?: "이동 기록", color = Lavender)
    if (record.memo.isNotBlank()) Text(record.memo, color = Pale, modifier = Modifier.padding(vertical = 12.dp))
    Text("음악 ${record.listenedTracks.size}곡 · 사진 ${record.photoContentUris.size}장", color = Subtle)
    Spacer(Modifier.height(14.dp))
    record.listenedTracks.forEach { Text("♫  ${it.title} · ${it.artist}", color = Pale, modifier = Modifier.padding(vertical = 4.dp)) }
    Spacer(Modifier.height(14.dp))
    GlassButton("사진 추가 · 대표 사진 변경", modifier = Modifier.fillMaxWidth(), onClick = onEditPhotos)
    Spacer(Modifier.height(8.dp))
    GlassButton("목록으로", modifier = Modifier.fillMaxWidth(), onClick = onBack)
}

@Composable
private fun MobileReview(state: GleoMobileUiState, actions: GleoMobileActions, onPhotos: () -> Unit) {
    GleoHeader("주행 종료 Review", "오늘 주행에서 들었던 음악")
    Spacer(Modifier.height(14.dp))
    val included = tracksToRemember(state.reviewTracks, state.excludedMusicIds)
    if (state.reviewTracks.isEmpty()) CenteredEmpty("정리할 음악이 없어요")
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        state.reviewTracks.forEach { track ->
            val excluded = track.musicId in state.excludedMusicIds
            var verticalDrag by remember(track.musicId) { mutableFloatStateOf(0f) }
            GlassPanel(Modifier.width(218.dp).pointerInput(track.musicId, excluded) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dy -> change.consume(); verticalDrag = (verticalDrag + dy).coerceIn(-140f, 0f) },
                    onDragEnd = {
                        if (verticalDrag < -70f && !excluded) actions.onExcludeFromHearts(track.musicId)
                        verticalDrag = 0f
                    },
                    onDragCancel = { verticalDrag = 0f }
                )
            }) {
                GleoArtwork(track.artworkContentUri, track.title, Modifier.fillMaxWidth().height(162.dp))
                Spacer(Modifier.height(8.dp))
                Text(track.title, fontWeight = FontWeight.Bold, color = Pale, maxLines = 1)
                Text(track.artist, color = Subtle, fontSize = 12.sp)
                Text(if (excluded) "제외됨" else "↑ 위로 올리면 제외", color = if (excluded) Destructive else Lavender, fontSize = 12.sp)
                if (excluded) GlassButton("되돌리기", onClick = { actions.onUndoExclude(track.musicId) })
            }
        }
    }
    Spacer(Modifier.height(22.dp))
    GlassButton("Hearts에 기억하기 (${included.size}곡)", enabled = included.isNotEmpty(), modifier = Modifier.fillMaxWidth(), onClick = {
        actions.onSubmitReview(included)
        // Caller must change state when storage ACK is real. No navigation here.
    })
    Spacer(Modifier.height(9.dp))
    GlassButton("사진 추가하기", modifier = Modifier.fillMaxWidth(), onClick = onPhotos)
}

@Composable
private fun MobilePhotos(
    record: GleoDriveRecord?,
    state: GleoMobileUiState,
    actions: GleoMobileActions,
    onBack: () -> Unit
) {
    GleoHeader("오늘의 사진도 기억할까요?", "사진과 음악을 하나의 Hearts로")
    Spacer(Modifier.height(14.dp))
    var photos by remember(record?.driveSessionId) { mutableStateOf<List<Uri>>(emptyList()) }
    var cover by remember(record?.driveSessionId) { mutableStateOf<Uri?>(null) }
    var memo by remember(record?.driveSessionId) { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { result ->
        photos = result
        cover = result.firstOrNull()
    }
    if (record == null) {
        CenteredEmpty("사진을 추가할 주행 기록을 선택해 주세요")
        GlassButton("돌아가기", onClick = onBack)
        return
    }
    if (photos.isNotEmpty()) {
        GleoArtwork(cover?.toString(), "대표 사진", Modifier.fillMaxWidth().height(240.dp), placeholder = "♡")
        Spacer(Modifier.height(8.dp))
        Text("대표 사진 선택", color = Pale, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            photos.forEach { uri ->
                Column(Modifier.width(99.dp).clickable { cover = uri }) {
                    GleoArtwork(uri.toString(), "선택한 사진", Modifier.fillMaxWidth().height(92.dp), placeholder = "♡")
                    Text(if (uri == cover) "✓ 대표 사진" else "대표로 선택", color = Lavender, fontSize = 11.sp)
                }
            }
        }
    } else {
        GlassPanel(Modifier.fillMaxWidth()) {
            CenteredEmpty("휴대폰에서 남기고 싶은 사진을 선택하세요")
        }
    }
    Spacer(Modifier.height(14.dp))
    GlassButton("사진 추가하기", modifier = Modifier.fillMaxWidth(), onClick = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    })
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = memo,
        onValueChange = { memo = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("추억 메모") },
        minLines = 2,
        maxLines = 4
    )
    Spacer(Modifier.height(12.dp))
    if (state.saveError != null) Text(state.saveError, color = Destructive)
    if (state.isSaving) {
        CenteredEmpty("사진과 음악을 기록하고 있어요")
    } else {
        GlassButton(
            "Hearts에 저장",
            enabled = state.canSavePhotos,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                actions.onSaveHeartsPhotos(
                    driveSessionId = record.driveSessionId,
                    photoUris = photos.map(Uri::toString),
                    coverPhotoUri = cover?.toString(),
                    memo = memo
                )
                // No fake success message; actual repository completion drives new state.
            }
        )
    }
    Spacer(Modifier.height(8.dp))
    GlassButton("사진 없이 나중에 정리", modifier = Modifier.fillMaxWidth(), onClick = onBack)
}

@Composable
private fun MobileMy(state: GleoMobileUiState, actions: GleoMobileActions, onLogin: () -> Unit) {
    GleoHeader("마이", "취향과 소중한 기록을 관리해요")
    Spacer(Modifier.height(16.dp))
    GlassPanel(Modifier.fillMaxWidth()) {
        Text(if (state.isSignedIn) "GleoHearts 계정" else "게스트", fontWeight = FontWeight.Bold, color = Pale)
        Text("음악 취향 · 기록 · 동기화 상태", color = Subtle)
    }
    Spacer(Modifier.height(16.dp))
    Text("Hearts 기록 ${state.records.size}개", color = Lavender)
    Spacer(Modifier.height(12.dp))
    if (!state.isSignedIn) GlassButton("로그인", modifier = Modifier.fillMaxWidth(), onClick = onLogin)
    else GlassButton("로그아웃", modifier = Modifier.fillMaxWidth(), onClick = actions::onLogoutRequested)
    Spacer(Modifier.height(10.dp))
    Text("사진 및 위치 권한은 기기 설정에서 변경할 수 있어요", color = Subtle, fontSize = 13.sp)
}

@Composable
private fun MobileOnboarding(onContinue: () -> Unit) {
    GleoHeader("GleoHearts", "나의 음악과 이동이 만나는 곳")
    Spacer(Modifier.height(36.dp))
    GlassPanel(Modifier.fillMaxWidth()) {
        Text("처음 만나는 GleoHearts", color = Pale, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(18.dp))
        Text("출발 전 · 음악을 준비하고", color = Lavender)
        Text("주행 중 · 운전에 집중하며 듣고", color = Lavender)
        Text("주행 후 · Hearts에 추억을 남겨요", color = Lavender)
    }
    Spacer(Modifier.height(28.dp))
    GlassButton("시작하기", modifier = Modifier.fillMaxWidth(), onClick = onContinue)
}

@Composable
private fun MobileLogin(actions: GleoMobileActions, onGuest: () -> Unit) {
    GleoHeader("GleoHearts", "음악과 길의 기억을 이어보세요")
    Spacer(Modifier.height(28.dp))
    GlassPanel(Modifier.fillMaxWidth()) {
        Text("로그인", fontWeight = FontWeight.Bold, color = Pale, fontSize = 24.sp)
        Spacer(Modifier.height(15.dp))
        Text("계정 연동을 통해 다른 기기의 기록도 볼 수 있어요", color = Subtle)
        Spacer(Modifier.height(15.dp))
        GlassButton("로그인", modifier = Modifier.fillMaxWidth(), onClick = actions::onLoginRequested)
    }
    Spacer(Modifier.height(20.dp))
    GlassButton("게스트로 둘러보기", modifier = Modifier.fillMaxWidth(), onClick = onGuest)
}
