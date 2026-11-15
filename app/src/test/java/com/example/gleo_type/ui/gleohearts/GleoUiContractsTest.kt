package com.example.gleo_type.ui.gleohearts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GleoUiContractsTest {
    private val songs = listOf(
        GleoTrack("track-newjeans", "Super Shy", "NewJeans"),
        GleoTrack("track-aespa", "Armageddon", "aespa"),
        GleoTrack("track-ive", "I AM", "IVE")
    )

    @Test fun excludedOnlyFromHeartsCandidateNotRawHistory() {
        val result = tracksToRemember(songs, setOf("track-aespa"))
        assertEquals(listOf("track-newjeans", "track-ive"), result)
        assertEquals(3, songs.size) // no source record was mutated
    }

    @Test fun editingForbiddenByDefault() {
        assertFalse(allowedReviewActions(GleoCarUiState()))
        assertTrue(allowedReviewActions(GleoCarUiState(canEditReview = true)))
    }

    @Test fun saveAndSyncNeverDefaultToConfirmed() {
        assertEquals(SyncStatus.LOCAL_ONLY, GleoDriveRecord("drive-1", "Drive", "2026-11-15", null, songs).syncStatus)
        assertEquals(null, GleoCarUiState().pinSaveStatus)
    }
}
