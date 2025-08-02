package damn.easy.todo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;

    public TaskViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public void deleteCompletedTasks() {
        repository.deleteCompletedTasks();
    }

    public void deleteAllTasks() {
        repository.deleteAllTasks();
    }

    public void markAllTasksCompleted() {
        repository.markAllTasksCompleted();
    }

    public void markAllTasksUncompleted() {
        repository.markAllTasksUncompleted();
    }
}
