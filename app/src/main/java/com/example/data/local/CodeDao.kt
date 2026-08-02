package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Query("SELECT * FROM code_files WHERE projectId = :projectId ORDER BY isDirectory DESC, name ASC")
    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>>

    @Query("SELECT * FROM code_files WHERE id = :fileId")
    suspend fun getFileById(fileId: Long): CodeFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CodeFileEntity): Long

    @Update
    suspend fun updateFile(file: CodeFileEntity)

    @Delete
    suspend fun deleteFile(file: CodeFileEntity)

    @Query("DELETE FROM code_files WHERE projectId = :projectId AND path LIKE :pathPrefix || '%'")
    suspend fun deleteFilesByPathPrefix(projectId: Long, pathPrefix: String)

    @Query("SELECT * FROM git_commits WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity)
}
