package com.navtasks.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity

/**
 * Search is an intermediate Back destination:
 * 1. When the IME is visible, Android handles Back and hides the keyboard.
 * 2. Once the IME is gone, the next Back hides search, without leaving the screen.
 * 3. When search is closed, parent/page Back navigation works normally.
 */
internal fun shouldHandleSearchBack(searchOpen: Boolean, imeVisible: Boolean): Boolean =
    searchOpen && !imeVisible

internal object NavSearchTransition {
    const val showHeightMs = 260
    const val showFadeMs = 220
    const val hideHeightMs = 230
    const val hideFadeMs = 170
}

@Composable
internal fun NavAnimatedSearchField(
    searchOpen: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    p: NavPalette,
    lang: UiLanguage,
    placeholder: String,
    resultCount: Int = 0,
    currentResult: Int = 0,
    onPreviousResult: () -> Unit = {},
    onNextResult: () -> Unit = {},
) {
    // Read actual Compose IME insets. Never intercept the first Back that
    // belongs to the keyboard; our handler is armed only after it closes.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler(enabled = shouldHandleSearchBack(searchOpen, imeVisible)) {
        onDismiss()
    }

    AnimatedVisibility(
        visible = searchOpen,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = NavSearchTransition.showHeightMs,
                easing = FastOutSlowInEasing,
            ),
            expandFrom = Alignment.Top,
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = NavSearchTransition.showFadeMs,
                delayMillis = 25,
                easing = LinearOutSlowInEasing,
            ),
        ),
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = NavSearchTransition.hideHeightMs,
                easing = FastOutSlowInEasing,
            ),
            shrinkTowards = Alignment.Top,
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = NavSearchTransition.hideFadeMs,
            ),
        ),
    ) {
        // Retain the existing autofocus/keyboard logic, search filtering and
        // glass styling; only animate the container that reveals the input.
        Column {
            NavGlassSearchField(value, onValueChange, p, lang, placeholder)
            if (searchOpen && value.trim().isNotEmpty()) {
                NavSearchMatchControls(
                    count = resultCount, current = currentResult,
                    p = p, lang = lang,
                    onPrevious = onPreviousResult, onNext = onNextResult,
                )
            }
        }
    }
}
