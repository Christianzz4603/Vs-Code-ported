package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.example.data.local.CodeFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object SafManager {

    const val SAF_WORKSPACE_PREFS = "saf_workspace_prefs"
    const val KEY_RECENT_SAF_URIS = "recent_saf_uris"

    fun getPersistedUris(context: Context): List<Uri> {
        val permissions = context.contentResolver.persistedUriPermissions
        return permissions.map { it.uri }
    }

    fun persistUriPermission(context: Context, uri: Uri) {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            saveRecentUri(context, uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveRecentUri(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(SAF_WORKSPACE_PREFS, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_RECENT_SAF_URIS, emptySet()) ?: emptySet()
        val updated = current.toMutableSet().apply { add(uri.toString()) }
        prefs.edit().putStringSet(KEY_RECENT_SAF_URIS, updated).apply()
    }

    suspend fun readSafTree(
        context: Context,
        treeUri: Uri,
        projectId: Long
    ): List<CodeFileEntity> = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val result = mutableListOf<CodeFileEntity>()

        fun processDocument(doc: DocumentFile, parentPath: String) {
            val name = doc.name ?: "unnamed"
            val currentPath = if (parentPath.isEmpty()) name else "$parentPath/$name"

            if (doc.isDirectory) {
                result.add(
                    CodeFileEntity(
                        projectId = projectId,
                        name = name,
                        path = currentPath,
                        content = "",
                        language = "folder",
                        isDirectory = true
                    )
                )
                doc.listFiles().forEach { child: DocumentFile ->
                    processDocument(child, currentPath)
                }
            } else if (doc.isFile) {
                val content = readDocumentContent(context, doc.uri)
                val lang = LanguageUtils.detectLanguage(name, content)
                result.add(
                    CodeFileEntity(
                        projectId = projectId,
                        name = name,
                        path = currentPath,
                        content = content,
                        language = lang,
                        isDirectory = false
                    )
                )
            }
        }

        rootDoc.listFiles().forEach { child: DocumentFile ->
            processDocument(child, "")
        }

        return@withContext result
    }

    fun readDocumentContent(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun writeDocumentContent(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "rwt")?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createSafDocumentInTree(
        context: Context,
        treeUri: Uri,
        fileName: String,
        mimeType: String = "text/plain",
        content: String = ""
    ): DocumentFile? {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return null
        val created = rootDoc.createFile(mimeType, fileName)
        if (created != null && content.isNotEmpty()) {
            writeDocumentContent(context, created.uri, content)
        }
        return created
    }
}
