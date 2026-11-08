package com.rk.gharstock.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rk.gharstock.R
import com.rk.gharstock.core.model.Category
import com.rk.gharstock.core.model.MeasureUnit
import com.rk.gharstock.core.model.StoragePlace
import com.rk.gharstock.core.ui.text.labelRes

@Composable
fun UnitDropdown(
    selected: MeasureUnit,
    onSelect: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownField(
        label = stringResource(R.string.field_unit),
        options = MeasureUnit.entries,
        selectedText = stringResource(selected.labelRes),
        optionText = { stringResource(it.labelRes) },
        onSelect = onSelect,
        modifier = modifier,
    )
}

/** Category picker with a "None" option at the top. */
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val none = stringResource(R.string.none_selected)
    DropdownField(
        label = stringResource(R.string.field_category),
        options = listOf<Category?>(null) + categories,
        selectedText = selected?.name ?: none,
        optionText = { it?.name ?: none },
        onSelect = onSelect,
        modifier = modifier,
    )
}

/** Storage place picker with a "None" option at the top. */
@Composable
fun PlaceDropdown(
    places: List<StoragePlace>,
    selected: StoragePlace?,
    onSelect: (StoragePlace?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val none = stringResource(R.string.none_selected)
    DropdownField(
        label = stringResource(R.string.field_place),
        options = listOf<StoragePlace?>(null) + places,
        selectedText = selected?.name ?: none,
        optionText = { it?.name ?: none },
        onSelect = onSelect,
        modifier = modifier,
    )
}
