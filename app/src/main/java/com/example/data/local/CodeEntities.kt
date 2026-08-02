package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val templateType: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "code_files")
data class CodeFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val path: String,
    val content: String,
    val language: String,
    val isModified: Boolean = false,
    val isDirectory: Boolean = false,
    val parentPath: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "git_commits")
data class GitCommitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val message: String,
    val author: String = "Developer",
    val hash: String,
    val timestamp: Long = System.currentTimeMillis()
)
