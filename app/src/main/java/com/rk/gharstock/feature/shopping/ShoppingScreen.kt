package com.rk.gharstock.feature.shopping

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object ShoppingRoute

fun NavGraphBuilder.shoppingScreen() {
    composable<ShoppingRoute> { ShoppingScreen() }
}

@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Outlined.ShoppingCart,
        title = R.string.nav_shopping,
        message = R.string.placeholder_shopping,
        modifier = modifier,
    )
}
