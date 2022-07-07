package com.doops.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doops.mvvmtodo.R
import com.doops.mvvmtodo.data.Task
import com.doops.mvvmtodo.databinding.FragmentTasksBinding
import com.doops.mvvmtodo.util.SortOrder
import com.doops.mvvmtodo.util.exhaustive
import com.doops.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {
    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(
                    0,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.addEditResult(result)
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.undoDeleteClick(event.task)
                            }.show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragment2ToAddEditTaskFragment(
                                null,
                                "New Task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragment2ToAddEditTaskFragment(
                                event.task,
                                "Edit Task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {

                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                    }
                    TasksViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =
                            TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }

        }
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_complete_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                return true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                return true
            }
            R.id.action_hide_complete_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                return true
            }
            R.id.action_delete_all_complete_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}