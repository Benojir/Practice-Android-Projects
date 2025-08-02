package damn.easy.todo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM task_table")
    void deleteAllTasks();

    @Query("SELECT * FROM task_table ORDER BY priority DESC, dueDate ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("DELETE FROM task_table WHERE isCompleted = 1")
    void deleteCompletedTasks();

    @Query("UPDATE task_table SET isCompleted = 1")
    void markAllTasksCompleted();

    @Query("UPDATE task_table SET isCompleted = 0")
    void markAllTasksUncompleted();
}
