package com.rk.gharstock.feature.medicine

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rk.gharstock.R
import com.rk.gharstock.core.ui.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object MedicineRoute

fun NavGraphBuilder.medicineScreen() {
    composable<MedicineRoute> { MedicineScreen() }
}

@Composable
fun MedicineScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Outlined.Medication,
        title = R.string.nav_medicine,
        message = R.string.placeholder_medicine,
        modifier = modifier,
    )
}
