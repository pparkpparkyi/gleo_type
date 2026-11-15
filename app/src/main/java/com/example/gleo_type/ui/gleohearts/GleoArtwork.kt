package com.example.gleo_type.ui.gleohearts

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Displays the actual content URI if available. Does not ship any licensed album
 * image or make an HTTP request on the UI thread. For https artwork, resolve / cache
 * it through the existing media repository, then pass a local content URI.
 */
@Composable
internal fun GleoArtwork(
    contentUri: String?,
    description: String,
    modifier: Modifier = Modifier,
    placeholder: String = "♫"
) {
    val context = LocalContext.current
    val artwork by produceState<ImageBitmap?>(initialValue = null, key1 = contentUri) {
        value = withContext(Dispatchers.IO) {
            if (contentUri.isNullOrBlank() || contentUri.startsWith("http")) return@withContext null
            try {
                context.contentResolver.openInputStream(Uri.parse(contentUri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Grad),
        contentAlignment = Alignment.Center
    ) {
        if (artwork != null) {
            Image(
                bitmap = artwork!!,
                contentDescription = description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                placeholder,
                color = Lavender,
                fontSize = 44.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
