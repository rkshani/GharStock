package com.rk.gharstock.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object MoreRoute

fun NavGraphBuilder.moreScreen() {
    composable<MoreRoute> { MoreScreen() }
}

@Composable
fun MoreScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Outlined.MoreHoriz,
        title = R.string.nav_more,
        message = R.string.placeholder_more,
        modifier = modifier,
    )
}
