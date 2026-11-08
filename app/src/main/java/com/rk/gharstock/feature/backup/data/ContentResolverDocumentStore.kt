package com.rk.gharstock.feature.backup.data

import android.content.Context
import androidx.core.net.toUri
import com.rk.gharstock.feature.backup.domain.DocumentStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentResolverDocumentStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DocumentStore {

    override suspend fun read(uri: String): String = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openInputStream(uri.toUri()) ?: throw FileNotFoundException(uri)
        stream.bufferedReader().use { it.readText() }
    }

    override suspend fun write(uri: String, text: String) = withContext(Dispatchers.IO) {
        // "wt" truncates, so overwriting an existing file doesn't leave old bytes at the end.
        val stream = context.contentResolver.openOutputStream(uri.toUri(), "wt") ?: throw FileNotFoundException(uri)
        stream.bufferedWriter().use { it.write(text) }
    }
}
