package com.rk.gharstock.feature.stock

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object StockRoute

fun NavGraphBuilder.stockScreen() {
    composable<StockRoute> { StockScreen() }
}

@Composable
fun StockScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Outlined.Inventory2,
        title = R.string.nav_stock,
        message = R.string.placeholder_stock,
        modifier = modifier,
    )
}
