package com.example.gleo_type.ui.gleohearts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * APP-WINDOW ONLY. Do not set system bars, Driving View, GNB or orientation here.
 * Host must put this in the exact Pleos-provided Activity/window bounds.
 */
@Composable
fun GleoCarRoot(
    state: GleoCarUiState,
    actions: GleoCarActions,
    modifier: Modifier = Modifier
) {
    GleoHeartsTheme {
        BoxWithConstraints(modifier = modifier.fillMaxSize().background(Midnight)) {
            val compact = maxWidth < 610.dp
            Column(
                modifier = Modifier.fillMaxSize().background(Grad)
                    .padding(if (compact) 16.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TinyStateLabel("♡ GleoHearts")
                    Spacer(Modifier.weight(1f))
                    TinyStateLabel(if (state.canEditReview) "주행 종료" else "주행 중 음악")
                }
                when (state.destination) {
                    CarDestination.MUSIC -> CarPlayer(state, actions, compact)
                    CarDestination.VOICE -> CarVoice(state, actions)
                    CarDestination.REASONS -> CarReasons(state, actions)
                    CarDestination.PIN -> CarPin(state, actions)
                    CarDestination.REVIEW -> {
                        if (allowedReviewActions(state)) CarReview(state, actions)
                        else CarRestricted(actions)
                    }
                    CarDestination.SAVING -> CarSaving()
                    CarDestination.SAVED -> CarSaved(state, actions)
                    CarDestination.ERROR -> CarFailure(state, actions)
                }
            }
        }
    }
}

@Composable
private fun CarPlayer(state: GleoCarUiState, actions: GleoCarActions, compact: Boolean) {
    val track = state.currentTrack
    GleoHeader("Drive with music", state.recommendationReason ?: "주행에 어울리는 음악을 만나보세요")
    if (track == null) {
        GlassPanel(Modifier.fillMaxWidth()) {
            CenteredEmpty("현재 재생 중인 음악이 없어요")
        }
    } else if (compact) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            GleoArtwork(track.artworkContentUri, track.title, Modifier.size(124.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Pale, maxLines = 2)
                Text(track.artist, fontSize = 15.sp, color = Subtle)
                Spacer(Modifier.height(10.dp))
                Text(state.recommendationReason ?: "", color = Lavender, fontSize = 13.sp, maxLines = 2)
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            GleoArtwork(track.artworkContentUri, track.title, Modifier.size(200.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, fontSize = 32.sp, color = Pale, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(track.artist, fontSize = 20.sp, color = Subtle)
                Spacer(Modifier.height(14.dp))
                Text(state.recommendationReason ?: "", color = Lavender, fontSize = 17.sp)
            }
        }
    }
    GlassPanel(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassButton("◀◀", modifier = Modifier.weight(1f), onClick = actions::onPrevious)
            GlassButton(
                if (state.playback == PlaybackStatus.PLAYING) "Ⅱ" else "▶",
                modifier = Modifier.weight(1f),
                onClick = actions::onPlayPause
            )
            GlassButton("▶▶", modifier = Modifier.weight(1f), onClick = actions::onNext)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassButton("추천 이유", modifier = Modifier.weight(1f), onClick = actions::onShowReasons)
        GlassButton("♡ pin! 기억", enabled = track != null, modifier = Modifier.weight(1f), onClick = actions::onPinCurrent)
    }
    if (state.canEditReview) GlassButton("주행 음악 정리", onClick = actions::onOpenReview)
}

@Composable
private fun CarVoice(state: GleoCarUiState, actions: GleoCarActions) {
    GleoHeader("듣고 있어요", "Gleo AI")
    Spacer(Modifier.height(22.dp))
    Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
        Text("◉", color = Lavender, fontSize = 72.sp)
    }
    GlassPanel(Modifier.fillMaxWidth()) {
        Text(state.voiceTranscript ?: "원하는 음악을 말씀해 주세요", color = Pale, fontSize = 20.sp)
    }
    GlassButton("음악으로 돌아가기", onClick = actions::onShowMusic)
}

@Composable
private fun CarReasons(state: GleoCarUiState, actions: GleoCarActions) {
    GleoHeader("음악 추천 이유", "지금의 이동과 음악 취향을 함께 고려했어요")
    if (state.recommendationReason != null) {
        GlassPanel(Modifier.fillMaxWidth()) { Text(state.recommendationReason, color = Pale, fontSize = 20.sp) }
    }
    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        state.context.forEach { item ->
            GlassPanel(Modifier.fillMaxWidth()) {
                Text(item.title, color = Lavender, fontWeight = FontWeight.Bold)
                Text(
                    item.detail ?: "정보 없음",
                    color = if (item.observed) Pale else Subtle,
                    fontSize = 15.sp
                )
            }
        }
    }
    GlassButton("음악으로 돌아가기", onClick = actions::onShowMusic)
}

@Composable
private fun CarPin(state: GleoCarUiState, actions: GleoCarActions) {
    GleoHeader("pin!", "마음에 드는 순간을 기록해요")
    if (state.currentTrack != null) {
        GlassPanel(Modifier.fillMaxWidth()) {
            Text(state.currentTrack.title, fontSize = 26.sp, color = Pale, fontWeight = FontWeight.Bold)
            Text(state.currentTrack.artist, color = Subtle)
        }
    }
    val status = when (state.pinSaveStatus) {
        SyncStatus.CONFIRMED -> "다른 기기에도 기록이 동기화됐어요"
        SyncStatus.PENDING -> "다른 기기와 동기화 대기 중"
        SyncStatus.LOCAL_ONLY -> "기기에 기록을 보관했어요"
        SyncStatus.ERROR -> "저장 상태를 확인해 주세요"
        null -> ""
    }
    if (status.isNotBlank()) Text(status, color = Success)
    GlassButton("재생 화면", onClick = actions::onShowMusic)
    if (state.canEditReview) GlassButton("주행 후 Review", onClick = actions::onOpenReview)
}

@Composable
private fun CarReview(state: GleoCarUiState, actions: GleoCarActions) {
    GleoHeader("오늘의 음악", "남기고 싶은 음악을 선택해 주세요")
    val available = tracksToRemember(state.reviewTracks, state.excludedMusicIds)
    if (state.reviewTracks.isEmpty()) {
        CenteredEmpty("이번 주행에서 정리할 음악이 없어요")
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.reviewTracks.forEach { track ->
                val excluded = track.musicId in state.excludedMusicIds
                var drag by remember(track.musicId) { mutableFloatStateOf(0f) }
                val dragModifier = Modifier.pointerInput(track.musicId, excluded) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            drag = (drag + amount).coerceIn(-150f, 0f)
                        },
                        onDragEnd = {
                            if (drag < -80f && !excluded) actions.onExcludeFromHearts(track.musicId)
                            drag = 0f
                        },
                        onDragCancel = { drag = 0f }
                    )
                }
                GlassPanel(Modifier.width(205.dp).then(dragModifier)) {
                    GleoArtwork(track.artworkContentUri, track.title, Modifier.fillMaxWidth().height(135.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(track.title, color = Pale, fontSize = 17.sp, maxLines = 1)
                    Text(track.artist, color = Subtle, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (excluded) "Hearts에서 제외됨" else if (drag < -25f) "↑ 놓으면 제외" else "위로 올려 제외",
                        color = if (excluded || drag < -25f) Destructive else Lavender,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            if (excluded) actions.onUndoExclude(track.musicId)
                        }
                    )
                    if (excluded) GlassButton("되돌리기", onClick = { actions.onUndoExclude(track.musicId) })
                }
            }
        }
    }
    GlassButton(
        "♡ Hearts에 기억하기 (${available.size}곡)",
        enabled = available.isNotEmpty() && state.canEditReview,
        modifier = Modifier.fillMaxWidth(),
        onClick = { actions.onRememberReviewedTracks(available) }
    )
}

@Composable
private fun CarRestricted(actions: GleoCarActions) {
    GleoHeader("주행 중에는 사용할 수 없어요", "안전한 정차 후 음악을 정리할 수 있어요")
    GlassButton("음악으로 돌아가기", onClick = actions::onShowMusic)
}

@Composable
private fun CarSaving() {
    GleoHeader("Hearts에 기록 중", "선택한 음악을 정리하고 있어요")
    Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
        Text("♡", fontSize = 76.sp, color = Lavender)
    }
    Text("저장이 완료되면 Hearts에서 다시 확인할 수 있어요", color = Subtle)
}

@Composable
private fun CarSaved(state: GleoCarUiState, actions: GleoCarActions) {
    GleoHeader("Hearts에 기억했어요", "주행과 음악이 남긴 순간")
    val message = if (state.pinSaveStatus == SyncStatus.CONFIRMED) "다른 기기와 동기화됐어요" else "음악 기록을 보관했어요"
    GlassPanel(Modifier.fillMaxWidth()) { Text(message, color = Success) }
    GlassButton("음악으로 돌아가기", onClick = actions::onShowMusic)
}

@Composable
private fun CarFailure(state: GleoCarUiState, actions: GleoCarActions) {
    GleoHeader("Gleo AI에 연결할 수 없어요", "음악은 계속 들을 수 있어요")
    Text(state.voiceError ?: "잠시 후 다시 시도해 주세요", color = Subtle)
    GlassButton("다시 말하기", onClick = actions::onVoiceRequest)
    GlassButton("음악으로 돌아가기", onClick = actions::onShowMusic)
}
