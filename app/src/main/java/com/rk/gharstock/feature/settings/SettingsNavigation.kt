package com.rk.gharstock.feature.settings

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.feature.backup.BackupScreen
import com.rk.gharstock.feature.report.ReportScreen
import com.rk.gharstock.feature.settings.about.AboutScreen
import com.rk.gharstock.feature.settings.catalog.ManageCatalogScreen
import com.rk.gharstock.feature.settings.members.FamilyMembersScreen
import com.rk.gharstock.feature.settings.more.MoreDestination
import com.rk.gharstock.feature.settings.more.MoreScreen
import kotlinx.serialization.Serializable

/** Kept so its serializer survives R8, as it is used in a navigation route. */
@Keep
enum class CatalogKind { CATEGORY, PLACE }

@Serializable
data object MoreRoute

@Serializable
data class ManageCatalogRoute(val kind: CatalogKind)

@Serializable
data object FamilyMembersRoute

@Serializable
data object AboutRoute

@Serializable
data object ReportRoute

@Serializable
data object BackupRoute

fun NavGraphBuilder.settingsScreens(navController: NavController) {
    composable<MoreRoute> {
        MoreScreen(
            onOpen = { destination ->
                navController.navigate(
                    when (destination) {
                        MoreDestination.REPORT -> ReportRoute
                        MoreDestination.CATEGORIES -> ManageCatalogRoute(CatalogKind.CATEGORY)
                        MoreDestination.PLACES -> ManageCatalogRoute(CatalogKind.PLACE)
                        MoreDestination.MEMBERS -> FamilyMembersRoute
                        MoreDestination.BACKUP -> BackupRoute
                        MoreDestination.ABOUT -> AboutRoute
                    },
                )
            },
        )
    }
    composable<ManageCatalogRoute> { ManageCatalogScreen(onBack = navController::popBackStack) }
    composable<FamilyMembersRoute> { FamilyMembersScreen(onBack = navController::popBackStack) }
    composable<AboutRoute> { AboutScreen(onBack = navController::popBackStack) }
    composable<ReportRoute> { ReportScreen(onBack = navController::popBackStack) }
    composable<BackupRoute> { BackupScreen(onBack = navController::popBackStack) }
}
