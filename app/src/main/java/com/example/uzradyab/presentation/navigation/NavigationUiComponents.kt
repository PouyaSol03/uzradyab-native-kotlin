package com.example.uzradyab.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.uzradyab.ui.theme.RoutingColors
import com.example.uzradyab.ui.theme.Vazirmatn

// ---------------------------------------------------------------------------
// Basics
// ---------------------------------------------------------------------------

private val OverlayShadow = Color(0xFF141828)

@Composable
internal fun NavText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    color: Color = RoutingColors.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: Float = 1.45f,
    textAlign: TextAlign? = null,
    ltr: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = TextStyle(
            fontFamily = Vazirmatn,
            fontSize = size,
            fontWeight = weight,
            lineHeight = size * lineHeight,
            textDirection = if (ltr) TextDirection.Ltr else TextDirection.Content,
        ),
    )
}

/** White map overlay surface with the soft shadow from the handoff. */
private fun Modifier.overlaySurface(shape: Shape, color: Color = RoutingColors.surface, elevation: Dp = 8.dp) =
    this
        .shadow(elevation, shape, ambientColor = OverlayShadow, spotColor = OverlayShadow)
        .background(color, shape)

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(26.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(CircleShape)
                .background(RoutingColors.outlineVariant),
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    container: Color,
    tint: Color,
    onClick: () -> Unit,
    size: Dp = 56.dp,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSize: Dp = 20.dp,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(iconSize))
            Spacer(Modifier.width(10.dp))
        }
        NavText(text, 16.sp, weight = FontWeight.Bold, color = content, maxLines = 1)
    }
}

/** "● متوقف" chip in the vehicle colors. */
@Composable
internal fun VehicleStatusChip(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(RoutingColors.vehicleContainer)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(RoutingColors.vehicle))
        NavText(text, 12.sp, weight = FontWeight.SemiBold, color = RoutingColors.onVehicleContainer, maxLines = 1)
    }
}

// ---------------------------------------------------------------------------
// Instruction banners
// ---------------------------------------------------------------------------

@Composable
private fun BannerIconTile(icon: ImageVector, exitNumber: Int? = null) {
    Box(modifier = Modifier.size(64.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
        }
        if (exitNumber != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                NavText(
                    NavigationFormat.number(exitNumber), 14.sp,
                    weight = FontWeight.ExtraBold, color = RoutingColors.banner, lineHeight = 1.1f,
                )
            }
        }
    }
}

/**
 * Blue maneuver banner with the optional "سپس" chip hanging under its start side.
 * Rotary steps get the roundabout icon with an exit badge.
 */
@Composable
fun InstructionBanner(
    step: GuidanceStep,
    distanceText: String,
    then: GuidanceStep?,
    modifier: Modifier = Modifier,
    /** Applied to the blue card only (not the chip), e.g. to measure where the card ends. */
    cardModifier: Modifier = Modifier,
    /** Tapping the banner opens (or closes) the full list of remaining steps. */
    onClick: (() -> Unit)? = null,
    /** Handle at the bottom of the banner, shown while the steps list is open. */
    showHandle: Boolean = false,
    /** The "سپس" chip folds away while the steps list is open. */
    showThen: Boolean = true,
) {
    val bannerShape = RoundedCornerShape(24.dp)
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .zIndex(1f)
                .then(cardModifier)
                .fillMaxWidth()
                .overlaySurface(bannerShape, RoutingColors.banner, elevation = 10.dp)
                .clip(bannerShape)
                .then(if (onClick != null) Modifier.clickable(onClickLabel = "مراحل بعدی", onClick = onClick) else Modifier)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (showHandle) 10.dp else 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BannerIconTile(
                    icon = NavigationIcons.maneuver(step.kind),
                    exitNumber = if (step.kind == ManeuverKind.ROTARY) step.rotaryExit else null,
                )
                Column(modifier = Modifier.weight(1f)) {
                    NavText(distanceText, 26.sp, weight = FontWeight.ExtraBold, color = Color.White, lineHeight = 1.25f, maxLines = 1)
                    NavText(step.title, 20.sp, weight = FontWeight.Bold, color = Color.White, lineHeight = 1.4f, maxLines = 1)
                    if (step.bannerInstruction.isNotBlank()) {
                        NavText(step.bannerInstruction, 13.sp, color = Color.White.copy(alpha = 0.85f), lineHeight = 1.5f, maxLines = 2)
                    }
                }
            }
            AnimatedVisibility(
                visible = showHandle,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enter = fadeIn(tween(200)) + expandVertically(tween(220)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
            ) {
                Box(
                    Modifier
                        .padding(top = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f)),
                )
            }
        }
        // Keep the last chip so it can animate away after the step list runs out.
        var lastThen by remember { mutableStateOf(then) }
        if (then != null) lastThen = then
        AnimatedVisibility(
            visible = showThen && then != null,
            // Tucked 12dp under the card; the offset sits outside the animation's clip.
            modifier = Modifier.offset(y = (-12).dp),
            enter = fadeIn(tween(200)) + expandVertically(tween(220), expandFrom = Alignment.Top),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200), shrinkTowards = Alignment.Top),
        ) {
            val chipStep = lastThen ?: return@AnimatedVisibility
            Row(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(RoutingColors.bannerNext)
                    .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavText("سپس", 13.sp, weight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f), maxLines = 1)
                Icon(NavigationIcons.maneuver(chipStep.kind), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                NavText(chipStep.chipText, 13.sp, weight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
            }
        }
    }
}

/** Amber (reroute) or green (arrived) banner. */
@Composable
fun StatusBanner(
    color: Color,
    icon: ImageVector,
    overline: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .overlaySurface(RoundedCornerShape(24.dp), color, elevation = 10.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BannerIconTile(icon)
        Column(modifier = Modifier.weight(1f)) {
            NavText(overline, 13.sp, weight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
            NavText(title, 20.sp, weight = FontWeight.ExtraBold, color = Color.White, lineHeight = 1.4f, maxLines = 1)
            NavText(subtitle, 13.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 2)
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom bar and map controls
// ---------------------------------------------------------------------------

/**
 * 128dp bar while navigating: end button on the start side, remaining time in the middle,
 * [trailingIcon] (overview / back to route) on the end side. Drag up or tap to open options.
 */
@Composable
fun NavBottomBar(
    primaryText: String,
    primaryColor: Color,
    secondaryText: String,
    onEnd: () -> Unit,
    /** Null leaves an empty slot, so the texts stay centered. */
    trailingIcon: ImageVector?,
    trailingDescription: String,
    onTrailing: () -> Unit,
    onOpenSheet: (() -> Unit)?,
    modifier: Modifier = Modifier,
    secondaryAlpha: Float = 1f,
) {
    var drag by remember { mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta -> drag += delta }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = OverlayShadow, spotColor = OverlayShadow)
            .background(RoutingColors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .then(
                if (onOpenSheet != null) {
                    Modifier.draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = {
                            if (drag < -40f) onOpenSheet()
                            drag = 0f
                        },
                    )
                } else {
                    Modifier
                },
            )
            .navigationBarsPadding()
            .height(128.dp)
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
    ) {
        DragHandle(
            modifier = if (onOpenSheet != null) Modifier.clickable(onClick = onOpenSheet) else Modifier,
        )
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircleIconButton(
                icon = NavigationIcons.Close,
                contentDescription = "پایان مسیریابی",
                container = RoutingColors.errorContainer,
                tint = RoutingColors.error,
                onClick = onEnd,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(if (onOpenSheet != null) Modifier.clickable(onClick = onOpenSheet) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NavText(primaryText, 24.sp, weight = FontWeight.ExtraBold, color = primaryColor, lineHeight = 1.3f, maxLines = 1)
                NavText(
                    secondaryText, 13.sp,
                    color = RoutingColors.onSurfaceVariant.copy(alpha = secondaryAlpha), lineHeight = 1.6f, maxLines = 1,
                )
            }
            if (trailingIcon != null) {
                CircleIconButton(
                    icon = trailingIcon,
                    contentDescription = trailingDescription,
                    container = RoutingColors.surfaceContainerHigh,
                    tint = RoutingColors.onSurface,
                    onClick = onTrailing,
                )
            } else {
                Spacer(Modifier.size(56.dp))
            }
        }
    }
}

/** 48dp squircle map button. */
@Composable
fun MapFab(onClick: () -> Unit, contentDescription: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .overlaySurface(RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
fun CompassFab(rotation: Float, onClick: () -> Unit) {
    MapFab(onClick = onClick, contentDescription = "شمال") {
        Image(NavigationIcons.Compass, contentDescription = "شمال", modifier = Modifier.size(22.dp).rotate(rotation))
    }
}

@Composable
fun IconFab(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    MapFab(onClick = onClick, contentDescription = contentDescription) {
        Icon(icon, contentDescription = contentDescription, tint = RoutingColors.onSurface, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun SpeedBadge(speedKmh: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .size(60.dp)
            .overlaySurface(CircleShape),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NavText(NavigationFormat.number(speedKmh.coerceAtLeast(0)), 20.sp, weight = FontWeight.ExtraBold, lineHeight = 1.1f)
        NavText("km/h", 9.sp, color = RoutingColors.onSurfaceVariant, lineHeight = 1.1f, ltr = true)
    }
}

@Composable
fun MapCredits(modifier: Modifier = Modifier) {
    NavText("© Neshan © OSM", 9.sp, modifier = modifier, color = Color(0xFF6B6F7A), ltr = true, maxLines = 1)
}

/** "بازگشت به مسیر" button shown in overview; also "بازگشت به نقشه" in the steps list. */
@Composable
fun ReturnToRouteButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String = "بازگشت به مسیر") {
    Row(
        modifier = modifier
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = RoutingColors.primary, spotColor = RoutingColors.primary)
            .clip(RoundedCornerShape(18.dp))
            .background(RoutingColors.primary)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(NavigationIcons.NavigationArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        NavText(text, 15.sp, weight = FontWeight.Bold, color = Color.White, maxLines = 1)
    }
}

// ---------------------------------------------------------------------------
// Remaining steps (full screen, opened from the banner)
// ---------------------------------------------------------------------------

@Composable
fun UpcomingStepRow(
    step: GuidanceStep,
    distanceText: String,
    highlighted: Boolean,
    shaded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (shaded) RoutingColors.surfaceContainerLow else RoutingColors.surface)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (highlighted) RoutingColors.primaryContainer else RoutingColors.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    NavigationIcons.maneuver(step.kind),
                    contentDescription = null,
                    tint = if (highlighted) RoutingColors.primary else RoutingColors.onSurface,
                    modifier = Modifier.size(28.dp),
                )
            }
            if (step.kind == ManeuverKind.ROTARY && step.rotaryExit != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(RoutingColors.primary)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    NavText(NavigationFormat.number(step.rotaryExit), 12.sp, weight = FontWeight.Bold, color = Color.White, lineHeight = 1.1f)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            if (distanceText.isNotBlank()) {
                NavText(distanceText, 20.sp, weight = FontWeight.ExtraBold, lineHeight = 1.35f, maxLines = 1)
            }
            NavText(step.title, 18.sp, weight = FontWeight.Bold, color = RoutingColors.primary, lineHeight = 1.4f, maxLines = 1)
            if (step.subtitle.isNotBlank()) {
                NavText(step.subtitle, 13.sp, color = RoutingColors.onSurfaceVariant, lineHeight = 1.5f, maxLines = 2)
            }
        }
    }
}

private const val STAGGERED_ROWS = 8
private const val ROW_DELAY_MS = 45L

/**
 * Fades and slides a row in after [index] * [ROW_DELAY_MS]. Only alpha and translation change,
 * in the draw phase, so rows are never re-measured while animating.
 */
@Composable
private fun Modifier.staggeredEntrance(index: Int, enabled: Boolean): Modifier {
    if (!enabled || index >= STAGGERED_ROWS) return this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(120L + index * ROW_DELAY_MS) // let the panel start unrolling first
        progress.animateTo(1f, tween(durationMillis = 260, easing = FastOutSlowInEasing))
    }
    val shift = with(LocalDensity.current) { 16.dp.toPx() }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * -shift
    }
}

/**
 * The remaining steps under the banner (the banner itself stays on the screen above it),
 * with a compact bottom bar. [rows] pairs each step with the distance driven before it.
 */
@Composable
fun UpcomingStepsPanel(
    rows: List<Pair<GuidanceStep, String>>,
    remainingTime: String,
    remainingDetail: String,
    onBackToMap: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stagger only while the panel opens, not when rows scroll back into view.
    var entering by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(120L + STAGGERED_ROWS * ROW_DELAY_MS + 300L)
        entering = false
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoutingColors.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavText("مراحل بعدی", 16.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f), maxLines = 1)
            NavText(
                "${NavigationFormat.number(rows.size)} مرحله تا خودرو", 13.sp,
                color = RoutingColors.onSurfaceVariant, maxLines = 1,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(RoutingColors.outlineVariant))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                itemsIndexed(rows) { index, (step, distance) ->
                    Column(Modifier.staggeredEntrance(index, entering)) {
                        UpcomingStepRow(
                            step = step,
                            distanceText = distance,
                            highlighted = index == 0,
                            shaded = index % 2 == 1,
                        )
                        if (index < rows.lastIndex) {
                            Box(
                                Modifier
                                    .padding(start = 84.dp)
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(RoutingColors.outlineVariant),
                            )
                        }
                    }
                }
            }
            ReturnToRouteButton(
                onClick = onBackToMap,
                text = "بازگشت به نقشه",
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(RoutingColors.outlineVariant))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircleIconButton(
                icon = NavigationIcons.Close,
                contentDescription = "پایان مسیریابی",
                container = RoutingColors.errorContainer,
                tint = RoutingColors.error,
                onClick = onEnd,
            )
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                NavText(remainingTime, 22.sp, weight = FontWeight.ExtraBold, color = RoutingColors.success, maxLines = 1)
                NavText(" · $remainingDetail", 14.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Route preview
// ---------------------------------------------------------------------------

@Composable
fun PreviewTopCard(
    destinationName: String,
    statusText: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .overlaySurface(RoundedCornerShape(20.dp))
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(NavigationIcons.ArrowRight, contentDescription = "بازگشت", tint = RoutingColors.onSurface)
        }
        Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
            Row(Modifier.height(44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .size(14.dp)
                        .border(4.dp, RoutingColors.primary, CircleShape),
                )
                Column {
                    NavText("مبدأ", 11.sp, color = RoutingColors.onSurfaceVariant, lineHeight = 1.4f)
                    NavText("موقعیت فعلی شما", 14.sp, weight = FontWeight.SemiBold, lineHeight = 1.5f, maxLines = 1)
                }
            }
            Row(Modifier.height(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.width(14.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(width = 2.dp, height = 12.dp)) {
                        drawLine(
                            color = RoutingColors.outline,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = size.width,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())),
                        )
                    }
                }
                Box(Modifier.weight(1f).height(1.dp).background(RoutingColors.outlineVariant))
            }
            Row(Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(RoutingColors.vehicle))
                Column(Modifier.weight(1f)) {
                    NavText("مقصد", 11.sp, color = RoutingColors.onSurfaceVariant, lineHeight = 1.4f)
                    NavText(destinationName, 14.sp, weight = FontWeight.SemiBold, lineHeight = 1.5f, maxLines = 1)
                }
                if (statusText != null) VehicleStatusChip(statusText)
            }
        }
    }
}

@Composable
fun RouteCard(
    name: String,
    time: String,
    distance: String,
    summary: String,
    fastest: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val content = if (selected) RoutingColors.onPrimaryContainer else RoutingColors.onSurface
    Column(
        modifier = modifier
            .height(108.dp)
            .clip(shape)
            .background(if (selected) RoutingColors.primaryContainer else RoutingColors.surfaceContainerLow)
            .border(
                if (selected) BorderStroke(2.dp, RoutingColors.primary) else BorderStroke(1.dp, RoutingColors.outlineVariant),
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            NavText(name, 12.sp, weight = FontWeight.SemiBold, color = content, maxLines = 1, modifier = Modifier.weight(1f))
            if (fastest) {
                NavText(
                    "سریع‌ترین", 10.sp,
                    weight = FontWeight.Bold,
                    color = if (selected) Color.White else RoutingColors.onPrimaryContainer,
                    maxLines = 1,
                    lineHeight = 1.4f,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) RoutingColors.primary else RoutingColors.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        NavText(time, 22.sp, weight = FontWeight.ExtraBold, color = content, lineHeight = 1.2f, maxLines = 1)
        NavText(distance, 12.sp, color = content, lineHeight = 1.5f, maxLines = 1)
        NavText(summary, 11.sp, color = content.copy(alpha = 0.8f), lineHeight = 1.5f, maxLines = 1)
    }
}

data class RouteCardData(
    val name: String,
    val time: String,
    val distance: String,
    val summary: String,
    val fastest: Boolean,
)

@Composable
fun PreviewSheet(
    cards: List<RouteCardData>,
    selectedIndex: Int,
    etaText: String,
    routeInfoText: String,
    onSelect: (Int) -> Unit,
    onDetails: () -> Unit,
    onStart: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = OverlayShadow, spotColor = OverlayShadow)
            .background(RoutingColors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        DragHandle()
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavText("مسیرهای پیشنهادی", 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDetails)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(NavigationIcons.StepList, contentDescription = null, tint = RoutingColors.primary, modifier = Modifier.size(18.dp))
                NavText("جزئیات مسیر", 13.sp, weight = FontWeight.SemiBold, color = RoutingColors.primary, maxLines = 1)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            cards.chunked(2).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEachIndexed { column, card ->
                        val index = rowIndex * 2 + column
                        RouteCard(
                            name = card.name,
                            time = card.time,
                            distance = card.distance,
                            summary = card.summary,
                            fastest = card.fastest,
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1 && cards.size > 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(NavigationIcons.Clock, contentDescription = null, tint = RoutingColors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            NavText("رسیدن حدود", 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
            NavText(etaText, 13.sp, weight = FontWeight.Bold, maxLines = 1)
            NavText("|", 13.sp, color = RoutingColors.outlineVariant, maxLines = 1)
            NavText(routeInfoText, 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillButton(
                text = "شروع مسیریابی",
                onClick = onStart,
                container = RoutingColors.primary,
                content = Color.White,
                icon = NavigationIcons.NavigationArrow,
                modifier = Modifier.weight(1f),
            )
            CircleIconButton(
                icon = NavigationIcons.Share,
                contentDescription = "اشتراک‌گذاری مسیر",
                container = RoutingColors.primaryContainer,
                tint = RoutingColors.onPrimaryContainer,
                onClick = onShare,
                iconSize = 22.dp,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Steps
// ---------------------------------------------------------------------------

@Composable
fun ManeuverTile(
    kind: ManeuverKind,
    exitNumber: Int?,
    container: Color,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(40.dp)) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(NavigationIcons.maneuver(kind), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        if (exitNumber != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(RoutingColors.primary)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                NavText(NavigationFormat.number(exitNumber), 11.sp, weight = FontWeight.Bold, color = Color.White, lineHeight = 1.1f)
            }
        }
    }
}

/**
 * One step: icon tile on the start side, title and subtitle, distance on the end side.
 * [lineAbove] / [lineBelow] draw the vertical line that joins the tiles.
 */
@Composable
fun StepRow(
    step: GuidanceStep,
    distanceText: String,
    modifier: Modifier = Modifier,
    rowHighlighted: Boolean = false,
    tileContainer: Color = RoutingColors.surfaceContainerHigh,
    tileTint: Color = RoutingColors.onSurface,
    lineAbove: Boolean = false,
    lineBelow: Boolean = false,
    horizontalPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .heightIn(min = 64.dp)
            .background(if (rowHighlighted) RoutingColors.primaryContainer.copy(alpha = 0.55f) else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.width(40.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            if (lineAbove || lineBelow) {
                Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.width(2.dp).weight(1f).background(if (lineAbove) RoutingColors.outlineVariant else Color.Transparent))
                    Box(Modifier.width(2.dp).weight(1f).background(if (lineBelow) RoutingColors.outlineVariant else Color.Transparent))
                }
            }
            ManeuverTile(
                kind = step.kind,
                exitNumber = if (step.kind == ManeuverKind.ROTARY) step.rotaryExit else null,
                container = tileContainer,
                tint = tileTint,
            )
        }
        Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
            NavText(step.title, 15.sp, weight = FontWeight.Bold, maxLines = 1, lineHeight = 1.5f)
            if (step.subtitle.isNotBlank()) {
                NavText(step.subtitle, 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 2, lineHeight = 1.5f)
            }
        }
        if (distanceText.isNotBlank()) {
            NavText(distanceText, 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun StepsTopBar(title: String, subtitle: String, onBack: () -> Unit, onShare: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RoutingColors.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Icon(NavigationIcons.ArrowRight, contentDescription = "بازگشت", tint = RoutingColors.onSurface)
        }
        Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
            NavText(title, 18.sp, weight = FontWeight.ExtraBold, maxLines = 1, lineHeight = 1.4f)
            NavText(subtitle, 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onShare), contentAlignment = Alignment.Center) {
            Icon(NavigationIcons.Share, contentDescription = "اشتراک‌گذاری", tint = RoutingColors.onSurface)
        }
    }
}

@Composable
private fun RowScope.InfoChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RoutingColors.surfaceContainerLow)
            .border(1.dp, RoutingColors.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = RoutingColors.onSurface, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        NavText(text, 14.sp, weight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun StepsSheet(
    steps: List<GuidanceStep>,
    focusedIndex: Int,
    distanceChip: String,
    durationChip: String,
    rotaryChip: String,
    etaText: String,
    actionText: String,
    onStepClick: (Int) -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(focusedIndex) {
        if (focusedIndex > 0) listState.animateScrollToItem((focusedIndex - 1).coerceAtLeast(0))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = OverlayShadow, spotColor = OverlayShadow)
            .background(RoutingColors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoChip(NavigationIcons.Route, distanceChip)
            InfoChip(NavigationIcons.Clock, durationChip)
            InfoChip(NavigationIcons.Rotary, rotaryChip)
        }
        NavText(
            "${NavigationFormat.number(steps.size)} مرحله · روی هر مرحله بزنید تا روی نقشه نشان داده شود",
            12.sp,
            color = RoutingColors.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp)) {
            itemsIndexed(steps) { index, step ->
                val focused = index == focusedIndex
                StepRow(
                    step = step,
                    distanceText = if (step.kind == ManeuverKind.ARRIVE) "" else NavigationFormat.distance(step.distanceMeters),
                    rowHighlighted = focused,
                    tileContainer = if (focused) RoutingColors.primary else RoutingColors.surfaceContainerHigh,
                    tileTint = if (focused) Color.White else RoutingColors.onSurface,
                    lineAbove = index > 0,
                    lineBelow = index < steps.lastIndex,
                    onClick = { onStepClick(index) },
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(RoutingColors.outlineVariant))
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                NavText("رسیدن حدود", 12.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
                NavText(etaText, 22.sp, weight = FontWeight.ExtraBold, maxLines = 1, lineHeight = 1.25f)
            }
            PillButton(
                text = actionText,
                onClick = onAction,
                container = RoutingColors.primary,
                content = Color.White,
                icon = NavigationIcons.NavigationArrow,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Navigation options sheet
// ---------------------------------------------------------------------------

@Composable
private fun RowScope.ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(if (active) RoutingColors.primaryContainer else RoutingColors.surfaceContainerLow)
            .border(1.dp, if (active) RoutingColors.primary else RoutingColors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val content = if (active) RoutingColors.onPrimaryContainer else RoutingColors.onSurface
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp).padding(bottom = 2.dp))
        NavText(title, 14.sp, weight = FontWeight.Bold, color = content, maxLines = 1, textAlign = TextAlign.Center)
        NavText(subtitle, 12.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1, textAlign = TextAlign.Center)
    }
}

@Composable
fun NavOptionsSheet(
    remainingTime: String,
    remainingDetail: String,
    destinationName: String,
    upcoming: List<Pair<GuidanceStep, String>>,
    totalSteps: Int,
    voiceEnabled: Boolean,
    onDetails: () -> Unit,
    onShareEta: () -> Unit,
    onToggleVoice: () -> Unit,
    onEnd: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var drag by remember { mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta -> drag += delta }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RoutingColors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                onDragStopped = {
                    if (drag > 60f) onDismiss()
                    drag = 0f
                },
            )
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        DragHandle(Modifier.clickable(onClick = onDismiss))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                NavText(remainingTime, 28.sp, weight = FontWeight.ExtraBold, color = RoutingColors.success, lineHeight = 1.3f, maxLines = 1)
                NavText(remainingDetail, 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.Start) {
                NavText("مقصد", 12.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(RoutingColors.vehicle))
                    NavText(destinationName, 15.sp, weight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
        Box(Modifier.padding(vertical = 16.dp).fillMaxWidth().height(1.dp).background(RoutingColors.outlineVariant))
        if (upcoming.isNotEmpty()) {
            NavText("پیش رو", 13.sp, weight = FontWeight.SemiBold, color = RoutingColors.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            upcoming.forEachIndexed { index, (step, distance) ->
                StepRow(
                    step = step,
                    distanceText = distance,
                    horizontalPadding = 0.dp,
                    tileContainer = if (index == 0) RoutingColors.primaryContainer else RoutingColors.surfaceContainerHigh,
                    tileTint = if (index == 0) RoutingColors.primary else RoutingColors.onSurface,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                NavigationIcons.StepList, "جزئیات مسیر",
                "همه‌ی ${NavigationFormat.number(totalSteps)} مرحله", active = false, onClick = onDetails,
            )
            ActionTile(NavigationIcons.Share, "ارسال زمان رسیدن", "پیام‌رسان‌ها", active = false, onClick = onShareEta)
            ActionTile(
                if (voiceEnabled) NavigationIcons.VolumeOn else NavigationIcons.VolumeOff,
                "راهنمای صوتی",
                if (voiceEnabled) "روشن" else "خاموش",
                active = voiceEnabled,
                onClick = onToggleVoice,
            )
        }
        Spacer(Modifier.height(24.dp))
        PillButton(
            text = "پایان مسیریابی",
            onClick = onEnd,
            container = RoutingColors.errorContainer,
            content = RoutingColors.error,
            icon = NavigationIcons.Close,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ---------------------------------------------------------------------------
// Arrived
// ---------------------------------------------------------------------------

@Composable
private fun RowScope.StatTile(label: String, value: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(RoutingColors.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        NavText(label, 12.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
        NavText(value, 18.sp, weight = FontWeight.ExtraBold, maxLines = 1, lineHeight = 1.35f)
    }
}

@Composable
fun ArrivedSheet(
    vehicleName: String,
    lastUpdateText: String,
    statusText: String?,
    traveled: String,
    tripDuration: String,
    distanceToVehicle: String,
    address: String?,
    onFinish: () -> Unit,
    onManageDevice: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = OverlayShadow, spotColor = OverlayShadow)
            .background(RoutingColors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(RoutingColors.vehicleContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(NavigationIcons.Car, contentDescription = null, tint = RoutingColors.vehicle, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f)) {
                NavText(vehicleName, 22.sp, weight = FontWeight.ExtraBold, maxLines = 1, lineHeight = 1.3f)
                NavText("آخرین بروزرسانی: $lastUpdateText", 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 1)
            }
            if (statusText != null) VehicleStatusChip(statusText)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("مسافت طی‌شده", traveled)
            StatTile("مدت سفر", tripDuration)
            StatTile("فاصله تا خودرو", distanceToVehicle)
        }
        if (!address.isNullOrBlank()) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(NavigationIcons.Pin, contentDescription = null, tint = RoutingColors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                NavText(address, 13.sp, color = RoutingColors.onSurfaceVariant, maxLines = 2)
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillButton(
                text = "پایان مسیریابی",
                onClick = onFinish,
                container = RoutingColors.primary,
                content = Color.White,
                icon = NavigationIcons.Check,
                modifier = Modifier.weight(1.6f),
            )
            if (onManageDevice != null) {
                PillButton(
                    text = "مدیریت دستگاه",
                    onClick = onManageDevice,
                    container = RoutingColors.primaryContainer,
                    content = RoutingColors.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Loading / errors
// ---------------------------------------------------------------------------

@Composable
fun MessageCard(
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    primaryAction: Pair<String, () -> Unit>? = null,
    secondaryAction: Pair<String, () -> Unit>? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .overlaySurface(RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (loading) CircularProgressIndicator(color = RoutingColors.primary, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
        NavText(title, 16.sp, weight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (message != null) NavText(message, 13.sp, color = RoutingColors.onSurfaceVariant, textAlign = TextAlign.Center)
        if (primaryAction != null || secondaryAction != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                primaryAction?.let { (text, action) ->
                    PillButton(text, action, RoutingColors.primary, Color.White, modifier = Modifier.weight(1f))
                }
                secondaryAction?.let { (text, action) ->
                    PillButton(text, action, RoutingColors.surfaceContainerHigh, RoutingColors.onSurface, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
