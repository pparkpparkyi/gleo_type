package com.example.gleo_type.ui.gleohearts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Midnight = Color(0xFF0C0F20)
internal val Panel = Color(0xFF222038)
internal val Accent = Color(0xFF8B6BCC)
internal val Lavender = Color(0xFFD5BEFA)
internal val Pale = Color(0xFFFFF8FF)
internal val Subtle = Color(0xFFC9BDD9)
internal val Success = Color(0xFFBBEED8)
internal val Destructive = Color(0xFFDD5B76)
internal val Grad = Brush.linearGradient(listOf(Color(0xFF20172F), Color(0xFF121327), Color(0xFF2C214D)))

@Composable
fun GleoHeartsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Pale,
            secondary = Lavender,
            background = Midnight,
            surface = Panel,
            onSurface = Pale,
            error = Destructive
        ),
        content = content
    )
}

@Composable
internal fun GlassPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(Color(0xBA2C2945), RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), RoundedCornerShape(20.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
internal fun GlassButton(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = Pale,
            disabledContainerColor = Color(0xFF4C445D),
            disabledContentColor = Subtle
        )
    ) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 15.sp)
    }
}

@Composable
internal fun GleoHeader(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Pale)
        if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 13.sp, color = Subtle)
    }
}

@Composable
internal fun TinyStateLabel(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.10f)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Lavender, fontSize = 12.sp)
    }
}

@Composable
internal fun CenteredEmpty(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().heightIn(min = 120.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Subtle, fontSize = 15.sp)
    }
}
