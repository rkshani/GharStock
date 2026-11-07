package com.rk.gharstock.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen() {
    composable<HomeRoute> { HomeScreen() }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Outlined.Home,
        title = R.string.nav_home,
        message = R.string.placeholder_home,
        modifier = modifier,
    )
}
