package com.rk.gharstock.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rk.gharstock.R

/** Screen layout shared by the "manage" lists: categories, places and family members. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListScaffold(
    title: String,
    addLabel: String,
    isLoading: Boolean,
    isEmpty: Boolean,
    emptyIcon: ImageVector,
    emptyTitle: String,
    emptyMessage: String,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = addLabel) }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            isLoading -> LoadingState(modifier)
            isEmpty -> EmptyState(emptyIcon, emptyTitle, emptyMessage, modifier, addLabel, onAdd)
            else -> LazyColumn(modifier, contentPadding = PaddingValues(bottom = 88.dp), content = content)
        }
    }
}

/** One row with a title, optional subtitle and Edit/Delete menu. */
@Composable
fun ManageListRow(
    title: String,
    subtitle: String?,
    editLabel: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            OverflowMenu(
                listOf(
                    OverflowMenuItem(editLabel, onEdit),
                    OverflowMenuItem(stringResource(R.string.action_delete), onDelete),
                ),
            )
        },
    )
    HorizontalDivider()
}
