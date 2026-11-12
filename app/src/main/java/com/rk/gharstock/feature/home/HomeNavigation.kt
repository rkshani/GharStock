package com.rk.gharstock.feature.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.feature.medicine.MedicineDetailRoute
import com.rk.gharstock.feature.stock.StockEditRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen(
    navController: NavController,
    onOpenStock: () -> Unit,
    onOpenShopping: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(
            onOpenStock = onOpenStock,
            onOpenShopping = onOpenShopping,
            onOpenDrawer = onOpenDrawer,
            onOpenStockItem = { navController.navigate(StockEditRoute(it)) },
            onOpenMedicine = { navController.navigate(MedicineDetailRoute(it)) },
        )
    }
}
