package com.rk.gharstock.core.ui.component

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/** A one-time snackbar message held in UiState until the screen reports it as shown. */
sealed interface UserMessage {
    data class Text(@StringRes val res: Int, val arg: String? = null) : UserMessage
    data class Plural(@PluralsRes val res: Int, val count: Int) : UserMessage
}

@Composable
private fun UserMessage.resolve(): String = when (this) {
    is UserMessage.Text -> if (arg == null) stringResource(res) else stringResource(res, arg)
    is UserMessage.Plural -> pluralStringResource(res, count, count)
}

/** Shows [message] in [hostState] once, then calls [onShown]. */
@Composable
fun UserMessageEffect(message: UserMessage?, hostState: SnackbarHostState, onShown: () -> Unit) {
    val text = message?.resolve() ?: return
    LaunchedEffect(message) {
        hostState.showSnackbar(text)
        onShown()
    }
}
