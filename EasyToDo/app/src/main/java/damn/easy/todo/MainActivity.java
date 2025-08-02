package damn.easy.todo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TaskViewModel taskViewModel;
    private View emptyView;
    private Date selectedDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final TaskAdapter adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        // FIX: Use submitList for ListAdapter
        taskViewModel.getAllTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            if (tasks.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(view -> {
            selectedDueDate = null;
            showAddEditTaskDialog(null, adapter);
        });

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                selectedDueDate = task.getDueDate();
                showAddEditTaskDialog(task, adapter);
            }

            @Override
            public void onCheckChange(Task task) {
                taskViewModel.update(task);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Task taskToDelete = adapter.getTaskAt(viewHolder.getAdapterPosition());
                taskViewModel.delete(taskToDelete);
                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> taskViewModel.insert(taskToDelete))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void showAddEditTaskDialog(final Task task, TaskAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTextTask = dialogView.findViewById(R.id.edit_text_task);
        RadioGroup radioGroupPriority = dialogView.findViewById(R.id.radio_group_priority);
        Button buttonDueDate = dialogView.findViewById(R.id.button_due_date);
        TextView textViewDueDate = dialogView.findViewById(R.id.text_view_due_date);

        if (task != null) {
            builder.setTitle("Edit Task");
            editTextTask.setText(task.getTitle());
            switch (task.getPriority()) {
                case 1:
                    radioGroupPriority.check(R.id.radio_button_low);
                    break;
                case 2:
                    radioGroupPriority.check(R.id.radio_button_medium);
                    break;
                case 3:
                    radioGroupPriority.check(R.id.radio_button_high);
                    break;
            }
        } else {
            builder.setTitle("Add New Task");
        }

        if (selectedDueDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            textViewDueDate.setText("Due: " + sdf.format(selectedDueDate));
            textViewDueDate.setVisibility(View.VISIBLE);
        } else {
            // Today date
            Calendar calendar = Calendar.getInstance();
            selectedDueDate = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            textViewDueDate.setText("Due: " + sdf.format(selectedDueDate));
        }

        buttonDueDate.setOnClickListener(v -> showDatePickerDialog(textViewDueDate));

        builder.setPositiveButton(task != null ? "Save" : "Add", (dialog, which) -> {
            String taskTitle = editTextTask.getText().toString().trim();
            if (taskTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
                return;
            }

            int priority = 2; // Default priority
            int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
            if (selectedPriorityId == R.id.radio_button_low) {
                priority = 1;
            } else if (selectedPriorityId == R.id.radio_button_high) {
                priority = 3;
            }

            if (task != null) {
                // Editing an existing task
                task.setTitle(taskTitle);
                task.setPriority(priority);
                task.setDueDate(selectedDueDate);
                taskViewModel.update(task);
                adapter.notifyDataSetChanged();
            } else {
                // FIX: Use the new constructor with 4 arguments
                Task newTask = new Task(taskTitle, false, priority, selectedDueDate);
                taskViewModel.insert(newTask);
            }

        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @SuppressLint("SetTextI18n")
    private void showDatePickerDialog(final TextView textViewDueDate) {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate != null) {
            calendar.setTime(selectedDueDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, month, dayOfMonth);
                    selectedDueDate = newDate.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    textViewDueDate.setText("Due: " + sdf.format(selectedDueDate));
                    textViewDueDate.setVisibility(View.VISIBLE);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private boolean allTasksMarked;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // In a real app, you would check the item ID
        if (item.getItemId() == R.id.delete_completed_tasks) {
            taskViewModel.deleteCompletedTasks();
            Toast.makeText(this, "Completed tasks deleted", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.check_all_item) {
            if (!allTasksMarked) {
                taskViewModel.markAllTasksCompleted();
                Toast.makeText(this, "All tasks marked as completed", Toast.LENGTH_SHORT).show();
                allTasksMarked = true;
            } else {
                taskViewModel.markAllTasksUncompleted();
                Toast.makeText(this, "All tasks marked as uncompleted", Toast.LENGTH_SHORT).show();
                allTasksMarked = false;
            }
            return true;
        } else if (item.getItemId() == R.id.delete_all_tasks) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Delete All Tasks");
            builder.setMessage("Are you sure you want to delete all tasks?");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                taskViewModel.deleteAllTasks();
                Toast.makeText(this, "All tasks deleted", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
