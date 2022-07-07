package com.doops.mvvmtodo.data

import androidx.room.*
import com.doops.mvvmtodo.util.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(searchQuery: String, sortOrder: SortOrder, hideCompleted: Boolean) =
        when (sortOrder) {
            SortOrder.BY_NAME -> getTasksSortedByName(searchQuery, hideCompleted)
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(searchQuery, hideCompleted)
        }


    @Query("SELECT * FROM task_table WHERE (completed!=:hideCompleted OR completed =0) AND name LIKE '%' || :searchQuery|| '%' ORDER BY important DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>


    @Query("SELECT * FROM task_table WHERE (completed!=:hideCompleted OR completed =0) AND name LIKE '%' || :searchQuery|| '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()
}