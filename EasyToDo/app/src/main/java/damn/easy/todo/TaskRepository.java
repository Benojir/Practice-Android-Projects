package damn.easy.todo;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TaskRepository {
    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
    }

    public void insert(Task task) {
        TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.delete(task));
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public void deleteCompletedTasks() {
        TaskDatabase.databaseWriteExecutor.execute(taskDao::deleteCompletedTasks);
    }

    public void deleteAllTasks() {
        TaskDatabase.databaseWriteExecutor.execute(taskDao::deleteAllTasks);
    }

    public void markAllTasksCompleted() {
        TaskDatabase.databaseWriteExecutor.execute(taskDao::markAllTasksCompleted);
    }

    public void markAllTasksUncompleted() {
        TaskDatabase.databaseWriteExecutor.execute(taskDao::markAllTasksUncompleted);
    }
}
