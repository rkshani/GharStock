package com.rk.gharstock.feature.stock

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.feature.stock.edit.StockEditScreen
import com.rk.gharstock.feature.stock.list.StockScreen
import kotlinx.serialization.Serializable

@Serializable
data object StockRoute

/** [itemId] 0 means a new item. */
@Serializable
data class StockEditRoute(val itemId: Long = 0)

fun NavGraphBuilder.stockScreens(navController: NavController) {
    composable<StockRoute> {
        StockScreen(
            onAddItem = { navController.navigate(StockEditRoute()) },
            onEditItem = { navController.navigate(StockEditRoute(it)) },
        )
    }
    composable<StockEditRoute> {
        StockEditScreen(onBack = navController::popBackStack)
    }
}
