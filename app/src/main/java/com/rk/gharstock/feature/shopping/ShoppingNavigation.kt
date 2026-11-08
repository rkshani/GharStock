package com.rk.gharstock.feature.shopping

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.feature.shopping.detail.ShoppingListDetailScreen
import com.rk.gharstock.feature.shopping.lists.ShoppingListsScreen
import kotlinx.serialization.Serializable

@Serializable
data object ShoppingRoute

@Serializable
data class ShoppingListDetailRoute(val listId: Long)

fun NavGraphBuilder.shoppingScreens(navController: NavController) {
    composable<ShoppingRoute> {
        ShoppingListsScreen(onOpenList = { navController.navigate(ShoppingListDetailRoute(it)) })
    }
    composable<ShoppingListDetailRoute> {
        ShoppingListDetailScreen(onBack = navController::popBackStack)
    }
}
