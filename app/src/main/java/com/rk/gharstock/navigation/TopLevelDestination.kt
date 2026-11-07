package com.rk.gharstock.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.rk.gharstock.R
import com.rk.gharstock.feature.home.HomeRoute
import com.rk.gharstock.feature.medicine.MedicineRoute
import com.rk.gharstock.feature.settings.MoreRoute
import com.rk.gharstock.feature.shopping.ShoppingRoute
import com.rk.gharstock.feature.stock.StockRoute

/** Tabs shown in the bottom navigation bar, in display order. */
enum class TopLevelDestination(
    val route: Any,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(HomeRoute, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    SHOPPING(ShoppingRoute, R.string.nav_shopping, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    STOCK(StockRoute, R.string.nav_stock, Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    MEDICINE(MedicineRoute, R.string.nav_medicine, Icons.Filled.Medication, Icons.Outlined.Medication),
    MORE(MoreRoute, R.string.nav_more, Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz),
}
