package com.erendogan6.havatahminim.ui.view.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme

private const val AWAIT_TIMEOUT_MS = 5_000L

/**
 * Shared setup for the Compose interaction tests. Screens take their ViewModel as a parameter
 * (the hiltViewModel() default only applies in the app), so the tests build real ViewModels over
 * the :core:testing fakes — no Hilt test graph, and no mocking library.
 */
fun ComposeContentTestRule.setScreen(content: @Composable () -> Unit) {
    setContent {
        HavaTahminimTheme(dynamicColor = false) { content() }
    }
}

/** Waits for [text] to render; the screens are fed by live pipelines, not immediate values. */
fun ComposeContentTestRule.awaitText(
    text: String,
    substring: Boolean = false,
) {
    waitUntil(AWAIT_TIMEOUT_MS) {
        onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
    }
}

/** Waits until no node matches [text]; the inverse assertion needs the same settling window. */
fun ComposeContentTestRule.awaitNoText(text: String) {
    waitUntil(AWAIT_TIMEOUT_MS) {
        onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
    }
}
