package com.example.uzradyab.presentation.events

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

/** Left-to-right mark: keeps "date - time" in order inside the RTL layout. */
private val LRM = Char(0x200E)

private const val COLLAPSED_REPLY_LINES = 3

/** Event card for `commandResult`: title, reply status, time, and the device reply as plain text. */
@Composable
fun CommandResultEventCard(event: EventUiModel, modifier: Modifier = Modifier) {
    val primary = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))
    val greyText = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))
    val softSurface = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
    val reply = event.commandResult

    var expanded by rememberSaveable(event.id) { mutableStateOf(false) }
    var overflowing by remember(event.id) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (reply != null) themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F)) else softSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Terminal,
                        contentDescription = null,
                        tint = if (reply != null) primary else greyText,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        color = UzradyabTheme.colors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ReplyStatusPill(hasReply = reply != null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$LRM${event.time}",
                    color = greyText,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                )
            }

            if (reply != null) {
                Spacer(modifier = Modifier.height(10.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = reply,
                        color = UzradyabTheme.colors.textPrimary,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Left,
                        maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_REPLY_LINES,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { result -> if (!expanded) overflowing = result.hasVisualOverflow },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(softSurface)
                            .animateContentSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }

                if (overflowing || expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ReplyToggle(
                        expanded = expanded,
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyStatusPill(hasReply: Boolean) {
    val background = if (hasReply) themedColor(light = Color(0xFFE6F4EA), dark = Color(0xFF1B3A26)) else themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
    val textColor = if (hasReply) themedColor(light = Color(0xFF1E8E3E), dark = Color(0xFF81C995)) else themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))
    Text(
        text = stringResource(if (hasReply) R.string.event_reply_received else R.string.event_reply_empty),
        color = textColor,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** Centred pill that expands or collapses the reply text. */
@Composable
private fun ReplyToggle(expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val primary = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F)))
            .border(1.dp, primary.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(if (expanded) R.string.event_reply_show_less else R.string.event_reply_show_more),
            color = primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = primary,
            modifier = Modifier.size(16.dp),
        )
    }
}
