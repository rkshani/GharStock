package com.rk.gharstock.feature.medicine

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.feature.medicine.detail.MedicineDetailScreen
import com.rk.gharstock.feature.medicine.edit.MedicineEditScreen
import com.rk.gharstock.feature.medicine.list.MedicineListScreen
import com.rk.gharstock.feature.settings.FamilyMembersRoute
import kotlinx.serialization.Serializable

@Serializable
data object MedicineRoute

@Serializable
data class MedicineDetailRoute(val medicineId: Long)

/** [medicineId] 0 means a new medicine; [memberId] pre-selects the member for a new one. */
@Serializable
data class MedicineEditRoute(val medicineId: Long = 0, val memberId: Long = 0)

fun NavGraphBuilder.medicineScreens(navController: NavController) {
    composable<MedicineRoute> {
        MedicineListScreen(
            onAddMedicine = { navController.navigate(MedicineEditRoute()) },
            onOpenMedicine = { navController.navigate(MedicineDetailRoute(it)) },
            onManageMembers = { navController.navigate(FamilyMembersRoute) },
        )
    }
    composable<MedicineDetailRoute> {
        MedicineDetailScreen(
            onBack = navController::popBackStack,
            onEdit = { navController.navigate(MedicineEditRoute(medicineId = it)) },
        )
    }
    composable<MedicineEditRoute> {
        MedicineEditScreen(onBack = navController::popBackStack)
    }
}
