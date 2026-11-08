package com.rk.gharstock.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.feature.settings.about.AboutScreen
import com.rk.gharstock.feature.settings.catalog.ManageCatalogScreen
import com.rk.gharstock.feature.settings.members.FamilyMembersScreen
import com.rk.gharstock.feature.settings.more.MoreDestination
import com.rk.gharstock.feature.settings.more.MoreScreen
import kotlinx.serialization.Serializable

enum class CatalogKind { CATEGORY, PLACE }

@Serializable
data object MoreRoute

@Serializable
data class ManageCatalogRoute(val kind: CatalogKind)

@Serializable
data object FamilyMembersRoute

@Serializable
data object AboutRoute

fun NavGraphBuilder.settingsScreens(navController: NavController) {
    composable<MoreRoute> {
        MoreScreen(
            onOpen = { destination ->
                navController.navigate(
                    when (destination) {
                        MoreDestination.CATEGORIES -> ManageCatalogRoute(CatalogKind.CATEGORY)
                        MoreDestination.PLACES -> ManageCatalogRoute(CatalogKind.PLACE)
                        MoreDestination.MEMBERS -> FamilyMembersRoute
                        MoreDestination.ABOUT -> AboutRoute
                    },
                )
            },
        )
    }
    composable<ManageCatalogRoute> { ManageCatalogScreen(onBack = navController::popBackStack) }
    composable<FamilyMembersRoute> { FamilyMembersScreen(onBack = navController::popBackStack) }
    composable<AboutRoute> { AboutScreen(onBack = navController::popBackStack) }
}
