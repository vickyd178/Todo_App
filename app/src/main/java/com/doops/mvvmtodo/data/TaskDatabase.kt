package com.doops.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doops.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    class CallBack @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().taskDao()

            applicationScope
                .launch {
                    dao.insert(Task("Wash the dishes"))
                    dao.insert(Task("Do the laundry"))
                    dao.insert(Task("Buy groceries", important = true))
                    dao.insert(Task("Prepare food"))
                    dao.insert(Task("Call Mom", completed = true))
                    dao.insert(Task("Service Bike",completed = true))
                    dao.insert(Task("Visit Home"))
                }
        }
    }
}