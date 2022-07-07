package com.doops.mvvmtodo.ui.deleteallcompleted

import androidx.lifecycle.ViewModel
import com.doops.mvvmtodo.data.TaskDao
import com.doops.mvvmtodo.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}