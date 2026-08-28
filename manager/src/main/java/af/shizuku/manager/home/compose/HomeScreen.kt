package af.shizuku.manager.home.compose

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import af.shizuku.manager.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import af.shizuku.core.ui.compose.Button
import af.shizuku.core.ui.compose.ButtonSize
import af.shizuku.manager.ShizukuSettings
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isEditMode: Boolean,
    showEmptyState: Boolean,
    wallpaperTheme: String,
    onStopClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onRestoreHomeCards: () -> Unit,
    recyclerViewProvider: (Context, PaddingValues) -> RecyclerView
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        if (isEditMode) stringResource(R.string.home_edit_mode_hint) 
                        else stringResource(R.string.app_name)
                    ) 
                },
                actions = {
                    if (!isEditMode) {
                        IconButton(onClick = onStopClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close_24),
                                contentDescription = stringResource(id = R.string.action_stop)
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_outline_24),
                                contentDescription = stringResource(id = R.string.settings_title)
                            )
                        }
                        IconButton(onClick = onHelpClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_help_outline_24),
                                contentDescription = stringResource(id = R.string.settings_plus_learn_more)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = if (ShizukuSettings.isBlurUiEnabled())
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    else
                        MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        // Samsung OneUI one-handed mode: scale content to 75% and anchor to bottom-center,
        // matching Samsung's actual one-handed mode behavior instead of just adding top padding.
        // animateFloatAsState provides a smooth spring-physics transition when toggling the mode.
        val isOneHanded = ShizukuSettings.isOneHandedModeEnabled()
        val scale by animateFloatAsState(
            targetValue = if (isOneHanded) 0.75f else 1f,
            animationSpec = if (!ShizukuSettings.isExpressiveAnimationsEnabled()) {
                snap()
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium * ShizukuSettings.getAnimationDurationScale()
                )
            },
            label = "oneHandedScale"
        )
        val adjustedPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + 72.dp
        )
        AnimatedGradientBackground(wallpaperTheme = wallpaperTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        // Pivot at bottom-center (Samsung OneUI style)
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                    )
            ) {
            if (showEmptyState) {
                Box(modifier = Modifier.padding(adjustedPadding)) {
                    HomeEmptyState(onRestoreHomeCards)
                }
            } else {
                AndroidView(
                    factory = { context ->
                        recyclerViewProvider(context, adjustedPadding).also { rv ->
                            (rv.parent as? android.view.ViewGroup)?.removeView(rv)
                        }
                    },
                    update = { view -> recyclerViewProvider(view.context, adjustedPadding) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
}

@Composable
fun AnimatedGradientBackground(
    wallpaperTheme: String,
    content: @Composable () -> Unit
) {
    val animationsEnabled = ShizukuSettings.isExpressiveAnimationsEnabled()
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        // Clamp to a static value when expressive animations are disabled, matching every
        // other animated element in the app and avoiding a perpetual recomposition/battery cost.
        targetValue = if (animationsEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f + 0.05f * alpha)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.03f + 0.05f * (1f - alpha))
    val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
    val wallpaperRes = when (wallpaperTheme) {
        "white_miku" -> R.drawable.wallpaper_bg_light
        "black_miku" -> R.drawable.wallpaper_bg_dark
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (wallpaperRes != null) {
            Image(
                painter = painterResource(id = wallpaperRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.sweepGradient(
                        colors = listOf(color1, color2, color3, color1),
                        center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) // Sweep from bottom corner
                    )
                )
        )
        content()
    }
}

@Composable
fun HomeEmptyState(onRestoreHomeCards: () -> Unit) {
    val animationsEnabled = ShizukuSettings.isExpressiveAnimationsEnabled()
    val infiniteTransition = rememberInfiniteTransition()
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        // Clamp to a static value when expressive animations are disabled, matching every
        // other animated element in the app and avoiding a perpetual recomposition/battery cost.
        targetValue = if (animationsEnabled) 8f else -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_empty_home_24),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .offset(y = floatAnim.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_state_title_no_home_cards),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_state_description_no_home_cards),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            size = ButtonSize.Medium,
            onClick = onRestoreHomeCards
        ) {
            Text(stringResource(R.string.empty_state_action_restore_home_cards))
        }
    }
}
