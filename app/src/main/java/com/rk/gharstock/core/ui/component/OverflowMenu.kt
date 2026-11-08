package com.rk.gharstock.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.rk.gharstock.R

data class OverflowMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/** Three-dot button that shows a menu of actions. */
@Composable
fun OverflowMenu(items: List<OverflowMenuItem>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    enabled = item.enabled,
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                )
            }
        }
    }
}
