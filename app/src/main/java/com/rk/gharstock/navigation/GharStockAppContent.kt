package com.rk.gharstock.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rk.gharstock.feature.home.HomeRoute
import com.rk.gharstock.feature.home.homeScreen
import com.rk.gharstock.feature.medicine.medicineScreens
import com.rk.gharstock.feature.settings.settingsScreens
import com.rk.gharstock.feature.shopping.shoppingScreens
import com.rk.gharstock.feature.stock.stockScreens
import kotlinx.coroutines.launch

@Composable
fun GharStockAppContent() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = TopLevelDestination.entries.any { currentDestination.isRoute(it) }
    val isHome = currentDestination.isRoute(TopLevelDestination.HOME)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    BackHandler(enabled = drawerState.isOpen, onBack = closeDrawer)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isHome || drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                isHomeSelected = isHome,
                onOpenHome = { navController.navigateToTopLevel(TopLevelDestination.HOME) },
                onClose = closeDrawer,
            )
        },
    ) {
        // Inner screens draw their own top bars, so only the bottom bar's space is reserved here.
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    GharStockBottomBar(
                        currentDestination = currentDestination,
                        onNavigate = navController::navigateToTopLevel,
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                homeScreen(
                    navController = navController,
                    onOpenStock = { navController.navigateToTopLevel(TopLevelDestination.STOCK) },
                    onOpenShopping = { navController.navigateToTopLevel(TopLevelDestination.SHOPPING) },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                )
                shoppingScreens(navController)
                stockScreens(navController)
                medicineScreens(navController)
                settingsScreens(navController)
            }
        }
    }
}

@Composable
private fun GharStockBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination.isRoute(destination)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}

private fun NavDestination?.isRoute(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
